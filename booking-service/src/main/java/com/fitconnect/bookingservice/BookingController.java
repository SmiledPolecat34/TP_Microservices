package com.fitconnect.bookingservice;
import jakarta.validation.Valid; import lombok.RequiredArgsConstructor; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.util.List;
@RestController @RequestMapping("/api/bookings") @RequiredArgsConstructor public class BookingController {private final BookingService service;
 @GetMapping public List<Booking> all(){return service.all();}@GetMapping("/{id}") public Booking get(@PathVariable Long id){return service.get(id);}@GetMapping("/user/{id}") public List<Booking> byUser(@PathVariable Long id){return service.byUser(id);}
 @PostMapping public ResponseEntity<Booking> create(@Valid @RequestBody Contracts.CreateBooking r){return ResponseEntity.status(201).body(service.create(r));}
 @PatchMapping("/{id}/confirm") public Booking confirm(@PathVariable Long id,@Valid @RequestBody Contracts.ConfirmBooking r){return service.confirm(id,r);}
 @PatchMapping("/{id}/cancel") public Booking cancel(@PathVariable Long id){return service.cancel(id);}@PatchMapping("/{id}/complete") public Booking complete(@PathVariable Long id){return service.complete(id);}@GetMapping("/expired") public List<Booking> expired(){return service.expired();}
}
