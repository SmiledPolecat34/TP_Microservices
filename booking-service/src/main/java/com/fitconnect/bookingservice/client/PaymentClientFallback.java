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
public class PaymentClientFallback implements FallbackFactory<PaymentClient> {
  public PaymentClient create(Throwable cause) {
    RuntimeException e = DownstreamUnavailableException.from("payment-service", cause);
    return new PaymentClient() {
      public Contracts.PaymentView pay(Contracts.PaymentRequest r) {
        throw e;
      }

      public Contracts.PaymentView byBooking(Long id) {
        throw e;
      }

      public Contracts.PaymentView refund(Long id) {
        throw e;
      }
    };
  }
}
