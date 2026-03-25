package com.careerplanning.backend.modules.career.dto;

import java.util.List;

public record AssessmentQuestionResponse(
        Long id,
        String questionText,
        int stage,
        List<AssessmentOptionResponse> options
) {
}
