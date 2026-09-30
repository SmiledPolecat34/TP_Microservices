package com.fitconnect.bookingservice.dto;

import com.fitconnect.bookingservice.client.*;
import com.fitconnect.bookingservice.controller.*;
import com.fitconnect.bookingservice.exception.*;
import com.fitconnect.bookingservice.model.*;
import com.fitconnect.bookingservice.repository.*;
import com.fitconnect.bookingservice.scheduler.*;
import com.fitconnect.bookingservice.service.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class Contracts {
  private Contracts() {}

  public record CreateBooking(
      @NotNull Long userId,
      @Email @NotBlank String userEmail,
      @NotBlank String userName,
      @NotNull Long classId,
      @Min(1) @Max(4) int numberOfSpots) {}

  public record ConfirmBooking(
      @NotNull PaymentMethod paymentMethod,
      @Pattern(regexp = "\\d{4}") String cardLastFour,
      String transactionId) {
    @com.fasterxml.jackson.annotation.JsonIgnore
    @AssertTrue(message = "cardLastFour est obligatoire pour un paiement par carte")
    public boolean isCardLastFourValid() {
      return (paymentMethod != PaymentMethod.CREDIT_CARD
              && paymentMethod != PaymentMethod.DEBIT_CARD)
          || cardLastFour != null;
    }
  }

  public enum PaymentMethod {
    CREDIT_CARD,
    DEBIT_CARD,
    PAYPAL,
    STRIPE
  }

  public record ClassView(
      Long id,
      String name,
      String instructor,
      String gymLocation,
      String category,
      String level,
      Integer maxParticipants,
      Integer currentParticipants,
      BigDecimal price,
      LocalDateTime dateTime,
      String status) {}

  public record PaymentRequest(
      Long bookingId,
      String bookingReference,
      Long userId,
      BigDecimal amount,
      PaymentMethod paymentMethod,
      String cardLastFour,
      String transactionId) {}

  public record PaymentView(Long id, String paymentReference, Long bookingId, String status) {}

  public record NotificationRequest(
      Long userId, String email, String type, String subject, String content) {}
}
