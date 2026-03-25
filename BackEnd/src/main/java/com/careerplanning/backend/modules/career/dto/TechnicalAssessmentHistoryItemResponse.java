package com.careerplanning.backend.modules.career.dto;

import java.time.Instant;

public record TechnicalAssessmentHistoryItemResponse(
        Instant assessedAt,
        int percentageScore,
        String proficiencyLevel
) {
}
