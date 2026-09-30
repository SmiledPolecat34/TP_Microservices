package com.fitconnect.bookingservice;

import static org.junit.jupiter.api.Assertions.*;

import com.fitconnect.bookingservice.client.*;
import com.fitconnect.bookingservice.controller.*;
import com.fitconnect.bookingservice.dto.*;
import com.fitconnect.bookingservice.exception.*;
import com.fitconnect.bookingservice.model.*;
import com.fitconnect.bookingservice.repository.*;
import com.fitconnect.bookingservice.scheduler.*;
import com.fitconnect.bookingservice.service.*;
import com.fitconnect.classservice.ClassServiceApplication;
import com.fitconnect.notificationservice.NotificationServiceApplication;
import com.fitconnect.paymentservice.PaymentServiceApplication;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;

/**
 * Test d'intégration interservices : class-service, payment-service et notification-service sont
 * réellement démarrés (serveur HTTP + base H2 chacun) et booking-service les appelle via ses
 * clients Feign, sans aucun mock.
 */
@SpringBootTest(
    properties = {
      "spring.cloud.config.enabled=false",
      "eureka.client.enabled=false",
      "spring.datasource.url=jdbc:h2:mem:booking-interservice;DB_CLOSE_DELAY=-1"
    })
class InterServiceIntegrationTest {
  static final ConfigurableApplicationContext CLASSES =
      start(ClassServiceApplication.class, "class-it");
  static final ConfigurableApplicationContext PAYMENTS =
      start(PaymentServiceApplication.class, "payment-it");
  static final ConfigurableApplicationContext NOTIFICATIONS =
      start(NotificationServiceApplication.class, "notification-it");

  @Autowired BookingService service;
  @Autowired BookingRepository repository;
  @Autowired BookingScheduler scheduler;
  final RestClient http = RestClient.create();

  static ConfigurableApplicationContext start(Class<?> application, String database) {
    return new SpringApplicationBuilder(application)
        .run(
            "--server.port=0",
            "--spring.cloud.config.enabled=false",
            "--eureka.client.enabled=false",
            "--spring.datasource.url=jdbc:h2:mem:" + database + ";DB_CLOSE_DELAY=-1",
            "--spring.jpa.hibernate.ddl-auto=create-drop");
  }

  static String url(ConfigurableApplicationContext context) {
    return "http://localhost:"
        + ((ServletWebServerApplicationContext) context).getWebServer().getPort();
  }

  /** Remplace Eureka : les clients Feign résolvent les trois services démarrés ci-dessus. */
  @DynamicPropertySource
  static void discovery(DynamicPropertyRegistry registry) {
    registry.add(
        "spring.cloud.discovery.client.simple.instances.class-service[0].uri", () -> url(CLASSES));
    registry.add(
        "spring.cloud.discovery.client.simple.instances.payment-service[0].uri",
        () -> url(PAYMENTS));
    registry.add(
        "spring.cloud.discovery.client.simple.instances.notification-service[0].uri",
        () -> url(NOTIFICATIONS));
  }

  @AfterAll
  static void stop() {
    CLASSES.close();
    PAYMENTS.close();
    NOTIFICATIONS.close();
  }

  @Test
  void shouldCompleteFullBookingFlow() {
    // 1. Create class
    long classId = createClass(10);
    // 2. Create booking
    Booking created =
        service.create(
            new Contracts.CreateBooking(41L, "john@example.com", "John Doe", classId, 2));
    assertEquals(Booking.Status.PENDING_PAYMENT, created.getStatus());
    assertEquals(2, participants(classId));
    // 3. Confirm payment
    service.confirm(
        created.getId(),
        new Contracts.ConfirmBooking(Contracts.PaymentMethod.CREDIT_CARD, "1234", "txn_it"));
    // 4. Verify booking status = CONFIRMED
    assertEquals(
        Booking.Status.CONFIRMED, repository.findById(created.getId()).orElseThrow().getStatus());
    assertEquals("SUCCESS", payment(created.getId()).get("status"));
    // 5. Verify spots decreased
    assertEquals(2, participants(classId));
    // 6. Verify notification sent
    assertEquals(List.of("BOOKING_CONFIRMATION", "PAYMENT_CONFIRMATION"), notificationTypes(41L));

    // Annulation dans les délais : remboursement et restitution des places
    service.cancel(created.getId());
    assertEquals(
        Booking.Status.CANCELLED, repository.findById(created.getId()).orElseThrow().getStatus());
    assertEquals("REFUNDED", payment(created.getId()).get("status"));
    assertEquals(0, participants(classId));
    assertTrue(notificationTypes(41L).contains("BOOKING_CANCELLED"));
  }

