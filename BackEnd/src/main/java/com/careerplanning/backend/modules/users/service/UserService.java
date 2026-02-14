package com.careerplanning.backend.modules.users.service;

import com.careerplanning.backend.modules.auth.service.SimpleTokenService;
import com.careerplanning.backend.modules.users.dto.UpdateUserProfileRequest;
import com.careerplanning.backend.modules.users.dto.UserProfileResponse;
import com.careerplanning.backend.modules.users.entity.CareerTrack;
import com.careerplanning.backend.modules.users.entity.User;
import com.careerplanning.backend.modules.users.entity.UserRole;
import com.careerplanning.backend.modules.users.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final SimpleTokenService simpleTokenService;

    public UserService(UserRepository userRepository, SimpleTokenService simpleTokenService) {
        this.userRepository = userRepository;
        this.simpleTokenService = simpleTokenService;
    }

    public UserProfileResponse getCurrentUserProfile(String token) {
        Long userId = simpleTokenService.getUserId(token);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return toProfileResponse(user);
    }

    public UserProfileResponse updateCurrentUserProfile(String token, UpdateUserProfileRequest request) {
        Long userId = simpleTokenService.getUserId(token);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String fullName = request.fullName() == null ? "" : request.fullName().trim();
        if (fullName.isBlank()) {
            throw new IllegalArgumentException("fullName must not be blank");
        }

        user.setFullName(fullName);
        user.setBio(toNullableTrimmed(request.bio()));
        user.setLocation(toNullableTrimmed(request.location()));
        user.setRole(validateRole(request.role()));
        user.setCareerTrack(validateCareerTrack(request.careerTrack()));

        User saved = userRepository.save(user);
        return toProfileResponse(saved);
    }

    private String toNullableTrimmed(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
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

    private UserProfileResponse toProfileResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getCareerTrack(),
                user.getBio(),
                user.getLocation(),
                user.getCreatedAt()
        );
    }
}
