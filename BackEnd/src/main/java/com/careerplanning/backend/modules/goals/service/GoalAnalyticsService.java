package com.careerplanning.backend.modules.goals.service;

import com.careerplanning.backend.modules.goals.dto.GoalStatsResponse;
import com.careerplanning.backend.modules.goals.entity.Goal;
import com.careerplanning.backend.modules.goals.entity.GoalCategory;
import com.careerplanning.backend.modules.goals.entity.GoalPriority;
import com.careerplanning.backend.modules.goals.entity.GoalStatus;
import com.careerplanning.backend.modules.goals.entity.GoalTask;
import com.careerplanning.backend.modules.users.entity.CareerTrack;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GoalAnalyticsService {

    private static final Map<CareerTrack, Map<GoalCategory, Integer>> CATEGORY_REQUIREMENTS = buildRequirements();

    public GoalStatsResponse buildStats(List<Goal> goals, List<GoalTask> tasks, String careerTrackValue) {
        int total = goals.size();
        int planned = (int) goals.stream().filter(goal -> goal.getStatus() == GoalStatus.PLANNED).count();
        int inProgress = (int) goals.stream().filter(goal -> goal.getStatus() == GoalStatus.IN_PROGRESS).count();
        int completed = (int) goals.stream().filter(goal -> goal.getStatus() == GoalStatus.COMPLETED).count();
        int completionRate = total == 0 ? 0 : (int) Math.round((completed * 100.0) / total);

        List<GoalStatsResponse.CategoryCount> byCategory = new ArrayList<>();
        for (GoalCategory category : GoalCategory.values()) {
            int count = (int) goals.stream().filter(goal -> goal.getCategory() == category).count();
            byCategory.add(new GoalStatsResponse.CategoryCount(category, count));
        }

        List<GoalStatsResponse.PriorityCount> byPriority = new ArrayList<>();
        for (GoalPriority priority : GoalPriority.values()) {
            int count = (int) goals.stream().filter(goal -> goal.getPriority() == priority).count();
            byPriority.add(new GoalStatsResponse.PriorityCount(priority, count));
        }

        List<Goal> blueprintGoals = goals.stream().filter(Goal::isBlueprintGoal).toList();
        int totalBlueprintGoals = blueprintGoals.size();
        int completedBlueprintGoals = (int) blueprintGoals.stream()
                .filter(goal -> goal.getStatus() == GoalStatus.COMPLETED)
                .count();
        int alignmentScore = totalBlueprintGoals == 0
                ? 0
                : (int) Math.round((completedBlueprintGoals * 100.0) / totalBlueprintGoals);

        Instant weekStart = Instant.now().minus(7, ChronoUnit.DAYS);
        int goalsCompletedThisWeek = (int) goals.stream()
                .filter(goal -> goal.getCompletedDate() != null)
                .filter(goal -> !goal.getCompletedDate().isBefore(weekStart))
                .count();
        int goalsUpdatedThisWeek = (int) goals.stream()
                .filter(goal -> goal.getUpdatedAt() != null)
                .filter(goal -> !goal.getUpdatedAt().isBefore(weekStart))
                .count();
        int weeklyExecutionScore = total == 0
                ? 0
                : (int) Math.round((goalsCompletedThisWeek * 100.0) / total);

        List<GoalStatsResponse.TimelineItem> timeline = goals.stream()
                .sorted(Comparator.comparing(Goal::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(goal -> new GoalStatsResponse.TimelineItem(
                        goal.getTitle(),
                        goal.getCreatedAt(),
                        goal.getCompletedDate()
                ))
                .toList();

        GoalStatsResponse.NextBestAction nextBestAction = blueprintGoals.stream()
                .filter(goal -> goal.getStatus() != GoalStatus.COMPLETED)
                .sorted(Comparator
                        .comparing(Goal::getBlueprintPhaseOrder, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Goal::getBlueprintDefaultOrder, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Goal::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .findFirst()
                .map(goal -> new GoalStatsResponse.NextBestAction(
                        goal.getId(),
                        goal.getTitle(),
                        resolvePhaseLabel(goal)
                ))
                .orElse(null);

        List<String> gapWarnings = buildGapWarnings(goals, careerTrackValue);
        Map<Long, String> goalTitleById = goals.stream()
                .collect(Collectors.toMap(Goal::getId, Goal::getTitle, (left, ignored) -> left));

        LocalDate today = LocalDate.now();
        LocalDate dueSoonBoundary = today.plusDays(7);

        List<GoalTask> pendingTasksWithDueDate = tasks.stream()
                .filter(task -> !Boolean.TRUE.equals(task.getCompleted()))
                .filter(task -> task.getDueDate() != null)
                .toList();

        int overdueTaskCount = (int) pendingTasksWithDueDate.stream()
                .filter(task -> task.getDueDate().isBefore(today))
                .count();

        List<GoalTask> dueSoonTasks = pendingTasksWithDueDate.stream()
                .filter(task -> !task.getDueDate().isAfter(dueSoonBoundary))
                .sorted(Comparator
                        .comparing((GoalTask task) -> task.getDueDate().isBefore(today) ? 0 : 1)
                        .thenComparing(GoalTask::getDueDate)
                        .thenComparing(GoalTask::getSortOrder, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(GoalTask::getId))
                .toList();

        int dueSoonTaskCount = dueSoonTasks.size();
        List<GoalStatsResponse.DueTaskItem> dueSoonTaskItems = dueSoonTasks.stream()
                .limit(8)
                .map(task -> new GoalStatsResponse.DueTaskItem(
                        task.getGoalId(),
                        goalTitleById.getOrDefault(task.getGoalId(), "Goal"),
                        task.getId(),
                        task.getTitle(),
                        task.getDueDate(),
                        task.getDueDate().isBefore(today)
                ))
                .toList();

        return new GoalStatsResponse(
                total,
                planned,
                inProgress,
                completed,
                byCategory,
                byPriority,
                completionRate,
                alignmentScore,
                gapWarnings,
                goalsCompletedThisWeek,
                goalsUpdatedThisWeek,
                weeklyExecutionScore,
                dueSoonTaskCount,
                overdueTaskCount,
                dueSoonTaskItems,
                timeline,
                nextBestAction
        );
    }

    private List<String> buildGapWarnings(List<Goal> goals, String careerTrackValue) {
        if (careerTrackValue == null || careerTrackValue.isBlank() || !CareerTrack.isValid(careerTrackValue)) {
            return List.of();
        }

        CareerTrack careerTrack = CareerTrack.valueOf(careerTrackValue);
        Map<GoalCategory, Integer> requirements = CATEGORY_REQUIREMENTS.getOrDefault(careerTrack, Map.of());

        Map<GoalCategory, Long> goalCounts = new EnumMap<>(GoalCategory.class);
        for (GoalCategory category : GoalCategory.values()) {
            long count = goals.stream().filter(goal -> goal.getCategory() == category).count();
            goalCounts.put(category, count);
        }

        List<String> warnings = new ArrayList<>();
        for (Map.Entry<GoalCategory, Integer> requirement : requirements.entrySet()) {
            GoalCategory category = requirement.getKey();
            int minimum = requirement.getValue();
            int actual = goalCounts.getOrDefault(category, 0L).intValue();

            if (actual < minimum) {
                int missing = minimum - actual;
                warnings.add("Add " + missing + " more " + humanize(category.name())
                        + " goal(s) to meet " + humanize(careerTrack.name()) + " baseline.");
            }
        }

        return warnings;
    }

    private String resolvePhaseLabel(Goal goal) {
        if (goal.getBlueprintPhaseTitle() != null && !goal.getBlueprintPhaseTitle().isBlank()) {
            return goal.getBlueprintPhaseTitle();
        }
        if (goal.getBlueprintPhaseOrder() != null) {
            return "Phase " + goal.getBlueprintPhaseOrder();
        }
        return "Blueprint";
    }

    private static Map<CareerTrack, Map<GoalCategory, Integer>> buildRequirements() {
        Map<CareerTrack, Map<GoalCategory, Integer>> requirements = new EnumMap<>(CareerTrack.class);

        requirements.put(CareerTrack.SOFTWARE_ENGINEERING, Map.of(
                GoalCategory.SKILL_DEVELOPMENT, 3,
                GoalCategory.PROJECT, 2,
                GoalCategory.CERTIFICATION, 1
        ));
        requirements.put(CareerTrack.JAVA_BACKEND_DEVELOPER, Map.of(
                GoalCategory.SKILL_DEVELOPMENT, 3,
                GoalCategory.PROJECT, 2,
                GoalCategory.CERTIFICATION, 1
        ));
        requirements.put(CareerTrack.FRONTEND_DEVELOPER, Map.of(
                GoalCategory.SKILL_DEVELOPMENT, 3,
                GoalCategory.PROJECT, 2,
                GoalCategory.CERTIFICATION, 1
        ));
        requirements.put(CareerTrack.FULL_STACK_DEVELOPER, Map.of(
                GoalCategory.SKILL_DEVELOPMENT, 3,
                GoalCategory.PROJECT, 2,
                GoalCategory.CERTIFICATION, 1
        ));
        requirements.put(CareerTrack.DATA_SCIENCE, Map.of(
                GoalCategory.SKILL_DEVELOPMENT, 2,
                GoalCategory.PROJECT, 2,
                GoalCategory.CERTIFICATION, 1
        ));
        requirements.put(CareerTrack.DATA_SCIENTIST, Map.of(
                GoalCategory.SKILL_DEVELOPMENT, 2,
                GoalCategory.PROJECT, 2,
                GoalCategory.CERTIFICATION, 1
        ));
        requirements.put(CareerTrack.DATA_ANALYST, Map.of(
                GoalCategory.SKILL_DEVELOPMENT, 2,
                GoalCategory.PROJECT, 2,
                GoalCategory.CERTIFICATION, 1
        ));
        requirements.put(CareerTrack.AI_ML_ENGINEER, Map.of(
                GoalCategory.SKILL_DEVELOPMENT, 2,
                GoalCategory.PROJECT, 2,
                GoalCategory.CERTIFICATION, 1
        ));
        requirements.put(CareerTrack.PRODUCT_MANAGEMENT, Map.of(
                GoalCategory.CAREER_GROWTH, 2,
                GoalCategory.PROJECT, 2,
                GoalCategory.NETWORKING, 1
        ));
        requirements.put(CareerTrack.PRODUCT_MANAGER, Map.of(
                GoalCategory.CAREER_GROWTH, 2,
                GoalCategory.PROJECT, 2,
                GoalCategory.NETWORKING, 1
        ));
        requirements.put(CareerTrack.DESIGN, Map.of(
                GoalCategory.SKILL_DEVELOPMENT, 2,
                GoalCategory.PROJECT, 2,
                GoalCategory.CAREER_GROWTH, 1
        ));
        requirements.put(CareerTrack.UI_UX_DESIGNER, Map.of(
                GoalCategory.SKILL_DEVELOPMENT, 2,
                GoalCategory.PROJECT, 2,
                GoalCategory.CAREER_GROWTH, 1
        ));
        requirements.put(CareerTrack.MARKETING, Map.of(
                GoalCategory.PROJECT, 2,
                GoalCategory.CAREER_GROWTH, 2,
                GoalCategory.CERTIFICATION, 1
        ));
        requirements.put(CareerTrack.BUSINESS_ANALYST, Map.of(
                GoalCategory.SKILL_DEVELOPMENT, 2,
                GoalCategory.PROJECT, 2,
                GoalCategory.CAREER_GROWTH, 1
        ));
        requirements.put(CareerTrack.DEVOPS_ENGINEER, Map.of(
                GoalCategory.SKILL_DEVELOPMENT, 2,
                GoalCategory.PROJECT, 2,
                GoalCategory.CERTIFICATION, 1
        ));
        requirements.put(CareerTrack.QA_ENGINEER, Map.of(
                GoalCategory.SKILL_DEVELOPMENT, 2,
                GoalCategory.PROJECT, 2,
                GoalCategory.CERTIFICATION, 1
        ));
        requirements.put(CareerTrack.CYBERSECURITY_ANALYST, Map.of(
                GoalCategory.SKILL_DEVELOPMENT, 2,
                GoalCategory.PROJECT, 2,
                GoalCategory.CERTIFICATION, 2
        ));

        return requirements;
    }

    private static String humanize(String value) {
        return value.toLowerCase().replace('_', ' ');
    }
}
