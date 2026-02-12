package com.careerplanning.backend.modules.goals.service;

import com.careerplanning.backend.modules.auth.service.SimpleTokenService;
import com.careerplanning.backend.modules.goals.dto.CreateGoalRequest;
import com.careerplanning.backend.modules.goals.dto.GoalResponse;
import com.careerplanning.backend.modules.goals.dto.UpdateGoalRequest;
import com.careerplanning.backend.modules.goals.entity.Goal;
import com.careerplanning.backend.modules.goals.repository.GoalRepository;
import org.springframework.stereotype.Service;

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

        Goal goal = new Goal();
        goal.setUserId(userId);
        goal.setTitle(request.title());
        goal.setDescription(request.description());
        goal.setTargetDate(request.targetDate());

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

    public GoalResponse updateGoal(String token, Long goalId, UpdateGoalRequest request) {
        Long userId = simpleTokenService.getUserId(token);

        Goal goal = goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found"));

        goal.setTitle(request.title());
        goal.setDescription(request.description());
        goal.setTargetDate(request.targetDate());
        if (request.status() != null) {
            goal.setStatus(request.status());
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

    private GoalResponse toResponse(Goal goal) {
        return new GoalResponse(
                goal.getId(),
                goal.getTitle(),
                goal.getDescription(),
                goal.getStatus(),
                goal.getTargetDate()
        );
    }
}
