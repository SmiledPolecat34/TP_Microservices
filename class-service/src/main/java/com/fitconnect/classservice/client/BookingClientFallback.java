package com.fitconnect.classservice.client;

import com.fitconnect.classservice.controller.*;
import com.fitconnect.classservice.exception.*;
import com.fitconnect.classservice.model.*;
import com.fitconnect.classservice.repository.*;
import com.fitconnect.classservice.service.*;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BookingClientFallback implements FallbackFactory<BookingClient> {
  @Override
  public BookingClient create(Throwable cause) {
    // Le cours reste annulé même si booking-service est injoignable : les inscrits ne sont pas
    // prévenus.
    return classId -> {
      log.warn("Cours {} annulé mais réservations non annulées: {}", classId, cause.toString());
      return List.of();
    };
  }
}
