package com.careerplanning.backend.modules.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminCreateCareerPhaseRequest(
        @NotBlank String careerTrack,
        @NotNull @Min(1) Integer phaseOrder,
        @NotBlank String phaseTitle,
        @Size(max = 2000) String description
) {
}
