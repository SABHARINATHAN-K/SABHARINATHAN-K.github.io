package com.careerplanning.backend.modules.goals.service;

import com.careerplanning.backend.modules.auth.service.SimpleTokenService;
import com.careerplanning.backend.modules.goals.dto.CreateGoalRequest;
import com.careerplanning.backend.modules.goals.dto.CreateGoalTaskRequest;
import com.careerplanning.backend.modules.goals.dto.GoalResponse;
import com.careerplanning.backend.modules.goals.dto.GoalStatsResponse;
import com.careerplanning.backend.modules.goals.dto.GoalTaskResponse;
import com.careerplanning.backend.modules.goals.dto.UpdateGoalRequest;
import com.careerplanning.backend.modules.goals.dto.UpdateGoalTaskRequest;
import com.careerplanning.backend.modules.goals.entity.Goal;
import com.careerplanning.backend.modules.goals.entity.GoalCategory;
import com.careerplanning.backend.modules.goals.entity.GoalPriority;
import com.careerplanning.backend.modules.goals.entity.GoalStatus;
import com.careerplanning.backend.modules.goals.entity.GoalTask;
import com.careerplanning.backend.modules.goals.repository.GoalRepository;
import com.careerplanning.backend.modules.goals.repository.GoalTaskRepository;
import com.careerplanning.backend.modules.users.entity.User;
import com.careerplanning.backend.modules.users.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class GoalService {

    private static final int MIN_TASKS_PER_GOAL = 5;

    private final GoalRepository goalRepository;
    private final GoalTaskRepository goalTaskRepository;
    private final GoalTaskPlannerService goalTaskPlannerService;
    private final SimpleTokenService simpleTokenService;
    private final UserRepository userRepository;
    private final GoalAnalyticsService goalAnalyticsService;

    public GoalService(GoalRepository goalRepository,
                       GoalTaskRepository goalTaskRepository,
                       GoalTaskPlannerService goalTaskPlannerService,
                       SimpleTokenService simpleTokenService,
                       UserRepository userRepository,
                       GoalAnalyticsService goalAnalyticsService) {
        this.goalRepository = goalRepository;
        this.goalTaskRepository = goalTaskRepository;
        this.goalTaskPlannerService = goalTaskPlannerService;
        this.simpleTokenService = simpleTokenService;
        this.userRepository = userRepository;
        this.goalAnalyticsService = goalAnalyticsService;
    }

    @Transactional
    public GoalResponse createGoal(String token, CreateGoalRequest request) {
        return createGoalForUser(simpleTokenService.getUserId(token), request);
    }

    @Transactional
    public GoalResponse createGoalForUser(Long userId, CreateGoalRequest request) {
        String normalizedTitle = normalizeRequiredTitle(request.title());

        Goal goal = new Goal();
        goal.setUserId(userId);
        goal.setTitle(normalizedTitle);
        goal.setDescription(request.description());
        goal.setPriority(request.priority() == null ? GoalPriority.MEDIUM : request.priority());
        goal.setCategory(request.category() == null ? GoalCategory.LEARNING : request.category());
        goal.setTargetDate(request.targetDate());
        goal.setProgress(0);
        goal.setTags(normalizeTags(request.tags()));

        Goal saved = goalRepository.save(goal);
        List<GoalTask> tasks = ensureTasksForGoal(saved);
        return toResponse(saved, tasks);
    }

    @Transactional
    public List<GoalResponse> listGoals(String token) {
        return listGoalsForUser(simpleTokenService.getUserId(token));
    }

    @Transactional
    public List<GoalResponse> listGoalsForUser(Long userId) {
        List<Goal> goals = goalRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return goals.stream()
                .map(goal -> toResponse(goal, ensureTasksForGoal(goal)))
                .toList();
    }

    @Transactional
    public GoalResponse getGoalById(String token, Long goalId) {
        return getGoalByIdForUser(simpleTokenService.getUserId(token), goalId);
    }

    @Transactional
    public GoalResponse getGoalByIdForUser(Long userId, Long goalId) {
        Goal goal = getOwnedGoal(userId, goalId);
        List<GoalTask> tasks = ensureTasksForGoal(goal);
        return toResponse(goal, tasks);
    }

    @Transactional
    public GoalStatsResponse getGoalStats(String token) {
        return getGoalStatsForUser(simpleTokenService.getUserId(token));
    }

    @Transactional
    public GoalStatsResponse getGoalStatsForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Goal> goals = goalRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<GoalTask> allTasks = new ArrayList<>();
        for (Goal goal : goals) {
            allTasks.addAll(ensureTasksForGoal(goal));
        }

        return goalAnalyticsService.buildStats(goals, allTasks, user.getCareerTrack());
    }

    @Transactional
    public GoalResponse updateGoal(String token, Long goalId, UpdateGoalRequest request) {
        return updateGoalForUser(simpleTokenService.getUserId(token), goalId, request);
    }

    @Transactional
    public GoalResponse updateGoalForUser(Long userId, Long goalId, UpdateGoalRequest request) {
        Goal goal = getOwnedGoal(userId, goalId);
        List<GoalTask> tasks = ensureTasksForGoal(goal);

        if (request.title() != null) {
            goal.setTitle(normalizeRequiredTitle(request.title()));
        }
        if (request.description() != null) {
            goal.setDescription(request.description());
        }
        if (request.priority() != null) {
            goal.setPriority(request.priority());
        }
        if (request.category() != null) {
            goal.setCategory(request.category());
        }
        if (request.targetDate() != null) {
            goal.setTargetDate(request.targetDate());
            goalTaskPlannerService.rescheduleTasks(goal, tasks);
            goalTaskRepository.saveAll(tasks);
        }
        if (request.notes() != null) {
            goal.setNotes(request.notes());
        }
        if (request.tags() != null) {
            goal.setTags(normalizeTags(request.tags()));
        }

        if (request.progress() != null) {
            applyProgressToTasks(tasks, normalizeProgress(request.progress()), Instant.now());
            goalTaskRepository.saveAll(tasks);
        }

        if (request.status() != null) {
            if (request.status() == GoalStatus.COMPLETED) {
                applyProgressToTasks(tasks, 100, Instant.now());
                goalTaskRepository.saveAll(tasks);
            } else if (request.status() == GoalStatus.PLANNED) {
                applyProgressToTasks(tasks, 0, Instant.now());
                goalTaskRepository.saveAll(tasks);
            }
        }

        goalRepository.save(goal);
        syncGoalFromTasks(goal, tasks, true);
        return toResponse(goal, tasks);
    }

    @Transactional
    public List<GoalTaskResponse> listGoalTasks(String token, Long goalId) {
        return listGoalTasksForUser(simpleTokenService.getUserId(token), goalId);
    }

    @Transactional
    public List<GoalTaskResponse> listGoalTasksForUser(Long userId, Long goalId) {
        Goal goal = getOwnedGoal(userId, goalId);
        List<GoalTask> tasks = ensureTasksForGoal(goal);
        return mapTasks(tasks);
    }

    @Transactional
    public GoalResponse createGoalTask(String token, Long goalId, CreateGoalTaskRequest request) {
        return createGoalTaskForUser(simpleTokenService.getUserId(token), goalId, request);
    }

    @Transactional
    public GoalResponse createGoalTaskForUser(Long userId, Long goalId, CreateGoalTaskRequest request) {
        Goal goal = getOwnedGoal(userId, goalId);
        List<GoalTask> existing = ensureTasksForGoal(goal);

        GoalTask task = new GoalTask();
        task.setGoalId(goal.getId());
        task.setTitle(normalizeTaskTitle(request.title()));
        task.setDetails(normalizeOptionalText(request.details(), 2000));
        task.setWeight(normalizeTaskWeight(request.weight()));
        task.setSortOrder(normalizeSortOrder(request.sortOrder(), existing));
        task.setDueDate(request.dueDate() == null ? inferNextDueDate(existing) : request.dueDate());
        task.setCompleted(false);

        goalTaskRepository.save(task);

        List<GoalTask> tasks = goalTaskRepository.findByGoalIdOrderBySortOrderAscIdAsc(goal.getId());
        syncGoalFromTasks(goal, tasks, true);
        return toResponse(goal, tasks);
    }

    @Transactional
    public GoalResponse updateGoalTask(String token, Long goalId, Long taskId, UpdateGoalTaskRequest request) {
        return updateGoalTaskForUser(simpleTokenService.getUserId(token), goalId, taskId, request);
    }

    @Transactional
    public GoalResponse updateGoalTaskForUser(Long userId, Long goalId, Long taskId, UpdateGoalTaskRequest request) {
        Goal goal = getOwnedGoal(userId, goalId);
        ensureTasksForGoal(goal);

        GoalTask task = goalTaskRepository.findByIdAndGoalId(taskId, goal.getId())
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (request.title() != null) {
            task.setTitle(normalizeTaskTitle(request.title()));
        }
        if (request.details() != null) {
            task.setDetails(normalizeOptionalText(request.details(), 2000));
        }
        if (request.dueDate() != null) {
            task.setDueDate(request.dueDate());
        }
        if (request.weight() != null) {
            task.setWeight(normalizeTaskWeight(request.weight()));
        }
        if (request.sortOrder() != null) {
            task.setSortOrder(normalizeSortOrder(request.sortOrder(), null));
        }
        if (request.completed() != null) {
            boolean completed = request.completed();
            task.setCompleted(completed);
            task.setCompletedAt(completed ? Instant.now() : null);
        }

        goalTaskRepository.save(task);

        List<GoalTask> tasks = goalTaskRepository.findByGoalIdOrderBySortOrderAscIdAsc(goal.getId());
        syncGoalFromTasks(goal, tasks, true);
        return toResponse(goal, tasks);
    }

    @Transactional
    public GoalResponse deleteGoalTask(String token, Long goalId, Long taskId) {
        return deleteGoalTaskForUser(simpleTokenService.getUserId(token), goalId, taskId);
    }

    @Transactional
    public GoalResponse deleteGoalTaskForUser(Long userId, Long goalId, Long taskId) {
        Goal goal = getOwnedGoal(userId, goalId);
        List<GoalTask> tasks = ensureTasksForGoal(goal);

        if (tasks.size() <= MIN_TASKS_PER_GOAL) {
            throw new IllegalArgumentException("Goal must keep at least " + MIN_TASKS_PER_GOAL + " milestone tasks.");
        }

        GoalTask task = goalTaskRepository.findByIdAndGoalId(taskId, goal.getId())
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        goalTaskRepository.delete(task);

        List<GoalTask> remaining = goalTaskRepository.findByGoalIdOrderBySortOrderAscIdAsc(goal.getId());
        syncGoalFromTasks(goal, remaining, true);
        return toResponse(goal, remaining);
    }

    @Transactional
    public void deleteGoal(String token, Long goalId) {
        deleteGoalForUser(simpleTokenService.getUserId(token), goalId);
    }

    @Transactional
    public void deleteGoalForUser(Long userId, Long goalId) {
        Goal goal = getOwnedGoal(userId, goalId);
        goalRepository.delete(goal);
    }

    private Goal getOwnedGoal(Long userId, Long goalId) {
        return goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found"));
    }

    private List<GoalTask> ensureTasksForGoal(Goal goal) {
        List<GoalTask> tasks = goalTaskRepository.findByGoalIdOrderBySortOrderAscIdAsc(goal.getId());
        if (tasks.isEmpty()) {
            List<GoalTask> generated = goalTaskPlannerService.buildInitialTasks(goal);
            applyLegacyProgressToTasks(goal, generated);
            tasks = goalTaskRepository.saveAll(generated);
            tasks = goalTaskRepository.findByGoalIdOrderBySortOrderAscIdAsc(goal.getId());
        }

        syncGoalFromTasks(goal, tasks, true);
        return tasks;
    }

    private void applyLegacyProgressToTasks(Goal goal, List<GoalTask> tasks) {
        if (goal.getStatus() == GoalStatus.COMPLETED || normalizeProgress(goal.getProgress()) >= 100) {
            Instant completedAt = goal.getCompletedDate() == null ? Instant.now() : goal.getCompletedDate();
            tasks.forEach(task -> {
                task.setCompleted(true);
                task.setCompletedAt(completedAt);
            });
            return;
        }

        int legacyProgress = normalizeProgress(goal.getProgress());
        if (legacyProgress > 0) {
            applyProgressToTasks(tasks, legacyProgress, goal.getUpdatedAt());
        }
    }

    private void applyProgressToTasks(List<GoalTask> tasks, int progress, Instant timestamp) {
        if (tasks == null || tasks.isEmpty()) {
            return;
        }

        int normalizedProgress = normalizeProgress(progress);
        Instant referenceTime = timestamp == null ? Instant.now() : timestamp;

        List<GoalTask> ordered = tasks.stream()
                .sorted(Comparator.comparing(GoalTask::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(GoalTask::getId, Comparator.nullsLast(Long::compareTo)))
                .toList();

        if (normalizedProgress == 0) {
            ordered.forEach(task -> {
                task.setCompleted(false);
                task.setCompletedAt(null);
            });
            return;
        }

        if (normalizedProgress >= 100) {
            ordered.forEach(task -> {
                task.setCompleted(true);
                task.setCompletedAt(referenceTime);
            });
            return;
        }

        int totalWeight = ordered.stream().mapToInt(task -> Math.max(1, safeInt(task.getWeight(), 1))).sum();
        int targetCompletedWeight = (int) Math.round((totalWeight * normalizedProgress) / 100.0);

        int cumulative = 0;
        int completedCount = 0;
        for (GoalTask task : ordered) {
            int weight = Math.max(1, safeInt(task.getWeight(), 1));
            boolean shouldComplete = cumulative + weight <= targetCompletedWeight;
            if (shouldComplete) {
                cumulative += weight;
                completedCount++;
            }
            task.setCompleted(shouldComplete);
            task.setCompletedAt(shouldComplete ? referenceTime : null);
        }

        if (completedCount == 0 && normalizedProgress > 0 && !ordered.isEmpty()) {
            GoalTask first = ordered.get(0);
            first.setCompleted(true);
            first.setCompletedAt(referenceTime);
        }
    }

    private void syncGoalFromTasks(Goal goal, List<GoalTask> tasks, boolean saveGoal) {
        if (tasks == null || tasks.isEmpty()) {
            return;
        }

        int totalWeight = tasks.stream().mapToInt(task -> Math.max(1, safeInt(task.getWeight(), 1))).sum();
        int completedWeight = tasks.stream()
                .filter(task -> Boolean.TRUE.equals(task.getCompleted()))
                .mapToInt(task -> Math.max(1, safeInt(task.getWeight(), 1)))
                .sum();

        int nextProgress = totalWeight == 0 ? 0 : (int) Math.round((completedWeight * 100.0) / totalWeight);
        GoalStatus nextStatus;
        if (completedWeight <= 0) {
            nextStatus = GoalStatus.PLANNED;
        } else if (completedWeight >= totalWeight) {
            nextStatus = GoalStatus.COMPLETED;
        } else {
            nextStatus = GoalStatus.IN_PROGRESS;
        }

        Instant nextCompletedDate = nextStatus == GoalStatus.COMPLETED
                ? (goal.getCompletedDate() == null ? Instant.now() : goal.getCompletedDate())
                : null;

        boolean changed = !Objects.equals(goal.getProgress(), nextProgress)
                || goal.getStatus() != nextStatus
                || !Objects.equals(goal.getCompletedDate(), nextCompletedDate);

        goal.setProgress(nextProgress);
        goal.setStatus(nextStatus);
        goal.setCompletedDate(nextCompletedDate);

        if (changed && saveGoal) {
            goalRepository.save(goal);
        }
    }

    private int normalizeSortOrder(Integer input, List<GoalTask> existingTasks) {
        if (input != null) {
            if (input < 1 || input > 99) {
                throw new IllegalArgumentException("sortOrder must be between 1 and 99");
            }
            return input;
        }

        if (existingTasks == null || existingTasks.isEmpty()) {
            return 1;
        }

        int max = existingTasks.stream()
                .map(GoalTask::getSortOrder)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0);
        return max + 1;
    }

    private int normalizeTaskWeight(Integer weight) {
        int value = weight == null ? 20 : weight;
        if (value < 5 || value > 40) {
            throw new IllegalArgumentException("task weight must be between 5 and 40");
        }
        return value;
    }

    private int normalizeProgress(Integer progress) {
        int value = progress == null ? 0 : progress;
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException("progress must be between 0 and 100");
        }
        return value;
    }

    private String normalizeRequiredTitle(String rawTitle) {
        String title = rawTitle == null ? "" : rawTitle.trim();
        if (title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        return title;
    }

    private String normalizeTaskTitle(String rawTitle) {
        String title = rawTitle == null ? "" : rawTitle.trim();
        if (title.isBlank()) {
            throw new IllegalArgumentException("task title must not be blank");
        }
        if (title.length() > 320) {
            throw new IllegalArgumentException("task title must be at most 320 characters");
        }
        return title;
    }

    private String normalizeOptionalText(String rawText, int maxLength) {
        if (rawText == null) {
            return null;
        }
        String value = rawText.trim();
        if (value.isEmpty()) {
            return null;
        }
        if (value.length() > maxLength) {
            throw new IllegalArgumentException("Text exceeds maximum length of " + maxLength);
        }
        return value;
    }

    private java.time.LocalDate inferNextDueDate(List<GoalTask> existingTasks) {
        java.time.LocalDate base = existingTasks.stream()
                .map(GoalTask::getDueDate)
                .filter(Objects::nonNull)
                .max(java.time.LocalDate::compareTo)
                .orElse(java.time.LocalDate.now());
        return base.plusDays(7);
    }

    private List<String> normalizeTags(List<String> rawTags) {
        if (rawTags == null) {
            return new ArrayList<>();
        }

        List<String> normalized = new ArrayList<>();
        for (String tag : rawTags) {
            if (tag == null) {
                continue;
            }
            String trimmed = tag.trim();
            if (!trimmed.isEmpty()) {
                normalized.add(trimmed);
            }
        }
        return normalized;
    }

    private int safeInt(Integer value, int fallback) {
        return value == null ? fallback : value;
    }

    private List<GoalTaskResponse> mapTasks(List<GoalTask> tasks) {
        return tasks.stream()
                .sorted(Comparator.comparing(GoalTask::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(GoalTask::getId, Comparator.nullsLast(Long::compareTo)))
                .map(task -> new GoalTaskResponse(
                        task.getId(),
                        task.getTitle(),
                        task.getDetails(),
                        task.getDueDate(),
                        task.getWeight(),
                        task.getSortOrder(),
                        task.getCompleted(),
                        task.getCompletedAt()
                ))
                .toList();
    }

    private GoalResponse toResponse(Goal goal, List<GoalTask> tasks) {
        return new GoalResponse(
                goal.getId(),
                goal.getUserId(),
                goal.getTitle(),
                goal.getDescription(),
                goal.getStatus(),
                goal.getPriority(),
                goal.getCategory(),
                goal.getTargetDate(),
                goal.getCompletedDate(),
                goal.getProgress(),
                mapTasks(tasks),
                goal.getNotes(),
                goal.getTags(),
                goal.isBlueprintGoal(),
                goal.getBlueprintTemplateId(),
                goal.getBlueprintPhaseOrder(),
                goal.getBlueprintDefaultOrder(),
                goal.getBlueprintPhaseTitle(),
                goal.getCreatedAt(),
                goal.getUpdatedAt()
        );
    }
}
