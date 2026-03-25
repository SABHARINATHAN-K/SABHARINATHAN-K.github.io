package com.careerplanning.backend.modules.auth.controller;

import com.careerplanning.backend.common.response.ApiResponse;
import com.careerplanning.backend.modules.auth.dto.AuthResponse;
import com.careerplanning.backend.modules.auth.dto.GoogleSignInRequest;
import com.careerplanning.backend.modules.auth.dto.LoginRequest;
import com.careerplanning.backend.modules.auth.dto.RegisterRequest;
import com.careerplanning.backend.modules.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success(authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @PostMapping("/google")
    public ApiResponse<AuthResponse> googleSignIn(@Valid @RequestBody GoogleSignInRequest request) {
        return ApiResponse.success(authService.googleSignIn(request));
    }
}
