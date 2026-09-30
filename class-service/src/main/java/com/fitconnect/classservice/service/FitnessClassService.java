package com.fitconnect.classservice.service;

import com.fitconnect.classservice.client.*;
import com.fitconnect.classservice.controller.*;
import com.fitconnect.classservice.dto.*;
import com.fitconnect.classservice.exception.*;
import com.fitconnect.classservice.model.*;
import com.fitconnect.classservice.repository.*;
import jakarta.persistence.EntityNotFoundException;
import java.time.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FitnessClassService {
  private final FitnessClassRepository repository;

  public Page<FitnessClass> search(
      FitnessClass.Category category,
      FitnessClass.Level level,
      LocalDate dateFrom,
      LocalDate dateTo,
      String location,
      String instructor,
      Pageable pageable) {
    Specification<FitnessClass> spec = Specification.where(null);
    if (category != null) spec = spec.and((r, q, c) -> c.equal(r.get("category"), category));
    if (level != null) spec = spec.and((r, q, c) -> c.equal(r.get("level"), level));
    if (dateFrom != null)
      spec =
          spec.and((r, q, c) -> c.greaterThanOrEqualTo(r.get("dateTime"), dateFrom.atStartOfDay()));
    if (dateTo != null)
      spec =
          spec.and((r, q, c) -> c.lessThan(r.get("dateTime"), dateTo.plusDays(1).atStartOfDay()));
    if (location != null && !location.isBlank())
      spec =
          spec.and(
              (r, q, c) ->
                  c.like(c.lower(r.get("gymLocation")), "%" + location.toLowerCase() + "%"));
    if (instructor != null && !instructor.isBlank())
      spec =
          spec.and(
              (r, q, c) ->
                  c.like(c.lower(r.get("instructor")), "%" + instructor.toLowerCase() + "%"));
    return repository.findAll(spec, pageable);
  }

  public FitnessClass get(Long id) {
    return repository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Cours introuvable: " + id));
  }

  @Transactional
  public FitnessClass create(FitnessClassRequest request) {
    return repository.save(request.applyTo(new FitnessClass()));
  }

  @Transactional
  public FitnessClass update(Long id, FitnessClassRequest request) {
    FitnessClass current = get(id);
    if (request.maxParticipants() < current.getCurrentParticipants())
      throw new IllegalStateException(
          "maxParticipants ne peut pas être inférieur aux places déjà réservées");
    return request.applyTo(current);
  }

  @Transactional
  public FitnessClass cancel(Long id) {
    FitnessClass value = get(id);
    value.setStatus(FitnessClass.Status.CANCELLED);
    return value;
  }

  @Transactional
  public FitnessClass increment(Long id, int spots) {
    FitnessClass value = get(id);
    if (value.getStatus() != FitnessClass.Status.SCHEDULED)
      throw new IllegalStateException("Ce cours n'est pas réservable");
    value.incrementParticipants(spots);
    return value;
  }

  @Transactional
  public FitnessClass decrement(Long id, int spots) {
    FitnessClass value = get(id);
    value.decrementParticipants(spots);
    return value;
  }
}
