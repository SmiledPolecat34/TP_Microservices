package com.fitconnect.bookingservice.model;

import com.fitconnect.bookingservice.client.*;
import com.fitconnect.bookingservice.controller.*;
import com.fitconnect.bookingservice.dto.*;
import com.fitconnect.bookingservice.exception.*;
import com.fitconnect.bookingservice.repository.*;
import com.fitconnect.bookingservice.scheduler.*;
import com.fitconnect.bookingservice.service.*;
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
public class Booking {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String bookingReference;

  @NotNull private Long userId;
  @Email @NotBlank private String userEmail;
  @NotBlank private String userName;
  @NotNull private Long classId;
  private String className;
  private LocalDateTime classDate;
  private String instructor;
  private BigDecimal price;

  @NotNull
  @Min(1)
  @Max(4)
  private Integer numberOfSpots;

  private BigDecimal totalAmount;
  private LocalDateTime bookingDate;

  @Enumerated(EnumType.STRING)
  private Status status;

  private LocalDateTime paymentDeadline;
  private LocalDateTime cancellationDeadline;
  @Builder.Default private boolean reminderSent = false;

  public enum Status {
    PENDING_PAYMENT,
    CONFIRMED,
    CANCELLED,
    COMPLETED,
    NO_SHOW
  }
}
