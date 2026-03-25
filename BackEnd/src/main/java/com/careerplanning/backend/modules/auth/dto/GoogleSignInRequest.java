package com.careerplanning.backend.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record GoogleSignInRequest(
        @NotBlank String idToken
) {
}
