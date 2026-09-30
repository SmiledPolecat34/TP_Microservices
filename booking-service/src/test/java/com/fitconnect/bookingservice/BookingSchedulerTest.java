package com.fitconnect.bookingservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fitconnect.bookingservice.client.*;
import com.fitconnect.bookingservice.controller.*;
import com.fitconnect.bookingservice.dto.*;
import com.fitconnect.bookingservice.exception.*;
import com.fitconnect.bookingservice.model.*;
import com.fitconnect.bookingservice.repository.*;
import com.fitconnect.bookingservice.scheduler.*;
import com.fitconnect.bookingservice.service.*;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.Test;

class BookingSchedulerTest {
  @Test
  void shouldCancelExpiredBookings() {
    BookingRepository repo = mock(BookingRepository.class);
    ClassClient classes = mock(ClassClient.class);
    NotificationClient notifications = mock(NotificationClient.class);
    Booking b =
        Booking.builder()
            .id(1L)
            .userId(1L)
            .userEmail("a@b.fr")
            .classId(2L)
            .numberOfSpots(2)
            .bookingReference("BK-X")
            .status(Booking.Status.PENDING_PAYMENT)
            .paymentDeadline(LocalDateTime.now().minusMinutes(1))
            .build();
    when(repo.findByStatusAndPaymentDeadlineBefore(eq(Booking.Status.PENDING_PAYMENT), any()))
        .thenReturn(List.of(b));
    new BookingScheduler(repo, classes, notifications).expirePayments();
    assertEquals(Booking.Status.CANCELLED, b.getStatus());
    verify(classes).decrement(2L, 2);
  }
}
