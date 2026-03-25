package com.careerplanning.backend.modules.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record AdminUpdateCareerPhaseRequest(
        String careerTrack,
        @Min(1) Integer phaseOrder,
        String phaseTitle,
        @Size(max = 2000) String description
) {
}
