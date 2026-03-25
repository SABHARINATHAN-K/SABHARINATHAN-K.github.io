package com.careerplanning.backend.modules.admin.dto;

import java.util.List;

public record AdminTechnicalAssessmentQuestionResponse(
        Long id,
        String careerTrack,
        String skillArea,
        String difficulty,
        String questionText,
        String explanation,
        int displayOrder,
        boolean active,
        List<AdminTechnicalAssessmentOptionResponse> options
) {
}
