package com.fitconnect.bookingservice.client;

import com.fitconnect.bookingservice.controller.*;
import com.fitconnect.bookingservice.dto.*;
import com.fitconnect.bookingservice.exception.*;
import com.fitconnect.bookingservice.model.*;
import com.fitconnect.bookingservice.repository.*;
import com.fitconnect.bookingservice.scheduler.*;
import com.fitconnect.bookingservice.service.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "payment-service", fallbackFactory = PaymentClientFallback.class)
public interface PaymentClient {
  @PostMapping("/api/payments")
  Contracts.PaymentView pay(@RequestBody Contracts.PaymentRequest request);

  @GetMapping("/api/payments/booking/{bookingId}")
  Contracts.PaymentView byBooking(@PathVariable Long bookingId);

  @PostMapping("/api/payments/{id}/refund")
  Contracts.PaymentView refund(@PathVariable Long id);
}
