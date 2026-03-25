package com.careerplanning.backend.modules.career.dto;

import java.time.Instant;
import java.util.List;

public record TechnicalAssessmentResultResponse(
        String careerTrack,
        String proficiencyLevel,
        int totalScore,
        int maxScore,
        int percentageScore,
        Integer improvementPercentagePoints,
        List<SkillAreaScoreResponse> skillAreas,
        String performanceSummary,
        Instant assessedAt
) {
}
