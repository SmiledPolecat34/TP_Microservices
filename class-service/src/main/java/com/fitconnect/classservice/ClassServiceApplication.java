package com.fitconnect.classservice;

import com.fitconnect.classservice.client.*;
import com.fitconnect.classservice.controller.*;
import com.fitconnect.classservice.exception.*;
import com.fitconnect.classservice.model.*;
import com.fitconnect.classservice.repository.*;
import com.fitconnect.classservice.service.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ClassServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(ClassServiceApplication.class, args);
  }
}
