package com.fitconnect.bookingservice.scheduler;

import com.fitconnect.bookingservice.client.*;
import com.fitconnect.bookingservice.controller.*;
import com.fitconnect.bookingservice.dto.*;
import com.fitconnect.bookingservice.exception.*;
import com.fitconnect.bookingservice.model.*;
import com.fitconnect.bookingservice.repository.*;
import com.fitconnect.bookingservice.service.*;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingScheduler {
  private final BookingRepository repository;
  private final ClassClient classes;
  private final NotificationClient notifications;

  // L'annulation locale est enregistrée avant l'appel distant et chaque réservation est traitée
  // isolément :
  // une panne en aval ne peut ni bloquer les autres, ni faire libérer deux fois les mêmes places au
  // passage suivant.
  @Scheduled(fixedRate = 300000)
  public void expirePayments() {
    repository
        .findByStatusAndPaymentDeadlineBefore(Booking.Status.PENDING_PAYMENT, LocalDateTime.now())
        .forEach(
            b -> {
              b.setStatus(Booking.Status.CANCELLED);
              repository.save(b);
              try {
                classes.decrement(b.getClassId(), b.getNumberOfSpots());
                notifications.send(
                    new Contracts.NotificationRequest(
                        b.getUserId(),
                        b.getUserEmail(),
                        "BOOKING_CANCELLED",
                        "Réservation expirée",
                        "Le délai de paiement de " + b.getBookingReference() + " est expiré"));
              } catch (RuntimeException e) {
                log.warn(
                    "Réservation {} annulée mais places non libérées dans class-service: {}",
                    b.getBookingReference(),
                    e.getMessage());
              }
            });
  }

  @Scheduled(fixedRate = 300000)
  @Transactional
  public void sendReminders() {
    LocalDateTime target = LocalDateTime.now().plusHours(24);
    repository
        .findByStatusAndClassDateBetweenAndReminderSentFalse(
            Booking.Status.CONFIRMED, target.minusMinutes(3), target.plusMinutes(3))
        .forEach(
            b -> {
              notifications.send(
                  new Contracts.NotificationRequest(
                      b.getUserId(),
                      b.getUserEmail(),
                      "BOOKING_REMINDER",
                      "Rappel de cours",
                      "Votre cours " + b.getClassName() + " commence dans 24 heures"));
              b.setReminderSent(true);
            });
  }
}
