package com.careerplanning.backend.modules.career.dto;

import java.util.List;

public record AssessCareerResponse(
        List<CareerMatchPercentage> matchPercentages,
        String recommendedTrack,
        String recommendedCareer,
        String confidenceLevel,
        String explanation
) {
}
