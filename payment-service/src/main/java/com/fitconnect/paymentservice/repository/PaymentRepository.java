package com.fitconnect.paymentservice.repository;

import com.fitconnect.paymentservice.controller.*;
import com.fitconnect.paymentservice.exception.*;
import com.fitconnect.paymentservice.model.*;
import com.fitconnect.paymentservice.service.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
  Optional<Payment> findByBookingId(Long bookingId);

  List<Payment> findByUserIdOrderByPaymentDateDesc(Long userId);
}
