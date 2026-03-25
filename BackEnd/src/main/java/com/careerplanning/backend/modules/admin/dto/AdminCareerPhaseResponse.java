package com.careerplanning.backend.modules.admin.dto;

public record AdminCareerPhaseResponse(
        Long id,
        String careerTrack,
        Integer phaseOrder,
        String phaseTitle,
        String description
) {
}
