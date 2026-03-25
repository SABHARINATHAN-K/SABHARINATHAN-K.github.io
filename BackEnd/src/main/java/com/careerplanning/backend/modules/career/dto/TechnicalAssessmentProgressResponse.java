package com.careerplanning.backend.modules.career.dto;

import java.time.Instant;
import java.util.List;

public record TechnicalAssessmentProgressResponse(
        String careerTrack,
        String currentLevel,
        Integer currentPercentageScore,
        Integer previousPercentageScore,
        Integer improvementPercentagePoints,
        Instant lastAssessedAt,
        Instant recommendedReassessmentAt,
        boolean reassessmentDue,
        String summary,
        List<SkillAreaScoreResponse> latestSkillAreas,
        List<TechnicalAssessmentHistoryItemResponse> history
) {
}
