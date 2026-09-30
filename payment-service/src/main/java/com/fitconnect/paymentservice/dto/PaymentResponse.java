package com.fitconnect.paymentservice.dto;

import com.fitconnect.paymentservice.model.Payment;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Vue d'un paiement renvoyée par l'API. */
public record PaymentResponse(
    Long id,
    String paymentReference,
    Long bookingId,
    String bookingReference,
    Long userId,
    BigDecimal amount,
    Payment.PaymentMethod paymentMethod,
    String cardLastFour,
    String transactionId,
    LocalDateTime paymentDate,
    Payment.Status status) {

  public static PaymentResponse from(Payment p) {
    return new PaymentResponse(
        p.getId(),
        p.getPaymentReference(),
        p.getBookingId(),
        p.getBookingReference(),
        p.getUserId(),
        p.getAmount(),
        p.getPaymentMethod(),
        p.getCardLastFour(),
        p.getTransactionId(),
        p.getPaymentDate(),
        p.getStatus());
  }
}
