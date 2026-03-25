package com.careerplanning.backend.modules.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record AdminUpdateUserRequest(
        @Size(max = 255) String fullName,
        @Email String email,
        @Size(max = 2000) String bio,
        @Size(max = 255) String location,
        String role,
        String careerTrack,
        Boolean onboardingCompleted
) {
}
