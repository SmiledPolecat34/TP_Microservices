package com.fitconnect.paymentservice;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/payments") @RequiredArgsConstructor public class PaymentController {
 private final PaymentService service;
 @PostMapping public ResponseEntity<Payment> process(@Valid @RequestBody Payment p){return ResponseEntity.status(HttpStatus.CREATED).body(service.process(p));}
 @GetMapping("/booking/{bookingId}") public Payment byBooking(@PathVariable Long bookingId){return service.byBooking(bookingId);}
 @PostMapping("/{id}/refund") public Payment refund(@PathVariable Long id){return service.refund(id);}
 @GetMapping("/user/{userId}") public List<Payment> byUser(@PathVariable Long userId){return service.byUser(userId);}
}
