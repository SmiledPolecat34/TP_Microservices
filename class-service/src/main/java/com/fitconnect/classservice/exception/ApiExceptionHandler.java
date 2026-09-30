package com.fitconnect.classservice.exception;

import com.fitconnect.classservice.client.*;
import com.fitconnect.classservice.controller.*;
import com.fitconnect.classservice.model.*;
import com.fitconnect.classservice.repository.*;
import com.fitconnect.classservice.service.*;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.*;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler(EntityNotFoundException.class)
  ResponseEntity<?> notFound(Exception e) {
    return error(HttpStatus.NOT_FOUND, e);
  }

  @ExceptionHandler({
    NoSpotsAvailableException.class,
    OptimisticLockingFailureException.class,
    IllegalStateException.class
  })
  ResponseEntity<?> conflict(Exception e) {
    return error(HttpStatus.CONFLICT, e);
  }

  @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class})
  ResponseEntity<?> bad(Exception e) {
    return error(HttpStatus.BAD_REQUEST, e);
  }

  private ResponseEntity<?> error(HttpStatus s, Exception e) {
    return ResponseEntity.status(s)
        .body(
            Map.of(
                "timestamp",
                Instant.now(),
                "status",
                s.value(),
                "error",
                s.getReasonPhrase(),
                "message",
                e.getMessage() == null ? s.getReasonPhrase() : e.getMessage()));
  }
}
