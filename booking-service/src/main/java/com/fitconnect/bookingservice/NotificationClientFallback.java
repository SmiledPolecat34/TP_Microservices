package com.fitconnect.bookingservice;
import org.springframework.stereotype.Component; import java.util.Map;
@Component public class NotificationClientFallback implements NotificationClient {public Map<String,Object> send(Contracts.NotificationRequest request){return Map.of("status","FAILED","message","notification-service indisponible");}}
