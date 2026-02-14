package com.careerplanning.backend.modules.goals.dto;

import com.careerplanning.backend.modules.goals.entity.GoalCategory;
import com.careerplanning.backend.modules.goals.entity.GoalPriority;

import java.util.List;

public record GoalStatsResponse(
        int total,
        int planned,
        int inProgress,
        int completed,
        List<CategoryCount> byCategory,
        List<PriorityCount> byPriority,
        int completionRate
) {
    public record CategoryCount(GoalCategory category, int count) {}

    public record PriorityCount(GoalPriority priority, int count) {}
}
