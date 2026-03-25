package com.careerplanning.backend.modules.career.repository;

import com.careerplanning.backend.modules.career.entity.TechnicalAssessmentResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TechnicalAssessmentResultRepository extends JpaRepository<TechnicalAssessmentResult, Long> {
    Optional<TechnicalAssessmentResult> findFirstByUserIdAndCareerTrackOrderByCreatedAtDesc(Long userId, String careerTrack);
    Optional<TechnicalAssessmentResult> findFirstByUserIdOrderByCreatedAtDesc(Long userId);
    List<TechnicalAssessmentResult> findTop8ByUserIdAndCareerTrackOrderByCreatedAtDesc(Long userId, String careerTrack);
}
