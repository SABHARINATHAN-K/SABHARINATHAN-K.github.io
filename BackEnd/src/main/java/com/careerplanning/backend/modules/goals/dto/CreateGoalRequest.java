package com.careerplanning.backend.modules.goals.dto;

import com.careerplanning.backend.modules.goals.entity.GoalCategory;
import com.careerplanning.backend.modules.goals.entity.GoalPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record CreateGoalRequest(
        @NotBlank String title,
        @Size(max = 2000) String description,
        GoalPriority priority,
        GoalCategory category,
        LocalDate targetDate,
        List<@Size(max = 100) String> tags
) {
}
