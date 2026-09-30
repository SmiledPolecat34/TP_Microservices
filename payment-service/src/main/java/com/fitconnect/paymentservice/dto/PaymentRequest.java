package com.fitconnect.paymentservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fitconnect.paymentservice.model.Payment;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * Demande de paiement envoyée par booking-service. Référence, date et statut sont fixés par le
 * service.
 */
public record PaymentRequest(
    @NotNull Long bookingId,
    String bookingReference,
    @NotNull Long userId,
    @NotNull @DecimalMin("0.00") BigDecimal amount,
    @NotNull Payment.PaymentMethod paymentMethod,
    @Pattern(regexp = "[0-9]{4}") String cardLastFour,
    String transactionId) {

  @JsonIgnore
  @AssertTrue(message = "cardLastFour est obligatoire pour un paiement par carte")
  public boolean isCardLastFourValid() {
    return (paymentMethod != Payment.PaymentMethod.CREDIT_CARD
            && paymentMethod != Payment.PaymentMethod.DEBIT_CARD)
        || cardLastFour != null;
  }

  public Payment toEntity() {
    return Payment.builder()
        .bookingId(bookingId)
        .bookingReference(bookingReference)
        .userId(userId)
        .amount(amount)
        .paymentMethod(paymentMethod)
        .cardLastFour(cardLastFour)
        .transactionId(transactionId)
        .build();
  }
}
