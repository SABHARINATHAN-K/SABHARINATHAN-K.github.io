package com.careerplanning.backend.modules.admin.dto;

import java.time.Instant;

public record AdminUserSummaryResponse(
        Long id,
        String fullName,
        String email,
        String role,
        String careerTrack,
        boolean onboardingCompleted,
        Instant joinedDate,
        int goalCount,
        int completedGoalCount,
        int taskCount,
        int completedTaskCount
) {
}
