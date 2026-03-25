package com.careerplanning.backend.modules.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminTechnicalAssessmentOptionRequest(
        @NotBlank String optionText,
        @NotNull Boolean correct,
        Integer sortOrder
) {
}
