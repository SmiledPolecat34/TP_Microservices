package com.fitconnect.classservice;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name="fitness_classes") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FitnessClass {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Version private Long version;
  @NotBlank @Size(min=3) private String name;
  @NotBlank @Column(length=2000) private String description;
  @NotBlank private String instructor;
  @NotBlank private String gymLocation;
  @Enumerated(EnumType.STRING) @NotNull private Category category;
  @Enumerated(EnumType.STRING) @NotNull private Level level;
  @NotNull private Integer durationMinutes;
  @NotNull @Min(5) @Max(30) private Integer maxParticipants;
  @NotNull @Min(0) @Builder.Default private Integer currentParticipants=0;
  @NotNull @DecimalMin("5.00") private BigDecimal price;
  @NotNull @FutureOrPresent private LocalDateTime dateTime;
  @Enumerated(EnumType.STRING) @NotNull @Builder.Default private Status status=Status.SCHEDULED;
  public enum Category { YOGA,CROSSFIT,ZUMBA,PILATES,SPINNING,BOXING }
  public enum Level { BEGINNER,INTERMEDIATE,ADVANCED }
  public enum Status { SCHEDULED,CANCELLED,COMPLETED }
  @com.fasterxml.jackson.annotation.JsonIgnore @AssertTrue(message="durationMinutes doit valoir 30, 45, 60 ou 90") public boolean isDurationValid(){ return durationMinutes==null || java.util.Set.of(30,45,60,90).contains(durationMinutes); }
  @com.fasterxml.jackson.annotation.JsonIgnore @AssertTrue(message="currentParticipants ne peut pas dépasser maxParticipants") public boolean isCapacityValid(){ return currentParticipants==null || maxParticipants==null || currentParticipants<=maxParticipants; }
  public void incrementParticipants(int spots){ if(spots<1 || currentParticipants+spots>maxParticipants) throw new NoSpotsAvailableException("Plus de places disponibles pour ce cours"); currentParticipants+=spots; }
  public void decrementParticipants(int spots){ if(spots<1) throw new IllegalArgumentException("Le nombre de places doit être positif"); currentParticipants=Math.max(0,currentParticipants-spots); }
}
