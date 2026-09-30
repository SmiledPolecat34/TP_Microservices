package com.fitconnect.bookingservice.repository;

import com.fitconnect.bookingservice.client.*;
import com.fitconnect.bookingservice.controller.*;
import com.fitconnect.bookingservice.dto.*;
import com.fitconnect.bookingservice.exception.*;
import com.fitconnect.bookingservice.model.*;
import com.fitconnect.bookingservice.scheduler.*;
import com.fitconnect.bookingservice.service.*;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
  List<Booking> findByUserIdOrderByBookingDateDesc(Long userId);

  List<Booking> findByClassIdAndStatusIn(
      Long classId, java.util.Collection<Booking.Status> statuses);

  List<Booking> findByStatusAndPaymentDeadlineBefore(Booking.Status status, LocalDateTime now);

  List<Booking> findByStatusAndClassDateBetweenAndReminderSentFalse(
      Booking.Status status, LocalDateTime from, LocalDateTime to);
}
