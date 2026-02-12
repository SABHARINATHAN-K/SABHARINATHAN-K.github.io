package com.careerplanning.backend.modules.users.entity;

import java.util.Arrays;
import java.util.List;

public enum UserRole {
    STUDENT,
    FRESH_GRADUATE,
    WORKING_PROFESSIONAL,
    CAREER_SWITCHER;

    public static boolean isValid(String value) {
        if (value == null) {
            return false;
        }
        return Arrays.stream(values()).anyMatch(role -> role.name().equals(value));
    }

    public static List<String> options() {
        return Arrays.stream(values()).map(Enum::name).toList();
    }
}
