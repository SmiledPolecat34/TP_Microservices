package com.fitconnect.paymentservice;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
@Service @RequiredArgsConstructor public class PaymentService {
 private final PaymentRepository repository;
 @Transactional public Payment process(Payment p){ p.setId(null); p.setPaymentReference("PAY-"+UUID.randomUUID().toString().substring(0,5).toUpperCase()); p.setPaymentDate(LocalDateTime.now()); p.setStatus(p.getAmount().compareTo(new BigDecimal("100"))<0?Payment.Status.SUCCESS:Payment.Status.FAILED); return repository.save(p); }
 public Payment byBooking(Long id){return repository.findByBookingId(id).orElseThrow(()->new EntityNotFoundException("Paiement introuvable pour la réservation "+id));}
 public List<Payment> byUser(Long id){return repository.findByUserIdOrderByPaymentDateDesc(id);}
 @Transactional public Payment refund(Long id){Payment p=repository.findById(id).orElseThrow(()->new EntityNotFoundException("Paiement introuvable: "+id)); if(p.getStatus()!=Payment.Status.SUCCESS)throw new IllegalStateException("Seul un paiement réussi peut être remboursé"); p.setStatus(Payment.Status.REFUNDED); return p;}
}
