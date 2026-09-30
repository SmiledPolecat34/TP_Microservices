package com.fitconnect.notificationservice.dto;

import com.fitconnect.notificationservice.model.Notification;
import jakarta.validation.constraints.*;

/**
 * Demande d'envoi émise par les autres services. Date d'envoi et statut sont fixés par le service.
 */
public record NotificationRequest(
    @NotNull Long userId,
    @Email String email,
    @NotNull Notification.Type type,
    @NotBlank String subject,
    @NotBlank String content) {

  public Notification toEntity() {
    return Notification.builder()
        .userId(userId)
        .email(email)
        .type(type)
        .subject(subject)
        .content(content)
        .build();
  }
}
