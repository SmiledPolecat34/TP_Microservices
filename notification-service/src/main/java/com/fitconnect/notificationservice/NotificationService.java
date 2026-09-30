package com.fitconnect.notificationservice;
import jakarta.persistence.EntityNotFoundException; import lombok.RequiredArgsConstructor; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.time.LocalDateTime; import java.util.List;
@Service @RequiredArgsConstructor public class NotificationService { private final NotificationRepository repository;
 @Transactional public Notification send(Notification n){n.setId(null);n.setStatus(Notification.Status.PENDING);n=repository.save(n);return deliver(n);}
 @Transactional public Notification retry(Long id){return deliver(repository.findById(id).orElseThrow(()->new EntityNotFoundException("Notification introuvable: "+id)));}
 private Notification deliver(Notification n){try{System.out.printf("NOTIFICATION [%s] vers %s: %s%n",n.getType(),n.getEmail(),n.getContent());n.setSentDate(LocalDateTime.now());n.setStatus(Notification.Status.SENT);}catch(RuntimeException e){n.setStatus(Notification.Status.FAILED);}return repository.save(n);}
 public List<Notification> byUser(Long id){return repository.findByUserIdOrderBySentDateDesc(id);} public List<Notification> pending(){return repository.findByStatus(Notification.Status.PENDING);}
}
