package com.fitconnect.notificationservice;
import jakarta.validation.Valid; import lombok.RequiredArgsConstructor; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.util.List;
@RestController @RequestMapping("/api/notifications") @RequiredArgsConstructor public class NotificationController {private final NotificationService service;
 @PostMapping public ResponseEntity<Notification> send(@Valid @RequestBody Notification n){return ResponseEntity.status(201).body(service.send(n));}
 @GetMapping("/user/{userId}") public List<Notification> byUser(@PathVariable Long userId){return service.byUser(userId);}
 @GetMapping("/pending") public List<Notification> pending(){return service.pending();}
 @PatchMapping("/{id}/retry") public Notification retry(@PathVariable Long id){return service.retry(id);}
}
