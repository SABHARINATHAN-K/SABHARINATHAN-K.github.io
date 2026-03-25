package com.careerplanning.backend.modules.admin.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AdminUpdateTechnicalAssessmentQuestionRequest(
        String careerTrack,
        String skillArea,
        String difficulty,
        @Size(max = 2000) String questionText,
        @Size(max = 2000) String explanation,
        @Min(1) Integer displayOrder,
        Boolean active,
        @Size(min = 4, max = 4) List<@Valid AdminTechnicalAssessmentOptionRequest> options
) {
}
