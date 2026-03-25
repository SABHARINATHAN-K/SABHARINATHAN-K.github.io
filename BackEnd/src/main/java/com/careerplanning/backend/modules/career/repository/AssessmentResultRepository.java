package com.careerplanning.backend.modules.career.repository;

import com.careerplanning.backend.modules.career.entity.AssessmentResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentResultRepository extends JpaRepository<AssessmentResult, Long> {
}
