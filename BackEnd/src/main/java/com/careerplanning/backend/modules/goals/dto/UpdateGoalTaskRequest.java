package com.careerplanning.backend.modules.goals.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateGoalTaskRequest(
        @Size(max = 320) String title,
        @Size(max = 2000) String details,
        LocalDate dueDate,
        @Min(1) @Max(100) Integer weight,
        @Min(1) @Max(99) Integer sortOrder,
        Boolean completed
) {
}
