package com.fitconnect.paymentservice.controller;

import com.fitconnect.paymentservice.dto.PaymentRequest;
import com.fitconnect.paymentservice.dto.PaymentResponse;
import com.fitconnect.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
  private final PaymentService service;

  @PostMapping
  public ResponseEntity<PaymentResponse> process(@Valid @RequestBody PaymentRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(PaymentResponse.from(service.process(request.toEntity())));
  }

  @GetMapping("/booking/{bookingId}")
  public PaymentResponse byBooking(@PathVariable Long bookingId) {
    return PaymentResponse.from(service.byBooking(bookingId));
  }

  @PostMapping("/{id}/refund")
  public PaymentResponse refund(@PathVariable Long id) {
    return PaymentResponse.from(service.refund(id));
  }

  @GetMapping("/user/{userId}")
  public List<PaymentResponse> byUser(@PathVariable Long userId) {
    return service.byUser(userId).stream().map(PaymentResponse::from).toList();
  }
}
