package com.fitconnect.notificationservice.dto;

import com.fitconnect.notificationservice.model.Notification;
import java.time.LocalDateTime;

/** Vue d'une notification renvoyée par l'API. */
public record NotificationResponse(
    Long id,
    Long userId,
    String email,
    Notification.Type type,
    String subject,
    String content,
    LocalDateTime sentDate,
    Notification.Status status) {

  public static NotificationResponse from(Notification n) {
    return new NotificationResponse(
        n.getId(),
        n.getUserId(),
        n.getEmail(),
        n.getType(),
        n.getSubject(),
        n.getContent(),
        n.getSentDate(),
        n.getStatus());
  }
}
