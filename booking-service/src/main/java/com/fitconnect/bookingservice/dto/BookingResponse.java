package com.fitconnect.bookingservice.dto;

import com.fitconnect.bookingservice.model.Booking;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Vue d'une réservation renvoyée par l'API : l'entité JPA et ses champs techniques ne sont pas
 * exposés.
 */
public record BookingResponse(
    Long id,
    String bookingReference,
    Long userId,
    String userEmail,
    String userName,
    Long classId,
    String className,
    LocalDateTime classDate,
    String instructor,
    BigDecimal price,
    Integer numberOfSpots,
    BigDecimal totalAmount,
    LocalDateTime bookingDate,
    Booking.Status status,
    LocalDateTime paymentDeadline,
    LocalDateTime cancellationDeadline) {

  public static BookingResponse from(Booking b) {
    return new BookingResponse(
        b.getId(),
        b.getBookingReference(),
        b.getUserId(),
        b.getUserEmail(),
        b.getUserName(),
        b.getClassId(),
        b.getClassName(),
        b.getClassDate(),
        b.getInstructor(),
        b.getPrice(),
        b.getNumberOfSpots(),
        b.getTotalAmount(),
        b.getBookingDate(),
        b.getStatus(),
        b.getPaymentDeadline(),
        b.getCancellationDeadline());
  }
}
