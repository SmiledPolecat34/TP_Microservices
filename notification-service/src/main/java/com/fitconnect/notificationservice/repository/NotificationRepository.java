package com.fitconnect.notificationservice.repository;

import com.fitconnect.notificationservice.controller.*;
import com.fitconnect.notificationservice.exception.*;
import com.fitconnect.notificationservice.model.*;
import com.fitconnect.notificationservice.service.*;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
  List<Notification> findByUserIdOrderBySentDateDesc(Long userId);

  List<Notification> findByStatus(Notification.Status status);
}
