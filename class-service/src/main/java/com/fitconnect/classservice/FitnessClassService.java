package com.fitconnect.classservice;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;

@Service @RequiredArgsConstructor
public class FitnessClassService {
  private final FitnessClassRepository repository;
  public Page<FitnessClass> search(FitnessClass.Category category, FitnessClass.Level level, LocalDate dateFrom, LocalDate dateTo, String location, String instructor, Pageable pageable){
    Specification<FitnessClass> spec=Specification.where(null);
    if(category!=null) spec=spec.and((r,q,c)->c.equal(r.get("category"),category));
    if(level!=null) spec=spec.and((r,q,c)->c.equal(r.get("level"),level));
    if(dateFrom!=null) spec=spec.and((r,q,c)->c.greaterThanOrEqualTo(r.get("dateTime"),dateFrom.atStartOfDay()));
    if(dateTo!=null) spec=spec.and((r,q,c)->c.lessThan(r.get("dateTime"),dateTo.plusDays(1).atStartOfDay()));
    if(location!=null&&!location.isBlank()) spec=spec.and((r,q,c)->c.like(c.lower(r.get("gymLocation")),"%"+location.toLowerCase()+"%"));
    if(instructor!=null&&!instructor.isBlank()) spec=spec.and((r,q,c)->c.like(c.lower(r.get("instructor")),"%"+instructor.toLowerCase()+"%"));
    return repository.findAll(spec,pageable);
  }
  public FitnessClass get(Long id){ return repository.findById(id).orElseThrow(()->new EntityNotFoundException("Cours introuvable: "+id)); }
  @Transactional public FitnessClass create(FitnessClass value){ value.setId(null); value.setVersion(null); if(value.getCurrentParticipants()==null)value.setCurrentParticipants(0); if(value.getStatus()==null)value.setStatus(FitnessClass.Status.SCHEDULED); return repository.save(value); }
  @Transactional public FitnessClass update(Long id,FitnessClass value){ FitnessClass current=get(id); Long version=current.getVersion(); value.setId(id); value.setVersion(version); value.setCurrentParticipants(current.getCurrentParticipants()); if(value.getMaxParticipants()<current.getCurrentParticipants())throw new IllegalStateException("maxParticipants ne peut pas être inférieur aux places déjà réservées"); return repository.save(value); }
  @Transactional public FitnessClass cancel(Long id){ FitnessClass value=get(id); value.setStatus(FitnessClass.Status.CANCELLED); return value; }
  @Transactional public FitnessClass increment(Long id,int spots){ FitnessClass value=get(id); if(value.getStatus()!=FitnessClass.Status.SCHEDULED)throw new IllegalStateException("Ce cours n'est pas réservable"); value.incrementParticipants(spots); return value; }
  @Transactional public FitnessClass decrement(Long id,int spots){ FitnessClass value=get(id); value.decrementParticipants(spots); return value; }
}
