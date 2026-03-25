package com.careerplanning.backend.modules.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record AdminUpdateCareerGoalTemplateRequest(
        String careerTrack,
        Long phaseId,
        String title,
        @Size(max = 2000) String description,
        String category,
        String priority,
        @Min(1) Integer defaultOrder
) {
}
