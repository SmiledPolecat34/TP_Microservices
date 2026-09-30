package com.fitconnect.classservice;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController @RequestMapping("/api/classes") @RequiredArgsConstructor
public class FitnessClassController {
  private final FitnessClassService service;
  @GetMapping public Page<FitnessClass> all(@RequestParam(required=false) FitnessClass.Category category,@RequestParam(required=false) FitnessClass.Level level,@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate dateFrom,@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate dateTo,@RequestParam(required=false) String location,@RequestParam(required=false) String instructor,Pageable pageable){ return service.search(category,level,dateFrom,dateTo,location,instructor,pageable); }
  @GetMapping("/search") public Page<FitnessClass> search(@RequestParam(required=false) FitnessClass.Category category,@RequestParam(required=false) FitnessClass.Level level,@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate dateFrom,@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate dateTo,@RequestParam(required=false) String location,@RequestParam(required=false) String instructor,Pageable pageable){ return all(category,level,dateFrom,dateTo,location,instructor,pageable); }
  @GetMapping("/{id}") public FitnessClass get(@PathVariable Long id){ return service.get(id); }
  @PostMapping public ResponseEntity<FitnessClass> create(@Valid @RequestBody FitnessClass value){ return ResponseEntity.status(HttpStatus.CREATED).body(service.create(value)); }
  @PutMapping("/{id}") public FitnessClass update(@PathVariable Long id,@Valid @RequestBody FitnessClass value){ return service.update(id,value); }
  @DeleteMapping("/{id}") public FitnessClass cancel(@PathVariable Long id){ return service.cancel(id); }
  @PatchMapping("/{id}/increment") public FitnessClass increment(@PathVariable Long id,@RequestParam int spots){ return service.increment(id,spots); }
  @PatchMapping("/{id}/decrement") public FitnessClass decrement(@PathVariable Long id,@RequestParam int spots){ return service.decrement(id,spots); }
}
