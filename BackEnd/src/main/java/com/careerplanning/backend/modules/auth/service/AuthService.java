package com.careerplanning.backend.modules.auth.service;

import com.careerplanning.backend.modules.auth.dto.AuthResponse;
import com.careerplanning.backend.modules.auth.dto.LoginRequest;
import com.careerplanning.backend.modules.auth.dto.RegisterRequest;
import com.careerplanning.backend.modules.users.entity.CareerTrack;
import com.careerplanning.backend.modules.users.entity.User;
import com.careerplanning.backend.modules.users.entity.UserRole;
import com.careerplanning.backend.modules.users.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final SimpleTokenService simpleTokenService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository, SimpleTokenService simpleTokenService) {
        this.userRepository = userRepository;
        this.simpleTokenService = simpleTokenService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        User user = new User();
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(validateRole(request.role()));
        user.setCareerTrack(validateCareerTrack(request.careerTrack()));

        User savedUser = userRepository.save(user);
        String token = simpleTokenService.createToken(savedUser.getId());
        return new AuthResponse(savedUser.getId(), token);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String token = simpleTokenService.createToken(user.getId());
        return new AuthResponse(user.getId(), token);
    }

    private String validateRole(String role) {
        String normalized = role == null ? "" : role.trim().toUpperCase();
        if (!UserRole.isValid(normalized)) {
            throw new IllegalArgumentException("Invalid role. Use one of: " + String.join(", ", UserRole.options()));
        }
        return normalized;
    }

    private String validateCareerTrack(String careerTrack) {
        String normalized = careerTrack == null ? "" : careerTrack.trim().toUpperCase();
        if (!CareerTrack.isValid(normalized)) {
            throw new IllegalArgumentException("Invalid careerTrack. Use one of: " + String.join(", ", CareerTrack.options()));
        }
        return normalized;
    }
}
