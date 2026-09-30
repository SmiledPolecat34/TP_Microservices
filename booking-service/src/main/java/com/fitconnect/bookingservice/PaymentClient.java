package com.fitconnect.bookingservice;
import org.springframework.cloud.openfeign.FeignClient; import org.springframework.web.bind.annotation.*;
@FeignClient(name="payment-service",fallbackFactory=PaymentClientFallback.class) public interface PaymentClient {@PostMapping("/api/payments") Contracts.PaymentView pay(@RequestBody Contracts.PaymentRequest request);@GetMapping("/api/payments/booking/{bookingId}") Contracts.PaymentView byBooking(@PathVariable Long bookingId);@PostMapping("/api/payments/{id}/refund") Contracts.PaymentView refund(@PathVariable Long id);}
