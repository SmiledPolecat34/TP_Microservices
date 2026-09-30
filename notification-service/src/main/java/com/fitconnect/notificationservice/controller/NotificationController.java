package com.fitconnect.notificationservice.controller;

import com.fitconnect.notificationservice.dto.NotificationRequest;
import com.fitconnect.notificationservice.dto.NotificationResponse;
import com.fitconnect.notificationservice.service.NotificationService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
  private final NotificationService service;

  @PostMapping
  public ResponseEntity<NotificationResponse> send(
      @Valid @RequestBody NotificationRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(NotificationResponse.from(service.send(request.toEntity())));
  }

  @GetMapping("/user/{userId}")
  public List<NotificationResponse> byUser(@PathVariable Long userId) {
    return service.byUser(userId).stream().map(NotificationResponse::from).toList();
  }

  @GetMapping("/pending")
  public List<NotificationResponse> pending() {
    return service.pending().stream().map(NotificationResponse::from).toList();
  }

  @PatchMapping("/{id}/retry")
  public NotificationResponse retry(@PathVariable Long id) {
    return NotificationResponse.from(service.retry(id));
  }
}
