package com.fitconnect.notificationservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fitconnect.notificationservice.controller.*;
import com.fitconnect.notificationservice.exception.*;
import com.fitconnect.notificationservice.model.*;
import com.fitconnect.notificationservice.repository.*;
import com.fitconnect.notificationservice.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {"spring.cloud.config.enabled=false", "eureka.client.enabled=false"})
@AutoConfigureMockMvc
class NotificationIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired NotificationRepository repository;

  private static final String BODY =
      "{\"userId\":7,\"email\":\"john@example.com\",\"type\":\"BOOKING_CONFIRMATION\",\"subject\":\"Réservation\",\"content\":\"Votre réservation est en attente de paiement\"}";

  @Test
  void shouldSendNotificationAndKeepHistory() throws Exception {
    mvc.perform(post("/api/notifications").contentType(MediaType.APPLICATION_JSON).content(BODY))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value("SENT"))
        .andExpect(jsonPath("$.sentDate").isNotEmpty());
    mvc.perform(get("/api/notifications/user/7"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].type").value("BOOKING_CONFIRMATION"))
        .andExpect(jsonPath("$[0].email").value("john@example.com"));
  }

  @Test
  void shouldRejectInvalidNotification() throws Exception {
    mvc.perform(
            post("/api/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(BODY.replace("BOOKING_CONFIRMATION", "UNKNOWN")))
        .andExpect(status().isBadRequest());
    mvc.perform(
            post("/api/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(BODY.replace("\"subject\":\"Réservation\"", "\"subject\":\"\"")))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldListPendingAndRetry() throws Exception {
    Notification pending =
        repository.save(
            Notification.builder()
                .userId(8L)
                .email("jane@example.com")
                .type(Notification.Type.BOOKING_REMINDER)
                .subject("Rappel")
                .content("Votre cours commence dans 24 heures")
                .status(Notification.Status.PENDING)
                .build());
    mvc.perform(get("/api/notifications/pending"))
        .andExpect(jsonPath("$[?(@.id==" + pending.getId() + ")]").exists());
    mvc.perform(patch("/api/notifications/{id}/retry", pending.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("SENT"));
    assertEquals(
        Notification.Status.SENT, repository.findById(pending.getId()).orElseThrow().getStatus());
    mvc.perform(patch("/api/notifications/{id}/retry", 999999)).andExpect(status().isNotFound());
  }
}
