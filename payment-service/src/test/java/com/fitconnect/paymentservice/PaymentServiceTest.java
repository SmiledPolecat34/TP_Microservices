package com.fitconnect.paymentservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fitconnect.paymentservice.controller.*;
import com.fitconnect.paymentservice.exception.*;
import com.fitconnect.paymentservice.model.*;
import com.fitconnect.paymentservice.repository.*;
import com.fitconnect.paymentservice.service.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PaymentServiceTest {
  @Test
  void acceptsUnder100AndRefuses100() {
    PaymentRepository r = mock(PaymentRepository.class);
    when(r.save(any())).thenAnswer(i -> i.getArgument(0));
    PaymentService s = new PaymentService(r);
    Payment low = Payment.builder().amount(new BigDecimal("99.99")).build();
    Payment high = Payment.builder().amount(new BigDecimal("100")).build();
    assertEquals(Payment.Status.SUCCESS, s.process(low).getStatus());
    assertEquals(Payment.Status.FAILED, s.process(high).getStatus());
  }
}
