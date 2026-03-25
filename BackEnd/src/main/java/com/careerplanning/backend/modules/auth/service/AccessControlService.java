package com.careerplanning.backend.modules.auth.service;

import com.careerplanning.backend.common.exception.AccessDeniedException;
import com.careerplanning.backend.modules.users.entity.User;
import com.careerplanning.backend.modules.users.entity.UserRole;
import com.careerplanning.backend.modules.users.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AccessControlService {

    private final SimpleTokenService simpleTokenService;
    private final UserRepository userRepository;

    public AccessControlService(SimpleTokenService simpleTokenService, UserRepository userRepository) {
        this.simpleTokenService = simpleTokenService;
        this.userRepository = userRepository;
    }

    public User getAuthenticatedUser(String token) {
        Long userId = simpleTokenService.getUserId(token);
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public User requireAdmin(String token) {
        User user = getAuthenticatedUser(token);
        ensureAdmin(user);
        return user;
    }

    public void ensureAdmin(User user) {
        if (user == null || !UserRole.ADMIN.name().equals(user.getRole())) {
            throw new AccessDeniedException("Admin access required");
        }
    }
}
