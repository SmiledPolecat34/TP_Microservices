package com.fitconnect.bookingservice;
import org.springframework.data.jpa.repository.JpaRepository; import java.time.LocalDateTime; import java.util.List;
public interface BookingRepository extends JpaRepository<Booking,Long>{List<Booking> findByUserIdOrderByBookingDateDesc(Long userId);List<Booking> findByStatusAndPaymentDeadlineBefore(Booking.Status status,LocalDateTime now);List<Booking> findByStatusAndClassDateBetweenAndReminderSentFalse(Booking.Status status,LocalDateTime from,LocalDateTime to);}
