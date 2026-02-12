package com.careerplanning.backend.modules.goals.controller;

import com.careerplanning.backend.common.response.ApiResponse;
import com.careerplanning.backend.modules.goals.dto.CreateGoalRequest;
import com.careerplanning.backend.modules.goals.dto.GoalResponse;
import com.careerplanning.backend.modules.goals.dto.UpdateGoalRequest;
import com.careerplanning.backend.modules.goals.service.GoalService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @PostMapping
    public ApiResponse<GoalResponse> createGoal(@RequestHeader("X-Auth-Token") String token,
                                                @Valid @RequestBody CreateGoalRequest request) {
        return ApiResponse.success(goalService.createGoal(token, request));
    }

    @GetMapping
    public ApiResponse<List<GoalResponse>> listGoals(@RequestHeader("X-Auth-Token") String token) {
        return ApiResponse.success(goalService.listGoals(token));
    }

    @PutMapping("/{goalId}")
    public ApiResponse<GoalResponse> updateGoal(@RequestHeader("X-Auth-Token") String token,
                                                @PathVariable Long goalId,
                                                @Valid @RequestBody UpdateGoalRequest request) {
        return ApiResponse.success(goalService.updateGoal(token, goalId, request));
    }

    @DeleteMapping("/{goalId}")
    public ApiResponse<String> deleteGoal(@RequestHeader("X-Auth-Token") String token,
                                          @PathVariable Long goalId) {
        goalService.deleteGoal(token, goalId);
        return ApiResponse.success("Goal deleted successfully");
    }
}
