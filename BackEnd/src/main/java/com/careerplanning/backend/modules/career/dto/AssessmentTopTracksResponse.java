package com.careerplanning.backend.modules.career.dto;

import java.util.List;

public record AssessmentTopTracksResponse(
        List<String> topTracks,
        List<String> topClusters,
        List<CareerMatchPercentage> matchPercentages
) {
}
