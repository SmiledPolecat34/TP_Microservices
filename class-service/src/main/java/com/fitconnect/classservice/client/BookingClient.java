package com.fitconnect.classservice.client;

import com.fitconnect.classservice.controller.*;
import com.fitconnect.classservice.exception.*;
import com.fitconnect.classservice.model.*;
import com.fitconnect.classservice.repository.*;
import com.fitconnect.classservice.service.*;
import java.util.List;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "booking-service", fallbackFactory = BookingClientFallback.class)
public interface BookingClient {
  /**
   * Annule les réservations actives d'un cours annulé et notifie les inscrits (CLASS_CANCELLED).
   */
  @PatchMapping("/api/bookings/class/{classId}/cancel")
  List<Map<String, Object>> cancelForClass(@PathVariable Long classId);
}
