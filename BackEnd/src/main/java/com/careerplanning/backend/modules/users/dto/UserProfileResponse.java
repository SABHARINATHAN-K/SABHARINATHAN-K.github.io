package com.careerplanning.backend.modules.users.dto;

import java.time.Instant;

public record UserProfileResponse(
        Long id,
        String fullName,
        String email,
        String role,
        String careerTrack,
        boolean onboardingCompleted,
        String bio,
        String location,
        Instant joinedDate
) {
}
