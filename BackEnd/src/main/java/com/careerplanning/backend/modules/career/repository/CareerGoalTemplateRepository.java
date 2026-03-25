package com.careerplanning.backend.modules.career.repository;

import com.careerplanning.backend.modules.career.entity.CareerGoalTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CareerGoalTemplateRepository extends JpaRepository<CareerGoalTemplate, Long> {

    @Query(value = """
            SELECT t.*
            FROM career_goal_templates t
            JOIN career_phases p ON p.id = t.phase_id
            WHERE t.career_track = :careerTrack
            ORDER BY p.phase_order ASC, t.default_order ASC
            """, nativeQuery = true)
    List<CareerGoalTemplate> findOrderedByCareerTrack(@Param("careerTrack") String careerTrack);

    List<CareerGoalTemplate> findAllByOrderByCareerTrackAscPhaseIdAscDefaultOrderAscIdAsc();
    List<CareerGoalTemplate> findByPhaseIdOrderByDefaultOrderAscIdAsc(Long phaseId);

    @Query("select distinct t.careerTrack from CareerGoalTemplate t order by t.careerTrack asc")
    List<String> findDistinctCareerTracks();
}
