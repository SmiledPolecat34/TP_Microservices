package com.fitconnect.paymentservice.exception;

import com.fitconnect.paymentservice.controller.*;
import com.fitconnect.paymentservice.model.*;
import com.fitconnect.paymentservice.repository.*;
import com.fitconnect.paymentservice.service.*;
import jakarta.persistence.EntityNotFoundException;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler(EntityNotFoundException.class)
  ResponseEntity<?> nf(Exception e) {
    return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
  }

  @ExceptionHandler(IllegalStateException.class)
  ResponseEntity<?> conflict(Exception e) {
    return ResponseEntity.status(409).body(Map.of("message", e.getMessage()));
  }
}
