package com.careerplanning.backend.modules.career.dto;

public record SkillAreaScoreResponse(
        String skillArea,
        int score,
        int maxScore,
        int percentageScore
) {
}
