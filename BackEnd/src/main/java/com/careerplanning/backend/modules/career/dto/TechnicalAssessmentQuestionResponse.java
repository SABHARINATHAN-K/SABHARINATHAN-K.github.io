package com.careerplanning.backend.modules.career.dto;

import java.util.List;

public record TechnicalAssessmentQuestionResponse(
        Long id,
        String careerTrack,
        String skillArea,
        String difficulty,
        String questionText,
        List<TechnicalAssessmentOptionResponse> options
) {
}
