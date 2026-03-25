package com.careerplanning.backend.modules.goals.dto;

import com.careerplanning.backend.modules.goals.entity.GoalCategory;
import com.careerplanning.backend.modules.goals.entity.GoalPriority;
import com.careerplanning.backend.modules.goals.entity.GoalStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record GoalResponse(
        Long id,
        Long userId,
        String title,
        String description,
        GoalStatus status,
        GoalPriority priority,
        GoalCategory category,
        LocalDate targetDate,
        Instant completedDate,
        Integer progress,
        List<GoalTaskResponse> tasks,
        String notes,
        List<String> tags,
        boolean isBlueprintGoal,
        Long blueprintTemplateId,
        Integer blueprintPhaseOrder,
        Integer blueprintDefaultOrder,
        String blueprintPhaseTitle,
        Instant createdAt,
        Instant updatedAt
) {
}
