package com.careerplanning.backend.modules.goals.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record CreateGoalRequest(
        @NotBlank String title,
        String description,
        LocalDate targetDate
) {
}
