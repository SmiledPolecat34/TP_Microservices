package com.fitconnect.bookingservice;
import org.springframework.cloud.openfeign.FeignClient; import org.springframework.web.bind.annotation.*;
@FeignClient(name="class-service",fallbackFactory=ClassClientFallback.class) public interface ClassClient { @GetMapping("/api/classes/{id}") Contracts.ClassView get(@PathVariable Long id); @PatchMapping("/api/classes/{id}/increment") Contracts.ClassView increment(@PathVariable Long id,@RequestParam int spots); @PatchMapping("/api/classes/{id}/decrement") Contracts.ClassView decrement(@PathVariable Long id,@RequestParam int spots); }
