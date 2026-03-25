package com.careerplanning.backend.modules.goals.repository;

import com.careerplanning.backend.modules.goals.entity.GoalTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface GoalTaskRepository extends JpaRepository<GoalTask, Long> {
    List<GoalTask> findByGoalIdOrderBySortOrderAscIdAsc(Long goalId);
    List<GoalTask> findByGoalIdInOrderByGoalIdAscSortOrderAscIdAsc(Collection<Long> goalIds);
    Optional<GoalTask> findByIdAndGoalId(Long id, Long goalId);
    long countByGoalId(Long goalId);
}
