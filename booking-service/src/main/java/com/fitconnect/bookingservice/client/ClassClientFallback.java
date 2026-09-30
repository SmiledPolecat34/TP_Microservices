package com.fitconnect.bookingservice.client;

import com.fitconnect.bookingservice.controller.*;
import com.fitconnect.bookingservice.dto.*;
import com.fitconnect.bookingservice.exception.*;
import com.fitconnect.bookingservice.model.*;
import com.fitconnect.bookingservice.repository.*;
import com.fitconnect.bookingservice.scheduler.*;
import com.fitconnect.bookingservice.service.*;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class ClassClientFallback implements FallbackFactory<ClassClient> {
  public ClassClient create(Throwable cause) {
    RuntimeException e = DownstreamUnavailableException.from("class-service", cause);
    return new ClassClient() {
      public Contracts.ClassView get(Long id) {
        throw e;
      }

      public Contracts.ClassView increment(Long id, int spots) {
        throw e;
      }

      public Contracts.ClassView decrement(Long id, int spots) {
        throw e;
      }
    };
  }
}
