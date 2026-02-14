package com.careerplanning.backend.modules.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(
        @NotBlank String fullName,
        @Size(max = 2000) String bio,
        @Size(max = 255) String location,
        @NotBlank String role,
        @NotBlank String careerTrack
) {
}
