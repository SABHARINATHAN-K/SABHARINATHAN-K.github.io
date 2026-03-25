package com.careerplanning.backend.modules.career.repository;

import com.careerplanning.backend.modules.career.entity.TechnicalAssessmentQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TechnicalAssessmentQuestionRepository extends JpaRepository<TechnicalAssessmentQuestion, Long> {
    List<TechnicalAssessmentQuestion> findByCareerTrackAndActiveTrueOrderByDisplayOrderAscIdAsc(String careerTrack);
    List<TechnicalAssessmentQuestion> findByCareerTrackOrderByDisplayOrderAscIdAsc(String careerTrack);
    boolean existsByCareerTrackAndDisplayOrder(String careerTrack, Integer displayOrder);
    boolean existsByCareerTrackAndDisplayOrderAndIdNot(String careerTrack, Integer displayOrder, Long id);

    @Query("select distinct q.careerTrack from TechnicalAssessmentQuestion q where q.active = true order by q.careerTrack asc")
    List<String> findDistinctCareerTracksByActiveTrue();
}
