package com.careerplanning.backend.modules.career.dto;

public record GenerateRoadmapResponse(
        String careerTrack,
        int templatesFound,
        int goalsCreated,
        int goalsSkipped,
        String warning
) {
}
