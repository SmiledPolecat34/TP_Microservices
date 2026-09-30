package com.fitconnect.notificationservice;

import com.fitconnect.notificationservice.controller.*;
import com.fitconnect.notificationservice.exception.*;
import com.fitconnect.notificationservice.model.*;
import com.fitconnect.notificationservice.repository.*;
import com.fitconnect.notificationservice.service.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NotificationServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(NotificationServiceApplication.class, args);
  }
}
