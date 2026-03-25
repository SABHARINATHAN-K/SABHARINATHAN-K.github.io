package com.careerplanning.backend.modules.users.entity;

import java.util.Arrays;
import java.util.List;

public enum UserRole {
    ADMIN,
    STUDENT,
    PROFESSIONAL,
    MANAGER,
    EXECUTIVE,
    FRESH_GRADUATE,
    WORKING_PROFESSIONAL,
    CAREER_SWITCHER;

    public static boolean isValid(String value) {
        if (value == null) {
            return false;
        }
        return Arrays.stream(values()).anyMatch(role -> role.name().equals(value));
    }

    public static boolean isSelfServiceRole(String value) {
        if (!isValid(value)) {
            return false;
        }
        return !ADMIN.name().equals(value);
    }

    public static List<String> options() {
        return Arrays.stream(values()).map(Enum::name).toList();
    }

    public static List<String> selfServiceOptions() {
        return Arrays.stream(values())
                .filter(role -> role != ADMIN)
                .map(Enum::name)
                .toList();
    }
}
