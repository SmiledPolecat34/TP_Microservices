package com.fitconnect.bookingservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(properties={"spring.cloud.config.enabled=false","eureka.client.enabled=false","spring.task.scheduling.enabled=false"})
@Transactional
class BookingFlowIntegrationTest {
  @Autowired BookingService service;
  @Autowired BookingRepository repository;
  @Autowired BookingScheduler scheduler;
  @MockBean ClassClient classes;
  @MockBean PaymentClient payments;
  @MockBean NotificationClient notifications;

  @Test void shouldCompleteFullBookingFlow(){
    Contracts.ClassView fitnessClass=new Contracts.ClassView(101L,"Yoga Flow","Marie","Paris","YOGA","INTERMEDIATE",10,5,new BigDecimal("20.00"),LocalDateTime.now().plusDays(4),"SCHEDULED");
    when(classes.get(101L)).thenReturn(fitnessClass);
    when(notifications.send(any())).thenReturn(Map.of("status","SENT"));
    when(payments.pay(any())).thenReturn(new Contracts.PaymentView(10L,"PAY-TEST",1L,"SUCCESS"));
    Booking created=service.create(new Contracts.CreateBooking(1L,"john@example.com","John Doe",101L,2));
    Booking confirmed=service.confirm(created.getId(),new Contracts.ConfirmBooking(Contracts.PaymentMethod.CREDIT_CARD,"1234","txn_test"));
    assertEquals(Booking.Status.CONFIRMED,repository.findById(confirmed.getId()).orElseThrow().getStatus());
    verify(classes).increment(101L,2);
    verify(notifications,times(2)).send(any());
  }

  @Test void shouldCancelExpiredBookings(){
    when(notifications.send(any())).thenReturn(Map.of("status","SENT"));
    Booking expired=repository.save(Booking.builder().bookingReference("BK-EXPIR").userId(1L).userEmail("john@example.com").userName("John Doe").classId(101L).numberOfSpots(2).status(Booking.Status.PENDING_PAYMENT).bookingDate(LocalDateTime.now().minusHours(2)).paymentDeadline(LocalDateTime.now().minusHours(1)).build());
    scheduler.expirePayments();
    assertEquals(Booking.Status.CANCELLED,repository.findById(expired.getId()).orElseThrow().getStatus());
    verify(classes).decrement(101L,2);
    verify(notifications).send(argThat(n->"BOOKING_CANCELLED".equals(n.type())));
  }
}
