package com.fitconnect.classservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fitconnect.classservice.model.FitnessClass;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Données acceptées à la création et à la mise à jour d'un cours. L'identifiant, la version, le
 * nombre de participants et le statut sont gérés par le service et ne peuvent pas être imposés par
 * le client.
 */
public record FitnessClassRequest(
    @NotBlank @Size(min = 3) String name,
    @NotBlank String description,
    @NotBlank String instructor,
    @NotBlank String gymLocation,
    @NotNull FitnessClass.Category category,
    @NotNull FitnessClass.Level level,
    @NotNull Integer durationMinutes,
    @NotNull @Min(5) @Max(30) Integer maxParticipants,
    @NotNull @DecimalMin("5.00") BigDecimal price,
    @NotNull @FutureOrPresent LocalDateTime dateTime) {

  @JsonIgnore
  @AssertTrue(message = "durationMinutes doit valoir 30, 45, 60 ou 90")
  public boolean isDurationValid() {
    return durationMinutes == null || Set.of(30, 45, 60, 90).contains(durationMinutes);
  }

  /** Copie les champs modifiables dans l'entité. */
  public FitnessClass applyTo(FitnessClass target) {
    target.setName(name);
    target.setDescription(description);
    target.setInstructor(instructor);
    target.setGymLocation(gymLocation);
    target.setCategory(category);
    target.setLevel(level);
    target.setDurationMinutes(durationMinutes);
    target.setMaxParticipants(maxParticipants);
    target.setPrice(price);
    target.setDateTime(dateTime);
    return target;
  }
}
