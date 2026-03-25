package com.careerplanning.backend.modules.goals.service;

import com.careerplanning.backend.modules.goals.dto.GoalStatsResponse;
import com.careerplanning.backend.modules.goals.entity.Goal;
import com.careerplanning.backend.modules.goals.entity.GoalCategory;
import com.careerplanning.backend.modules.goals.entity.GoalStatus;
import com.careerplanning.backend.modules.goals.entity.GoalTask;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GoalAnalyticsServiceTest {

    private final GoalAnalyticsService goalAnalyticsService = new GoalAnalyticsService();

    @Test
    void calculatesAlignmentScoreUsingOnlyBlueprintGoals() {
        Goal completedBlueprintGoal = goal(1L, "API milestone", GoalStatus.COMPLETED, GoalCategory.PROJECT,
                true, 1, 1, "Foundation", Instant.now().minus(2, ChronoUnit.DAYS), Instant.now().minus(1, ChronoUnit.DAYS));
        completedBlueprintGoal.setCompletedDate(Instant.now().minus(1, ChronoUnit.DAYS));

        Goal pendingBlueprintGoal = goal(2L, "System design practice", GoalStatus.PLANNED, GoalCategory.SKILL_DEVELOPMENT,
                true, 2, 1, "Execution", Instant.now().minus(1, ChronoUnit.DAYS), Instant.now());

        Goal manualGoal = goal(3L, "Networking meetup", GoalStatus.COMPLETED, GoalCategory.NETWORKING,
                false, null, null, null, Instant.now().minus(5, ChronoUnit.DAYS), Instant.now().minus(2, ChronoUnit.DAYS));
        manualGoal.setCompletedDate(Instant.now().minus(2, ChronoUnit.DAYS));

        GoalStatsResponse stats = goalAnalyticsService.buildStats(
                List.of(completedBlueprintGoal, pendingBlueprintGoal, manualGoal),
                List.of(),
                "SOFTWARE_ENGINEERING"
        );

        assertEquals(50, stats.alignmentScore());
        assertNotNull(stats.nextBestAction());
        assertEquals(2L, stats.nextBestAction().goalId());
        assertEquals("System design practice", stats.nextBestAction().title());
    }

    @Test
    void buildsGapWarningsFromCareerRequirements() {
        Goal skillGoal = goal(10L, "Improve Java", GoalStatus.PLANNED, GoalCategory.SKILL_DEVELOPMENT,
                false, null, null, null, Instant.now(), Instant.now());

        GoalStatsResponse stats = goalAnalyticsService.buildStats(
                List.of(skillGoal),
                List.of(),
                "SOFTWARE_ENGINEERING"
        );

        assertEquals(3, stats.gapWarnings().size());
        assertTrue(stats.gapWarnings().stream().anyMatch(value -> value.contains("skill development")));
        assertTrue(stats.gapWarnings().stream().anyMatch(value -> value.contains("project")));
        assertTrue(stats.gapWarnings().stream().anyMatch(value -> value.contains("certification")));
    }

    @Test
    void computesWeeklyExecutionScoreFromLastSevenDays() {
        Goal recentlyCompleted = goal(21L, "Recent completion", GoalStatus.COMPLETED, GoalCategory.PROJECT,
                false, null, null, null, Instant.now().minus(4, ChronoUnit.DAYS), Instant.now().minus(2, ChronoUnit.DAYS));
        recentlyCompleted.setCompletedDate(Instant.now().minus(2, ChronoUnit.DAYS));

        Goal oldCompletion = goal(22L, "Old completion", GoalStatus.COMPLETED, GoalCategory.PROJECT,
                false, null, null, null, Instant.now().minus(20, ChronoUnit.DAYS), Instant.now().minus(15, ChronoUnit.DAYS));
        oldCompletion.setCompletedDate(Instant.now().minus(15, ChronoUnit.DAYS));

        Goal activeGoal = goal(23L, "In progress", GoalStatus.IN_PROGRESS, GoalCategory.SKILL_DEVELOPMENT,
                false, null, null, null, Instant.now().minus(1, ChronoUnit.DAYS), Instant.now());

        GoalStatsResponse stats = goalAnalyticsService.buildStats(
                List.of(recentlyCompleted, oldCompletion, activeGoal),
                List.of(),
                "SOFTWARE_ENGINEERING"
        );

        assertEquals(1, stats.goalsCompletedThisWeek());
        assertEquals(2, stats.goalsUpdatedThisWeek());
        assertEquals(33, stats.weeklyExecutionScore());
    }

    @Test
    void computesDueSoonAndOverdueTasks() {
        Goal goal = goal(31L, "Ship backend project", GoalStatus.IN_PROGRESS, GoalCategory.PROJECT,
                false, null, null, null, Instant.now().minus(2, ChronoUnit.DAYS), Instant.now());

        GoalTask overdueTask = task(501L, 31L, "Fix blocker", LocalDate.now().minusDays(1), false, 1);
        GoalTask dueSoonTask = task(502L, 31L, "Write integration tests", LocalDate.now().plusDays(3), false, 2);
        GoalTask futureTask = task(503L, 31L, "Optimize query plan", LocalDate.now().plusDays(12), false, 3);
        GoalTask completedTask = task(504L, 31L, "Scope finalized", LocalDate.now().plusDays(2), true, 0);

        GoalStatsResponse stats = goalAnalyticsService.buildStats(
                List.of(goal),
                List.of(overdueTask, dueSoonTask, futureTask, completedTask),
                "SOFTWARE_ENGINEERING"
        );

        assertEquals(2, stats.dueSoonTaskCount());
        assertEquals(1, stats.overdueTaskCount());
        assertEquals(2, stats.dueSoonTasks().size());
        assertTrue(stats.dueSoonTasks().stream().anyMatch(item -> item.overdue()));
    }

    private Goal goal(Long id,
                      String title,
                      GoalStatus status,
                      GoalCategory category,
                      boolean blueprint,
                      Integer phaseOrder,
                      Integer defaultOrder,
                      String phaseTitle,
                      Instant createdAt,
                      Instant updatedAt) {
        Goal goal = new Goal();
        goal.setUserId(99L);
        goal.setTitle(title);
        goal.setStatus(status);
        goal.setCategory(category);
        goal.setBlueprintGoal(blueprint);
        goal.setBlueprintPhaseOrder(phaseOrder);
        goal.setBlueprintDefaultOrder(defaultOrder);
        goal.setBlueprintPhaseTitle(phaseTitle);
        goal.setProgress(status == GoalStatus.COMPLETED ? 100 : 0);

        ReflectionTestUtils.setField(goal, "id", id);
        ReflectionTestUtils.setField(goal, "createdAt", createdAt);
        ReflectionTestUtils.setField(goal, "updatedAt", updatedAt);
        return goal;
    }

    private GoalTask task(Long id, Long goalId, String title, LocalDate dueDate, boolean completed, int sortOrder) {
        GoalTask task = new GoalTask();
        task.setGoalId(goalId);
        task.setTitle(title);
        task.setDueDate(dueDate);
        task.setCompleted(completed);
        task.setSortOrder(sortOrder);
        ReflectionTestUtils.setField(task, "id", id);
        return task;
    }
}
