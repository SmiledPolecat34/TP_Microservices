package com.fitconnect.paymentservice;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(unique=true,nullable=false) private String paymentReference;
 @NotNull private Long bookingId;
 private String bookingReference;
 @NotNull private Long userId;
 @NotNull @DecimalMin("0.00") private BigDecimal amount;
 @NotNull @Enumerated(EnumType.STRING) private PaymentMethod paymentMethod;
 @Pattern(regexp="\\d{4}") private String cardLastFour;
 private String transactionId;
 private LocalDateTime paymentDate;
 @Enumerated(EnumType.STRING) private Status status;
 public enum PaymentMethod { CREDIT_CARD,DEBIT_CARD,PAYPAL,STRIPE }
 public enum Status { PENDING,SUCCESS,FAILED,REFUNDED }
}
