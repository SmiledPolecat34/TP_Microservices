package com.fitconnect.bookingservice;
import org.springframework.cloud.openfeign.FeignClient; import org.springframework.web.bind.annotation.*; import java.util.Map;
@FeignClient(name="notification-service",fallback=NotificationClientFallback.class) public interface NotificationClient {@PostMapping("/api/notifications") Map<String,Object> send(@RequestBody Contracts.NotificationRequest request);}
