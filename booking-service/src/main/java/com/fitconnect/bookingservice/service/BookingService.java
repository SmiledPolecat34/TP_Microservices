package com.fitconnect.bookingservice.service;

import com.fitconnect.bookingservice.client.*;
import com.fitconnect.bookingservice.controller.*;
import com.fitconnect.bookingservice.dto.*;
import com.fitconnect.bookingservice.exception.*;
import com.fitconnect.bookingservice.model.*;
import com.fitconnect.bookingservice.repository.*;
import com.fitconnect.bookingservice.scheduler.*;
import jakarta.persistence.EntityNotFoundException;
import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingService {
  private final BookingRepository repository;
  private final ClassClient classes;
  private final PaymentClient payments;
  private final NotificationClient notifications;

  // 1 heure par défaut (cahier des charges) ; réglable pour démontrer l'expiration sans attendre.
  @org.springframework.beans.factory.annotation.Value("${booking.payment-deadline-minutes:60}")
  private long paymentDeadlineMinutes = 60;

  public List<Booking> all() {
    return repository.findAll();
  }

  public Booking get(Long id) {
    return repository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Réservation introuvable: " + id));
  }

  public List<Booking> byUser(Long id) {
    return repository.findByUserIdOrderByBookingDateDesc(id);
  }

  @Transactional
  public Booking create(Contracts.CreateBooking r) {
    Contracts.ClassView c = classes.get(r.classId());
    if (!"SCHEDULED".equals(c.status()) || c.dateTime().isBefore(LocalDateTime.now()))
      throw new IllegalStateException("Ce cours n'est pas réservable");
    if (c.currentParticipants() + r.numberOfSpots() > c.maxParticipants())
      throw new IllegalStateException("Plus de places disponibles pour ce cours");
    classes.increment(c.id(), r.numberOfSpots());
    boolean saved = false;
    try {
      LocalDateTime now = LocalDateTime.now();
      Booking b =
          Booking.builder()
              .bookingReference("BK-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase())
              .userId(r.userId())
              .userEmail(r.userEmail())
              .userName(r.userName())
              .classId(c.id())
              .className(c.name())
              .classDate(c.dateTime())
              .instructor(c.instructor())
              .price(c.price())
              .numberOfSpots(r.numberOfSpots())
              .totalAmount(c.price().multiply(java.math.BigDecimal.valueOf(r.numberOfSpots())))
              .bookingDate(now)
              .status(Booking.Status.PENDING_PAYMENT)
              .paymentDeadline(
                  c.dateTime().isBefore(now.plusMinutes(paymentDeadlineMinutes))
                      ? c.dateTime()
                      : now.plusMinutes(paymentDeadlineMinutes))
              .cancellationDeadline(c.dateTime().minusHours(24))
              .build();
      b = repository.save(b);
      saved = true;
      notify(
          b,
          "BOOKING_CONFIRMATION",
          "Réservation en attente de paiement",
          "Votre réservation est en attente de paiement. Payez avant " + b.getPaymentDeadline());
      return b;
    } catch (RuntimeException e) {
      if (!saved) classes.decrement(c.id(), r.numberOfSpots());
      throw e;
    }
  }

  @Transactional
  public Booking confirm(Long id, Contracts.ConfirmBooking r) {
    Booking b = get(id);
    if (b.getStatus() != Booking.Status.PENDING_PAYMENT)
      throw new IllegalStateException("La réservation n'est pas en attente de paiement");
    if (b.getPaymentDeadline().isBefore(LocalDateTime.now()))
      throw new IllegalStateException("Le délai de paiement est expiré");
    Contracts.PaymentView p =
        payments.pay(
            new Contracts.PaymentRequest(
                b.getId(),
                b.getBookingReference(),
                b.getUserId(),
                b.getTotalAmount(),
                r.paymentMethod(),
                r.cardLastFour(),
                r.transactionId()));
    if ("SUCCESS".equals(p.status())) {
      b.setStatus(Booking.Status.CONFIRMED);
      notify(
          b,
          "PAYMENT_CONFIRMATION",
          "Paiement confirmé",
          "Paiement " + p.paymentReference() + " confirmé pour " + b.getClassName());
    } else {
      b.setStatus(Booking.Status.CANCELLED);
      classes.decrement(b.getClassId(), b.getNumberOfSpots());
      notify(
          b,
          "BOOKING_CANCELLED",
          "Paiement refusé",
          "Le paiement a échoué et la réservation a été annulée");
    }
    return b;
  }

  @Transactional
  public Booking cancel(Long id) {
    Booking b = get(id);
    if (b.getStatus() == Booking.Status.CANCELLED || b.getStatus() == Booking.Status.COMPLETED)
      throw new IllegalStateException("Cette réservation ne peut plus être annulée");
    if (LocalDateTime.now().isAfter(b.getCancellationDeadline()))
      throw new IllegalStateException("Le délai d'annulation gratuite est dépassé");
    if (b.getStatus() == Booking.Status.CONFIRMED) {
      Contracts.PaymentView p = payments.byBooking(b.getId());
      payments.refund(p.id());
    }
    classes.decrement(b.getClassId(), b.getNumberOfSpots());
    b.setStatus(Booking.Status.CANCELLED);
    notify(
        b,
        "BOOKING_CANCELLED",
        "Réservation annulée",
        "Votre réservation " + b.getBookingReference() + " a été annulée");
    return b;
  }

  @Transactional
  public Booking complete(Long id) {
    Booking b = get(id);
    if (b.getStatus() != Booking.Status.CONFIRMED)
      throw new IllegalStateException("Seule une réservation confirmée peut être terminée");
    b.setStatus(Booking.Status.COMPLETED);
    return b;
  }

  // Annulation d'un cours par la salle : toutes les réservations actives sont annulées et
  // remboursées, sans condition de délai.
  @Transactional
  public List<Booking> cancelForClass(Long classId) {
    List<Booking> active =
        repository.findByClassIdAndStatusIn(
            classId, List.of(Booking.Status.PENDING_PAYMENT, Booking.Status.CONFIRMED));
    active.forEach(
        b -> {
          if (b.getStatus() == Booking.Status.CONFIRMED)
            payments.refund(payments.byBooking(b.getId()).id());
          b.setStatus(Booking.Status.CANCELLED);
          notify(
              b,
              "CLASS_CANCELLED",
              "Cours annulé",
              "Le cours "
                  + b.getClassName()
                  + " du "
                  + b.getClassDate()
                  + " est annulé. Votre réservation "
                  + b.getBookingReference()
                  + " est annulée et tout paiement est remboursé");
        });
    return active;
  }

  public List<Booking> expired() {
    return repository.findByStatusAndPaymentDeadlineBefore(
        Booking.Status.PENDING_PAYMENT, LocalDateTime.now());
  }

  private void notify(Booking b, String type, String subject, String content) {
    notifications.send(
        new Contracts.NotificationRequest(b.getUserId(), b.getUserEmail(), type, subject, content));
  }
}
