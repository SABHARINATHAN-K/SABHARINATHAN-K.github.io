package com.careerplanning.backend.modules.users.service;

import com.careerplanning.backend.modules.auth.service.SimpleTokenService;
import com.careerplanning.backend.modules.users.dto.UserProfileResponse;
import com.careerplanning.backend.modules.users.entity.User;
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

        return new UserProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getCareerTrack()
        );
    }
}
