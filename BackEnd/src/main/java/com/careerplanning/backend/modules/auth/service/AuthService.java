package com.careerplanning.backend.modules.auth.service;

import java.util.Locale;
import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.careerplanning.backend.common.exception.AccessDeniedException;
import com.careerplanning.backend.modules.auth.dto.AuthResponse;
import com.careerplanning.backend.modules.auth.dto.GoogleSignInRequest;
import com.careerplanning.backend.modules.auth.dto.LoginRequest;
import com.careerplanning.backend.modules.auth.dto.RegisterRequest;
import com.careerplanning.backend.modules.career.service.CareerTrackCatalogService;
import com.careerplanning.backend.modules.users.entity.User;
import com.careerplanning.backend.modules.users.entity.UserRole;
import com.careerplanning.backend.modules.users.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final SimpleTokenService simpleTokenService;
    private final GoogleTokenVerifierService googleTokenVerifierService;
    private final CareerTrackCatalogService careerTrackCatalogService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository,
                       SimpleTokenService simpleTokenService,
                       GoogleTokenVerifierService googleTokenVerifierService,
                       CareerTrackCatalogService careerTrackCatalogService) {
        this.userRepository = userRepository;
        this.simpleTokenService = simpleTokenService;
        this.googleTokenVerifierService = googleTokenVerifierService;
        this.careerTrackCatalogService = careerTrackCatalogService;
    }

    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        String normalizedFullName = normalizeFullName(request.fullName());
        validatePassword(request.password());

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("Email is already in use");
        }

        User user = new User();
        user.setFullName(normalizedFullName);
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(validateRegistrationRole(request.role()));
        user.setCareerTrack(resolveCareerTrack(request.careerTrack()));
        user.setOnboardingCompleted(request.careerTrack() != null && !request.careerTrack().trim().isBlank());

        User savedUser = userRepository.save(user);
        String token = simpleTokenService.createToken(savedUser.getId());
        return new AuthResponse(savedUser.getId(), token);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(normalizeEmail(request.email()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String token = simpleTokenService.createToken(user.getId());
        return new AuthResponse(user.getId(), token);
    }

    public AuthResponse googleSignIn(GoogleSignInRequest request) {
        GoogleTokenVerifierService.VerifiedGoogleUser verifiedUser =
                googleTokenVerifierService.verifyIdToken(request.idToken());
        String normalizedEmail = normalizeEmail(verifiedUser.email());
        String normalizedFullName = normalizeFullName(verifiedUser.fullName());

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setFullName(normalizedFullName);
                    newUser.setEmail(normalizedEmail);
                    newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                    newUser.setRole(UserRole.STUDENT.name());
                    newUser.setCareerTrack(careerTrackCatalogService.defaultCareerTrack());
                    newUser.setOnboardingCompleted(false);
                    return userRepository.save(newUser);
                });

        String token = simpleTokenService.createToken(user.getId());
        return new AuthResponse(user.getId(), token);
    }

    private String validateRegistrationRole(String role) {
        String normalized = role == null ? "" : role.trim().toUpperCase();
        if (UserRole.ADMIN.name().equals(normalized)) {
            if (!userRepository.existsByRole(UserRole.ADMIN.name())) {
                return normalized;
            }
            throw new AccessDeniedException("Admin role cannot be self-assigned");
        }
        if (!UserRole.isSelfServiceRole(normalized)) {
            throw new IllegalArgumentException("Invalid role. Use one of: " + String.join(", ", UserRole.selfServiceOptions()));
        }
        return normalized;
    }

    private String resolveCareerTrack(String careerTrack) {
        if (careerTrack == null || careerTrack.trim().isBlank()) {
            return careerTrackCatalogService.defaultCareerTrack();
        }
        return careerTrackCatalogService.validateKnownCareerTrack(careerTrack);
    }

    private String normalizeEmail(String email) {
        String normalized = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Email must not be blank");
        }
        return normalized;
    }

    private String normalizeFullName(String fullName) {
        String normalized = fullName == null ? "" : fullName.trim().replaceAll("\\s+", " ");
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Full name must not be blank");
        }
        return normalized;
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }

        boolean hasDigit = false;
        boolean hasLower = false;
        boolean hasUpper = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isDigit(c)) hasDigit = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isUpperCase(c)) hasUpper = true;
            else hasSpecial = true;
        }

        if (!hasDigit || !hasLower || !hasUpper || !hasSpecial) {
            throw new IllegalArgumentException("Password must contain at least one digit, one lowercase letter, one uppercase letter, and one special character");
        }
    }
}
