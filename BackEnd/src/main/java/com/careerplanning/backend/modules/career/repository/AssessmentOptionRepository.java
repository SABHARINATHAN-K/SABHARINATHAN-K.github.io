package com.careerplanning.backend.modules.career.repository;

import com.careerplanning.backend.modules.career.entity.AssessmentOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface AssessmentOptionRepository extends JpaRepository<AssessmentOption, Long> {
    List<AssessmentOption> findByQuestionIdInOrderByQuestionIdAscIdAsc(Collection<Long> questionIds);
}
