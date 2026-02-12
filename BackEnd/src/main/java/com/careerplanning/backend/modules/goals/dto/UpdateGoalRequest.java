package com.careerplanning.backend.modules.goals.dto;

import com.careerplanning.backend.modules.goals.entity.GoalStatus;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record UpdateGoalRequest(
        @NotBlank String title,
        String description,
        GoalStatus status,
        LocalDate targetDate
) {
}
