package com.careerplanning.backend.modules.users.entity;

import java.util.Arrays;
import java.util.List;

public enum CareerTrack {
    JAVA_BACKEND_DEVELOPER,
    FRONTEND_DEVELOPER,
    FULL_STACK_DEVELOPER,
    DATA_ANALYST,
    DATA_SCIENTIST,
    DEVOPS_ENGINEER,
    QA_ENGINEER,
    UI_UX_DESIGNER,
    PRODUCT_MANAGER,
    CYBERSECURITY_ANALYST;

    public static boolean isValid(String value) {
        if (value == null) {
            return false;
        }
        return Arrays.stream(values()).anyMatch(track -> track.name().equals(value));
    }

    public static List<String> options() {
        return Arrays.stream(values()).map(Enum::name).toList();
    }
}
