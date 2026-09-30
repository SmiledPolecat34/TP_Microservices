package com.fitconnect.paymentservice.model;

import com.fitconnect.paymentservice.controller.*;
import com.fitconnect.paymentservice.exception.*;
import com.fitconnect.paymentservice.repository.*;
import com.fitconnect.paymentservice.service.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String paymentReference;

  @NotNull private Long bookingId;
  private String bookingReference;
  @NotNull private Long userId;

  @NotNull
  @DecimalMin("0.00")
  private BigDecimal amount;

  @NotNull
  @Enumerated(EnumType.STRING)
  private PaymentMethod paymentMethod;

  @Pattern(regexp = "\\d{4}")
  private String cardLastFour;

  private String transactionId;
  private LocalDateTime paymentDate;

  @Enumerated(EnumType.STRING)
  private Status status;

  public enum PaymentMethod {
    CREDIT_CARD,
    DEBIT_CARD,
    PAYPAL,
    STRIPE
  }

  public enum Status {
    PENDING,
    SUCCESS,
    FAILED,
    REFUNDED
  }

  @com.fasterxml.jackson.annotation.JsonIgnore
  @AssertTrue(message = "cardLastFour est obligatoire pour un paiement par carte")
  public boolean isCardLastFourValid() {
    return (paymentMethod != PaymentMethod.CREDIT_CARD && paymentMethod != PaymentMethod.DEBIT_CARD)
        || cardLastFour != null;
  }
}
