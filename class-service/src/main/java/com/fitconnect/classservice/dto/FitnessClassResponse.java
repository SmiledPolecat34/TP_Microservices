package com.fitconnect.classservice.dto;

import com.fitconnect.classservice.model.FitnessClass;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Vue d'un cours renvoyée par l'API : l'entité JPA (et son champ de version) n'est pas exposée. */
public record FitnessClassResponse(
    Long id,
    String name,
    String description,
    String instructor,
    String gymLocation,
    FitnessClass.Category category,
    FitnessClass.Level level,
    Integer durationMinutes,
    Integer maxParticipants,
    Integer currentParticipants,
    BigDecimal price,
    LocalDateTime dateTime,
    FitnessClass.Status status) {

  public static FitnessClassResponse from(FitnessClass c) {
    return new FitnessClassResponse(
        c.getId(),
        c.getName(),
        c.getDescription(),
        c.getInstructor(),
        c.getGymLocation(),
        c.getCategory(),
        c.getLevel(),
        c.getDurationMinutes(),
        c.getMaxParticipants(),
        c.getCurrentParticipants(),
        c.getPrice(),
        c.getDateTime(),
        c.getStatus());
  }
}
