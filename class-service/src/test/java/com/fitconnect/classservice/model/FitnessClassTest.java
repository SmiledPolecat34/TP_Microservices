package com.fitconnect.classservice.model;

import static org.junit.jupiter.api.Assertions.*;

import com.fitconnect.classservice.client.*;
import com.fitconnect.classservice.controller.*;
import com.fitconnect.classservice.exception.*;
import com.fitconnect.classservice.repository.*;
import com.fitconnect.classservice.service.*;
import org.junit.jupiter.api.Test;

class FitnessClassTest {
  @Test
  void shouldIncrement_whenSpotsAvailable() {
    FitnessClass c = FitnessClass.builder().maxParticipants(10).currentParticipants(5).build();
    c.incrementParticipants(2);
    assertEquals(7, c.getCurrentParticipants());
  }

  @Test
  void shouldThrowException_whenNoSpotsAvailable() {
    FitnessClass c = FitnessClass.builder().maxParticipants(10).currentParticipants(9).build();
    assertThrows(NoSpotsAvailableException.class, () -> c.incrementParticipants(2));
  }
}
