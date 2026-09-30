package com.fitconnect.bookingservice.client;

import com.fitconnect.bookingservice.controller.*;
import com.fitconnect.bookingservice.dto.*;
import com.fitconnect.bookingservice.exception.*;
import com.fitconnect.bookingservice.model.*;
import com.fitconnect.bookingservice.repository.*;
import com.fitconnect.bookingservice.scheduler.*;
import com.fitconnect.bookingservice.service.*;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class NotificationClientFallback implements NotificationClient {
  public Map<String, Object> send(Contracts.NotificationRequest request) {
    return Map.of("status", "FAILED", "message", "notification-service indisponible");
  }
}
