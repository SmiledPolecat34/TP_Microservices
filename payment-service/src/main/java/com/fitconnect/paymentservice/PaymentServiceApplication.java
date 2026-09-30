package com.fitconnect.paymentservice;

import com.fitconnect.paymentservice.controller.*;
import com.fitconnect.paymentservice.exception.*;
import com.fitconnect.paymentservice.model.*;
import com.fitconnect.paymentservice.repository.*;
import com.fitconnect.paymentservice.service.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PaymentServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(PaymentServiceApplication.class, args);
  }
}
