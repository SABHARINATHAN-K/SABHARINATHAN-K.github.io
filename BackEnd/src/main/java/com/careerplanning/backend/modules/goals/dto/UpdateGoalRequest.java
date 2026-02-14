package com.careerplanning.backend.modules.goals.dto;

import com.careerplanning.backend.modules.goals.entity.GoalCategory;
import com.careerplanning.backend.modules.goals.entity.GoalPriority;
import com.careerplanning.backend.modules.goals.entity.GoalStatus;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record UpdateGoalRequest(
        String title,
        @Size(max = 2000) String description,
        GoalStatus status,
        GoalPriority priority,
        GoalCategory category,
        LocalDate targetDate,
        Integer progress,
        @Size(max = 4000) String notes,
        List<@Size(max = 100) String> tags
) {
}