  @Test
  void shouldRejectBooking_whenClassIsFull() {
    long classId = createClass(5);
    service.create(new Contracts.CreateBooking(42L, "jane@example.com", "Jane Doe", classId, 4));
    IllegalStateException refused =
        assertThrows(
            IllegalStateException.class,
            () ->
                service.create(
                    new Contracts.CreateBooking(43L, "bob@example.com", "Bob Martin", classId, 2)));
    assertEquals("Plus de places disponibles pour ce cours", refused.getMessage());
    assertEquals(4, participants(classId));
  }

  @Test
  void shouldCancelExpiredBookings() {
    // 1. Create booking with paymentDeadline in past
    long classId = createClass(10);
    Booking booking =
        service.create(
            new Contracts.CreateBooking(44L, "late@example.com", "Late Payer", classId, 3));
    assertEquals(3, participants(classId));
    booking.setPaymentDeadline(LocalDateTime.now().minusMinutes(5));
    repository.save(booking);
    // 2. Run scheduler
    scheduler.expirePayments();
    // 3. Verify status = CANCELLED
    assertEquals(
        Booking.Status.CANCELLED, repository.findById(booking.getId()).orElseThrow().getStatus());
    // 4. Verify spots restored
    assertEquals(0, participants(classId));
    assertTrue(notificationTypes(44L).contains("BOOKING_CANCELLED"));
  }

  @Test
  void shouldCancelAndRefundBookings_whenClassIsCancelled() {
    long classId = createClass(10);
    Booking paid =
        service.create(
            new Contracts.CreateBooking(45L, "paid@example.com", "Paid User", classId, 1));
    service.confirm(
        paid.getId(),
        new Contracts.ConfirmBooking(Contracts.PaymentMethod.PAYPAL, null, "txn_class"));
    Booking pending =
        service.create(
            new Contracts.CreateBooking(46L, "pending@example.com", "Pending User", classId, 1));

    assertEquals(2, service.cancelForClass(classId).size());

    assertEquals(
        Booking.Status.CANCELLED, repository.findById(paid.getId()).orElseThrow().getStatus());
    assertEquals(
        Booking.Status.CANCELLED, repository.findById(pending.getId()).orElseThrow().getStatus());
    assertEquals("REFUNDED", payment(paid.getId()).get("status"));
    assertTrue(notificationTypes(45L).contains("CLASS_CANCELLED"));
    assertTrue(notificationTypes(46L).contains("CLASS_CANCELLED"));
  }

  private long createClass(int maxParticipants) {
    Map<String, Object> body =
        Map.of(
            "name", "Yoga Flow",
            "description", "Cours de test interservices",
            "instructor", "Marie",
            "gymLocation", "Paris",
            "category", "YOGA",
            "level", "INTERMEDIATE",
            "durationMinutes", 60,
            "maxParticipants", maxParticipants,
            "price", 20.00,
            "dateTime", LocalDateTime.now().plusDays(5).withNano(0).toString());
    Map<?, ?> created =
        http.post()
            .uri(url(CLASSES) + "/api/classes")
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .retrieve()
            .body(Map.class);
    return ((Number) created.get("id")).longValue();
  }

  private int participants(long classId) {
    Map<?, ?> fitnessClass =
        http.get().uri(url(CLASSES) + "/api/classes/" + classId).retrieve().body(Map.class);
    return ((Number) fitnessClass.get("currentParticipants")).intValue();
  }

  private Map<?, ?> payment(long bookingId) {
    return http.get()
        .uri(url(PAYMENTS) + "/api/payments/booking/" + bookingId)
        .retrieve()
        .body(Map.class);
  }

  private List<String> notificationTypes(long userId) {
    List<?> notifications =
        http.get()
            .uri(url(NOTIFICATIONS) + "/api/notifications/user/" + userId)
            .retrieve()
            .body(List.class);
    return notifications.stream().map(n -> (String) ((Map<?, ?>) n).get("type")).sorted().toList();
  }
}
