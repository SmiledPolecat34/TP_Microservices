package com.fitconnect.classservice;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class FitnessClassTest {
  @Test void shouldIncrement_whenSpotsAvailable(){ FitnessClass c=FitnessClass.builder().maxParticipants(10).currentParticipants(5).build(); c.incrementParticipants(2); assertEquals(7,c.getCurrentParticipants()); }
  @Test void shouldThrowException_whenNoSpotsAvailable(){ FitnessClass c=FitnessClass.builder().maxParticipants(10).currentParticipants(9).build(); assertThrows(NoSpotsAvailableException.class,()->c.incrementParticipants(2)); }
}
