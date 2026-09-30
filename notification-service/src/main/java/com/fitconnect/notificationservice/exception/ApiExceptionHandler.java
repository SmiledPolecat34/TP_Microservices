package com.fitconnect.notificationservice.exception;

import com.fitconnect.notificationservice.controller.*;
import com.fitconnect.notificationservice.model.*;
import com.fitconnect.notificationservice.repository.*;
import com.fitconnect.notificationservice.service.*;
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
}
