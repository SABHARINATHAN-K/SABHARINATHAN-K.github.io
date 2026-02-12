package com.careerplanning.backend.modules.goals.dto;

import com.careerplanning.backend.modules.goals.entity.GoalStatus;

import java.time.LocalDate;

public record GoalResponse(
        Long id,
        String title,
        String description,
        GoalStatus status,
        LocalDate targetDate
) {
}
