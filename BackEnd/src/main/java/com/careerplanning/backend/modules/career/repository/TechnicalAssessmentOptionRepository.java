package com.careerplanning.backend.modules.career.repository;

import com.careerplanning.backend.modules.career.entity.TechnicalAssessmentOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface TechnicalAssessmentOptionRepository extends JpaRepository<TechnicalAssessmentOption, Long> {
    List<TechnicalAssessmentOption> findByQuestionIdInOrderByQuestionIdAscSortOrderAscIdAsc(Collection<Long> questionIds);
    List<TechnicalAssessmentOption> findByQuestionIdOrderBySortOrderAscIdAsc(Long questionId);
    void deleteByQuestionId(Long questionId);
}
