package com.fitconnect.bookingservice.exception;

import com.fitconnect.bookingservice.client.*;
import com.fitconnect.bookingservice.controller.*;
import com.fitconnect.bookingservice.dto.*;
import com.fitconnect.bookingservice.model.*;
import com.fitconnect.bookingservice.repository.*;
import com.fitconnect.bookingservice.scheduler.*;
import com.fitconnect.bookingservice.service.*;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class ApiExceptionHandler {
  private ResponseEntity<?> e(HttpStatus s, Exception e) {
    return ResponseEntity.status(s)
        .body(Map.of("timestamp", Instant.now(), "status", s.value(), "message", e.getMessage()));
  }

  @ExceptionHandler(EntityNotFoundException.class)
  ResponseEntity<?> nf(Exception e) {
    return e(HttpStatus.NOT_FOUND, e);
  }

  @ExceptionHandler(IllegalStateException.class)
  ResponseEntity<?> conflict(Exception e) {
    return e(HttpStatus.CONFLICT, e);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  ResponseEntity<?> bad(Exception e) {
    return e(HttpStatus.BAD_REQUEST, e);
  }

  @ExceptionHandler(DownstreamUnavailableException.class)
  ResponseEntity<?> down(Exception e) {
    return e(HttpStatus.SERVICE_UNAVAILABLE, e);
  }
}
