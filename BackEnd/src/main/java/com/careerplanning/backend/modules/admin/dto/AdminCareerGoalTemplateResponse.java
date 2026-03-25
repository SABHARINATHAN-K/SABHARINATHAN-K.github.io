package com.careerplanning.backend.modules.admin.dto;

public record AdminCareerGoalTemplateResponse(
        Long id,
        String careerTrack,
        Long phaseId,
        Integer phaseOrder,
        String phaseTitle,
        String title,
        String description,
        String category,
        String priority,
        Integer defaultOrder
) {
}
