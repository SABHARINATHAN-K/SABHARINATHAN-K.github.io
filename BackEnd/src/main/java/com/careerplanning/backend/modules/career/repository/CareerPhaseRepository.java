package com.careerplanning.backend.modules.career.repository;

import com.careerplanning.backend.modules.career.entity.CareerPhase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CareerPhaseRepository extends JpaRepository<CareerPhase, Long> {
    List<CareerPhase> findByCareerTrackOrderByPhaseOrderAsc(String careerTrack);
    List<CareerPhase> findAllByOrderByCareerTrackAscPhaseOrderAscIdAsc();
    boolean existsByCareerTrackAndPhaseOrder(String careerTrack, Integer phaseOrder);
    boolean existsByCareerTrackAndPhaseOrderAndIdNot(String careerTrack, Integer phaseOrder, Long id);

    @Query("select distinct p.careerTrack from CareerPhase p order by p.careerTrack asc")
    List<String> findDistinctCareerTracks();
}
