package com.fitconnect.paymentservice;
import jakarta.persistence.EntityNotFoundException; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.util.Map;
@RestControllerAdvice public class ApiExceptionHandler { @ExceptionHandler(EntityNotFoundException.class) ResponseEntity<?> nf(Exception e){return ResponseEntity.status(404).body(Map.of("message",e.getMessage()));} @ExceptionHandler(IllegalStateException.class) ResponseEntity<?> conflict(Exception e){return ResponseEntity.status(409).body(Map.of("message",e.getMessage()));} }
