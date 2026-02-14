package com.careerplanning.backend.modules.goals.service;

import com.careerplanning.backend.modules.auth.service.SimpleTokenService;
import com.careerplanning.backend.modules.goals.dto.CreateGoalRequest;
import com.careerplanning.backend.modules.goals.dto.GoalResponse;
import com.careerplanning.backend.modules.goals.dto.GoalStatsResponse;
import com.careerplanning.backend.modules.goals.dto.UpdateGoalRequest;
import com.careerplanning.backend.modules.goals.entity.Goal;
import com.careerplanning.backend.modules.goals.entity.GoalCategory;
import com.careerplanning.backend.modules.goals.entity.GoalPriority;
import com.careerplanning.backend.modules.goals.entity.GoalStatus;
import com.careerplanning.backend.modules.goals.repository.GoalRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class GoalService {

    private final GoalRepository goalRepository;
    private final SimpleTokenService simpleTokenService;

    public GoalService(GoalRepository goalRepository, SimpleTokenService simpleTokenService) {
        this.goalRepository = goalRepository;
        this.simpleTokenService = simpleTokenService;
    }

    public GoalResponse createGoal(String token, CreateGoalRequest request) {
        Long userId = simpleTokenService.getUserId(token);

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
        return toResponse(saved);
    }

    public List<GoalResponse> listGoals(String token) {
        Long userId = simpleTokenService.getUserId(token);

        return goalRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public GoalResponse getGoalById(String token, Long goalId) {
        Long userId = simpleTokenService.getUserId(token);

        Goal goal = goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found"));
        return toResponse(goal);
    }

    public GoalStatsResponse getGoalStats(String token) {
        Long userId = simpleTokenService.getUserId(token);
        List<Goal> goals = goalRepository.findByUserIdOrderByCreatedAtDesc(userId);

        int total = goals.size();
        int planned = (int) goals.stream().filter(goal -> goal.getStatus() == GoalStatus.PLANNED).count();
        int inProgress = (int) goals.stream().filter(goal -> goal.getStatus() == GoalStatus.IN_PROGRESS).count();
        int completed = (int) goals.stream().filter(goal -> goal.getStatus() == GoalStatus.COMPLETED).count();

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

        int completionRate = total == 0 ? 0 : (int) Math.round((completed * 100.0) / total);
        return new GoalStatsResponse(total, planned, inProgress, completed, byCategory, byPriority, completionRate);
    }

    public GoalResponse updateGoal(String token, Long goalId, UpdateGoalRequest request) {
        Long userId = simpleTokenService.getUserId(token);

        Goal goal = goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found"));

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
        }
        if (request.progress() != null) {
            int progress = request.progress();
            if (progress < 0 || progress > 100) {
                throw new IllegalArgumentException("progress must be between 0 and 100");
            }
            goal.setProgress(progress);
        }
        if (request.notes() != null) {
            goal.setNotes(request.notes());
        }
        if (request.tags() != null) {
            goal.setTags(normalizeTags(request.tags()));
        }

        if (request.status() != null) {
            GoalStatus previousStatus = goal.getStatus();
            goal.setStatus(request.status());

            if (request.status() == GoalStatus.COMPLETED) {
                if (previousStatus != GoalStatus.COMPLETED) {
                    goal.setCompletedDate(Instant.now());
                }
                goal.setProgress(100);
            } else if (previousStatus == GoalStatus.COMPLETED) {
                goal.setCompletedDate(null);
            }
        }

        Goal saved = goalRepository.save(goal);
        return toResponse(saved);
    }

    public void deleteGoal(String token, Long goalId) {
        Long userId = simpleTokenService.getUserId(token);

        Goal goal = goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found"));

        goalRepository.delete(goal);
    }

    private String normalizeRequiredTitle(String rawTitle) {
        String title = rawTitle == null ? "" : rawTitle.trim();
        if (title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        return title;
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

    private GoalResponse toResponse(Goal goal) {
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
                goal.getNotes(),
                goal.getTags(),
                goal.getCreatedAt(),
                goal.getUpdatedAt()
        );
    }
}
