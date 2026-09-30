package com.fitconnect.classservice.controller;

import com.fitconnect.classservice.client.BookingClient;
import com.fitconnect.classservice.dto.FitnessClassRequest;
import com.fitconnect.classservice.dto.FitnessClassResponse;
import com.fitconnect.classservice.model.FitnessClass;
import com.fitconnect.classservice.service.FitnessClassService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class FitnessClassController {
  private final FitnessClassService service;
  private final BookingClient bookings;

  @GetMapping
  public Page<FitnessClassResponse> all(
      @RequestParam(required = false) FitnessClass.Category category,
      @RequestParam(required = false) FitnessClass.Level level,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate dateFrom,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate dateTo,
      @RequestParam(required = false) String location,
      @RequestParam(required = false) String instructor,
      Pageable pageable) {
    return service
        .search(category, level, dateFrom, dateTo, location, instructor, pageable)
        .map(FitnessClassResponse::from);
  }

  @GetMapping("/search")
  public Page<FitnessClassResponse> search(
      @RequestParam(required = false) FitnessClass.Category category,
      @RequestParam(required = false) FitnessClass.Level level,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate dateFrom,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate dateTo,
      @RequestParam(required = false) String location,
      @RequestParam(required = false) String instructor,
      Pageable pageable) {
    return all(category, level, dateFrom, dateTo, location, instructor, pageable);
  }

  @GetMapping("/{id}")
  public FitnessClassResponse get(@PathVariable Long id) {
    return FitnessClassResponse.from(service.get(id));
  }

  @PostMapping
  public ResponseEntity<FitnessClassResponse> create(
      @Valid @RequestBody FitnessClassRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(FitnessClassResponse.from(service.create(request)));
  }

  @PutMapping("/{id}")
  public FitnessClassResponse update(
      @PathVariable Long id, @Valid @RequestBody FitnessClassRequest request) {
    return FitnessClassResponse.from(service.update(id, request));
  }

  @DeleteMapping("/{id}")
  public FitnessClassResponse cancel(@PathVariable Long id) {
    FitnessClass cancelled = service.cancel(id);
    bookings.cancelForClass(id);
    return FitnessClassResponse.from(cancelled);
  }

  @PatchMapping("/{id}/increment")
  public FitnessClassResponse increment(@PathVariable Long id, @RequestParam int spots) {
    return FitnessClassResponse.from(retryOnConflict(() -> service.increment(id, spots)));
  }

  @PatchMapping("/{id}/decrement")
  public FitnessClassResponse decrement(@PathVariable Long id, @RequestParam int spots) {
    return FitnessClassResponse.from(retryOnConflict(() -> service.decrement(id, spots)));
  }

  // Un conflit de version ne signifie pas que le cours est complet : on relit et on réessaie avant
  // de répondre 409.
  private FitnessClass retryOnConflict(Supplier<FitnessClass> action) {
    for (int attempt = 1; ; attempt++) {
      try {
        return action.get();
      } catch (OptimisticLockingFailureException e) {
        if (attempt >= 5) throw e;
      }
    }
  }
}
