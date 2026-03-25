package com.careerplanning.backend.modules.career.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SubmitTechnicalAssessmentRequest(
        @NotBlank String careerTrack,
        @NotEmpty List<@Valid AssessmentAnswer> answers
) {
}
