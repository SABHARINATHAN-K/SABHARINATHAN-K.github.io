package com.careerplanning.backend.modules.career.dto;

import jakarta.validation.constraints.NotNull;

public record AssessmentAnswer(
        @NotNull Long questionId,
        @NotNull Long optionId
) {
}
