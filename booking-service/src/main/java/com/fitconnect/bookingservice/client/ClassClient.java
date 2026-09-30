package com.fitconnect.bookingservice.client;

import com.fitconnect.bookingservice.controller.*;
import com.fitconnect.bookingservice.dto.*;
import com.fitconnect.bookingservice.exception.*;
import com.fitconnect.bookingservice.model.*;
import com.fitconnect.bookingservice.repository.*;
import com.fitconnect.bookingservice.scheduler.*;
import com.fitconnect.bookingservice.service.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "class-service", fallbackFactory = ClassClientFallback.class)
public interface ClassClient {
  @GetMapping("/api/classes/{id}")
  Contracts.ClassView get(@PathVariable Long id);

  @PatchMapping("/api/classes/{id}/increment")
  Contracts.ClassView increment(@PathVariable Long id, @RequestParam int spots);

  @PatchMapping("/api/classes/{id}/decrement")
  Contracts.ClassView decrement(@PathVariable Long id, @RequestParam int spots);
}
