package com.careerplanning.backend.modules.users.dto;

public record UserProfileResponse(
        Long id,
        String fullName,
        String email,
        String role,
        String careerTrack
) {
}
