package com.careerplanning.backend.modules.career.repository;

import com.careerplanning.backend.modules.career.entity.AssessmentQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssessmentQuestionRepository extends JpaRepository<AssessmentQuestion, Long> {
    List<AssessmentQuestion> findByStageAndIsActiveTrueOrderByIdAsc(Integer stage);
    List<AssessmentQuestion> findAllByIsActiveTrueOrderByStageAscIdAsc();
}
