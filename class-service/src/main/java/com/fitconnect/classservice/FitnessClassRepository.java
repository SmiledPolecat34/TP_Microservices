package com.fitconnect.classservice;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.jpa.domain.Specification;
public interface FitnessClassRepository extends JpaRepository<FitnessClass,Long>, JpaSpecificationExecutor<FitnessClass> {}
