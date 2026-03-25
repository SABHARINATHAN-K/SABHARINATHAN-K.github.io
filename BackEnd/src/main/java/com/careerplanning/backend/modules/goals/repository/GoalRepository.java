package com.careerplanning.backend.modules.goals.repository;

import com.careerplanning.backend.modules.goals.entity.Goal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<Goal> findByIdAndUserId(Long id, Long userId);
    List<Goal> findByUserIdAndBlueprintGoalTrue(Long userId);
    boolean existsByUserIdAndBlueprintTemplateId(Long userId, Long blueprintTemplateId);
}
