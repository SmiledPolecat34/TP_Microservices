package com.fitconnect.notificationservice.service;

import com.fitconnect.notificationservice.controller.*;
import com.fitconnect.notificationservice.exception.*;
import com.fitconnect.notificationservice.model.*;
import com.fitconnect.notificationservice.repository.*;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
  private final NotificationRepository repository;

  @Transactional
  public Notification send(Notification n) {
    n.setId(null);
    n.setStatus(Notification.Status.PENDING);
    n = repository.save(n);
    return deliver(n);
  }

  @Transactional
  public Notification retry(Long id) {
    return deliver(
        repository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Notification introuvable: " + id)));
  }

  private Notification deliver(Notification n) {
    try {
      // Envoi simulé : aucun serveur SMTP ni passerelle SMS n'est requis pour le TP.
      log.info("NOTIFICATION [{}] vers {}: {}", n.getType(), n.getEmail(), n.getContent());
      n.setSentDate(LocalDateTime.now());
      n.setStatus(Notification.Status.SENT);
    } catch (RuntimeException e) {
      n.setStatus(Notification.Status.FAILED);
    }
    return repository.save(n);
  }

  public List<Notification> byUser(Long id) {
    return repository.findByUserIdOrderBySentDateDesc(id);
  }

  public List<Notification> pending() {
    return repository.findByStatus(Notification.Status.PENDING);
  }
}
