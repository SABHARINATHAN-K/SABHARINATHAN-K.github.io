package com.careerplanning.backend.modules.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminCreateCareerGoalTemplateRequest(
        @NotBlank String careerTrack,
        @NotNull Long phaseId,
        @NotBlank String title,
        @Size(max = 2000) String description,
        @NotBlank String category,
        @NotBlank String priority,
        @NotNull @Min(1) Integer defaultOrder
) {
}
