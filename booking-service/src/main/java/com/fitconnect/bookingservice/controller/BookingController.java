package com.fitconnect.bookingservice.controller;

import com.fitconnect.bookingservice.dto.BookingResponse;
import com.fitconnect.bookingservice.dto.Contracts;
import com.fitconnect.bookingservice.model.Booking;
import com.fitconnect.bookingservice.service.BookingService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {
  private final BookingService service;

  @GetMapping
  public List<BookingResponse> all() {
    return views(service.all());
  }

  @GetMapping("/{id}")
  public BookingResponse get(@PathVariable Long id) {
    return BookingResponse.from(service.get(id));
  }

  @GetMapping("/user/{id}")
  public List<BookingResponse> byUser(@PathVariable Long id) {
    return views(service.byUser(id));
  }

  @PostMapping
  public ResponseEntity<BookingResponse> create(
      @Valid @RequestBody Contracts.CreateBooking request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(BookingResponse.from(service.create(request)));
  }

  @PatchMapping("/{id}/confirm")
  public BookingResponse confirm(
      @PathVariable Long id, @Valid @RequestBody Contracts.ConfirmBooking request) {
    return BookingResponse.from(service.confirm(id, request));
  }

  @PatchMapping("/{id}/cancel")
  public BookingResponse cancel(@PathVariable Long id) {
    return BookingResponse.from(service.cancel(id));
  }

  @PatchMapping("/{id}/complete")
  public BookingResponse complete(@PathVariable Long id) {
    return BookingResponse.from(service.complete(id));
  }

  @PatchMapping("/class/{classId}/cancel")
  public List<BookingResponse> cancelForClass(@PathVariable Long classId) {
    return views(service.cancelForClass(classId));
  }

  @GetMapping("/expired")
  public List<BookingResponse> expired() {
    return views(service.expired());
  }

  private List<BookingResponse> views(List<Booking> bookings) {
    return bookings.stream().map(BookingResponse::from).toList();
  }
}
