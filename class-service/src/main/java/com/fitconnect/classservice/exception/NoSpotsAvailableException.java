package com.fitconnect.classservice.exception;

import com.fitconnect.classservice.client.*;
import com.fitconnect.classservice.controller.*;
import com.fitconnect.classservice.model.*;
import com.fitconnect.classservice.repository.*;
import com.fitconnect.classservice.service.*;

public class NoSpotsAvailableException extends RuntimeException {
  public NoSpotsAvailableException(String message) {
    super(message);
  }
}
