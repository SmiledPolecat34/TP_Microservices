package com.fitconnect.paymentservice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface PaymentRepository extends JpaRepository<Payment,Long>{ Optional<Payment> findByBookingId(Long bookingId); List<Payment> findByUserIdOrderByPaymentDateDesc(Long userId); }
