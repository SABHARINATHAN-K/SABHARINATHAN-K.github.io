package com.careerplanning.backend.modules.auth.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SimpleTokenService {

    private final Map<String, Long> tokenToUserId = new ConcurrentHashMap<>();

    public String createToken(Long userId) {
        String token = UUID.randomUUID().toString();
        tokenToUserId.put(token, userId);
        return token;
    }

    public Long getUserId(String token) {
        Long userId = tokenToUserId.get(token);
        if (userId == null) {
            throw new IllegalArgumentException("Invalid or expired token");
        }
        return userId;
    }
}
