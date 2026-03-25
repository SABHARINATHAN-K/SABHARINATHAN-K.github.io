package com.careerplanning.backend.modules.goals.dto;

import com.careerplanning.backend.modules.goals.entity.GoalCategory;
import com.careerplanning.backend.modules.goals.entity.GoalPriority;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record GoalStatsResponse(
        int total,
        int planned,
        int inProgress,
        int completed,
        List<CategoryCount> byCategory,
        List<PriorityCount> byPriority,
        int completionRate,
        int alignmentScore,
        List<String> gapWarnings,
        int goalsCompletedThisWeek,
        int goalsUpdatedThisWeek,
        int weeklyExecutionScore,
        int dueSoonTaskCount,
        int overdueTaskCount,
        List<DueTaskItem> dueSoonTasks,
        List<TimelineItem> timeline,
        NextBestAction nextBestAction
) {
    public record CategoryCount(GoalCategory category, int count) {}

    public record PriorityCount(GoalPriority priority, int count) {}

    public record DueTaskItem(Long goalId, String goalTitle, Long taskId, String taskTitle, LocalDate dueDate, boolean overdue) {}

    public record TimelineItem(String goalTitle, Instant createdDate, Instant completedDate) {}

    public record NextBestAction(Long goalId, String title, String phase) {}
}
