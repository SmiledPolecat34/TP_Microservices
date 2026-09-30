package com.fitconnect.notificationservice;
import jakarta.persistence.*; import jakarta.validation.constraints.*; import lombok.*; import java.time.LocalDateTime;
@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder public class Notification {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @NotNull private Long userId; @Email private String email;
 @NotNull @Enumerated(EnumType.STRING) private Type type;
 @NotBlank private String subject; @NotBlank @Column(length=3000) private String content;
 private LocalDateTime sentDate; @Enumerated(EnumType.STRING) private Status status;
 public enum Type { BOOKING_CONFIRMATION,PAYMENT_CONFIRMATION,BOOKING_REMINDER,BOOKING_CANCELLED,CLASS_CANCELLED }
 public enum Status { PENDING,SENT,FAILED }
}
