package com.careerplanning.backend.modules.career.dto;

import jakarta.validation.constraints.NotBlank;

public record ConfirmCareerTrackRequest(
        @NotBlank String careerTrack
) {
}
