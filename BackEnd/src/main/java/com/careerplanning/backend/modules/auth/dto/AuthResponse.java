package com.careerplanning.backend.modules.auth.dto;

public record AuthResponse(
        Long userId,
        String token
) {
}
