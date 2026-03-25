package com.careerplanning.backend.modules.admin.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AdminCreateTechnicalAssessmentQuestionRequest(
        @NotBlank String careerTrack,
        @NotBlank String skillArea,
        @NotBlank String difficulty,
        @NotBlank @Size(max = 2000) String questionText,
        @Size(max = 2000) String explanation,
        @NotNull @Min(1) Integer displayOrder,
        Boolean active,
        @NotNull @Size(min = 4, max = 4) List<@Valid AdminTechnicalAssessmentOptionRequest> options
) {
}
