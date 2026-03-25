package com.careerplanning.backend.modules.admin.dto;

public record AdminTechnicalAssessmentOptionResponse(
        Long id,
        String optionText,
        boolean correct,
        int sortOrder
) {
}
