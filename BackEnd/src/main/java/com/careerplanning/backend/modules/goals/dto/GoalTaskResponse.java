package com.careerplanning.backend.modules.goals.dto;

import java.time.Instant;
import java.time.LocalDate;

public record GoalTaskResponse(
        Long id,
        String title,
        String details,
        LocalDate dueDate,
        Integer weight,
        Integer sortOrder,
        Boolean completed,
        Instant completedAt
) {
}
