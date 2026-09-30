package com.fitconnect.classservice.repository;

import com.fitconnect.classservice.client.*;
import com.fitconnect.classservice.controller.*;
import com.fitconnect.classservice.exception.*;
import com.fitconnect.classservice.model.*;
import com.fitconnect.classservice.service.*;
import org.springframework.data.jpa.repository.*;

public interface FitnessClassRepository
    extends JpaRepository<FitnessClass, Long>, JpaSpecificationExecutor<FitnessClass> {}
