package com.careerplanning.backend.modules.admin.controller;

import com.careerplanning.backend.common.response.ApiResponse;
import com.careerplanning.backend.modules.admin.dto.AdminCareerGoalTemplateResponse;
import com.careerplanning.backend.modules.admin.dto.AdminCareerPathResponse;
import com.careerplanning.backend.modules.admin.dto.AdminCareerPhaseResponse;
import com.careerplanning.backend.modules.admin.dto.AdminCreateCareerGoalTemplateRequest;
import com.careerplanning.backend.modules.admin.dto.AdminCreateCareerPhaseRequest;
import com.careerplanning.backend.modules.admin.dto.AdminUpdateCareerGoalTemplateRequest;
import com.careerplanning.backend.modules.admin.dto.AdminUpdateCareerPhaseRequest;
import com.careerplanning.backend.modules.admin.dto.AdminCreateTechnicalAssessmentQuestionRequest;
import com.careerplanning.backend.modules.admin.dto.AdminTechnicalAssessmentQuestionResponse;
import com.careerplanning.backend.modules.admin.dto.AdminUpdateUserRequest;
import com.careerplanning.backend.modules.admin.dto.AdminUpdateTechnicalAssessmentQuestionRequest;
import com.careerplanning.backend.modules.admin.dto.AdminUserDetailResponse;
import com.careerplanning.backend.modules.admin.dto.AdminUserSummaryResponse;
import com.careerplanning.backend.modules.admin.service.AdminService;
import com.careerplanning.backend.modules.admin.service.AdminTechnicalAssessmentService;
import com.careerplanning.backend.modules.goals.dto.CreateGoalRequest;
import com.careerplanning.backend.modules.goals.dto.CreateGoalTaskRequest;
import com.careerplanning.backend.modules.goals.dto.GoalResponse;
import com.careerplanning.backend.modules.goals.dto.UpdateGoalRequest;
import com.careerplanning.backend.modules.goals.dto.UpdateGoalTaskRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;
    private final AdminTechnicalAssessmentService adminTechnicalAssessmentService;

    public AdminController(AdminService adminService,
                           AdminTechnicalAssessmentService adminTechnicalAssessmentService) {
        this.adminService = adminService;
        this.adminTechnicalAssessmentService = adminTechnicalAssessmentService;
    }

    @GetMapping("/roles")
    public ApiResponse<List<String>> listRoles(@RequestHeader("X-Auth-Token") String token) {
        return ApiResponse.success(adminService.listRoles(token));
    }

    @GetMapping("/users")
    public ApiResponse<List<AdminUserSummaryResponse>> listUsers(@RequestHeader("X-Auth-Token") String token) {
        return ApiResponse.success(adminService.listUsers(token));
    }

    @GetMapping("/users/{userId}")
    public ApiResponse<AdminUserDetailResponse> getUserDetail(@RequestHeader("X-Auth-Token") String token,
                                                              @PathVariable Long userId) {
        return ApiResponse.success(adminService.getUserDetail(token, userId));
    }

    @PutMapping("/users/{userId}")
    public ApiResponse<AdminUserDetailResponse> updateUser(@RequestHeader("X-Auth-Token") String token,
                                                           @PathVariable Long userId,
                                                           @Valid @RequestBody AdminUpdateUserRequest request) {
        return ApiResponse.success(adminService.updateUser(token, userId, request));
    }

    @PostMapping("/users/{userId}/goals")
    public ApiResponse<GoalResponse> createGoalForUser(@RequestHeader("X-Auth-Token") String token,
                                                       @PathVariable Long userId,
                                                       @Valid @RequestBody CreateGoalRequest request) {
        return ApiResponse.success(adminService.createGoalForUser(token, userId, request));
    }

    @PutMapping("/users/{userId}/goals/{goalId}")
    public ApiResponse<GoalResponse> updateGoalForUser(@RequestHeader("X-Auth-Token") String token,
                                                       @PathVariable Long userId,
                                                       @PathVariable Long goalId,
                                                       @Valid @RequestBody UpdateGoalRequest request) {
        return ApiResponse.success(adminService.updateGoalForUser(token, userId, goalId, request));
    }

    @DeleteMapping("/users/{userId}/goals/{goalId}")
    public ApiResponse<String> deleteGoalForUser(@RequestHeader("X-Auth-Token") String token,
                                                 @PathVariable Long userId,
                                                 @PathVariable Long goalId) {
        adminService.deleteGoalForUser(token, userId, goalId);
        return ApiResponse.success("Goal deleted successfully");
    }

    @PostMapping("/users/{userId}/goals/{goalId}/tasks")
    public ApiResponse<GoalResponse> createGoalTaskForUser(@RequestHeader("X-Auth-Token") String token,
                                                           @PathVariable Long userId,
                                                           @PathVariable Long goalId,
                                                           @Valid @RequestBody CreateGoalTaskRequest request) {
        return ApiResponse.success(adminService.createGoalTaskForUser(token, userId, goalId, request));
    }

    @PutMapping("/users/{userId}/goals/{goalId}/tasks/{taskId}")
    public ApiResponse<GoalResponse> updateGoalTaskForUser(@RequestHeader("X-Auth-Token") String token,
                                                           @PathVariable Long userId,
                                                           @PathVariable Long goalId,
                                                           @PathVariable Long taskId,
                                                           @Valid @RequestBody UpdateGoalTaskRequest request) {
        return ApiResponse.success(adminService.updateGoalTaskForUser(token, userId, goalId, taskId, request));
    }

    @DeleteMapping("/users/{userId}/goals/{goalId}/tasks/{taskId}")
    public ApiResponse<GoalResponse> deleteGoalTaskForUser(@RequestHeader("X-Auth-Token") String token,
                                                           @PathVariable Long userId,
                                                           @PathVariable Long goalId,
                                                           @PathVariable Long taskId) {
        return ApiResponse.success(adminService.deleteGoalTaskForUser(token, userId, goalId, taskId));
    }

    @GetMapping("/career-paths")
    public ApiResponse<List<AdminCareerPathResponse>> listCareerPaths(@RequestHeader("X-Auth-Token") String token) {
        return ApiResponse.success(adminService.listCareerPaths(token));
    }

    @PostMapping("/career-paths/phases")
    public ApiResponse<AdminCareerPhaseResponse> createCareerPhase(@RequestHeader("X-Auth-Token") String token,
                                                                   @Valid @RequestBody AdminCreateCareerPhaseRequest request) {
        return ApiResponse.success(adminService.createCareerPhase(token, request));
    }

    @PutMapping("/career-paths/phases/{phaseId}")
    public ApiResponse<AdminCareerPhaseResponse> updateCareerPhase(@RequestHeader("X-Auth-Token") String token,
                                                                   @PathVariable Long phaseId,
                                                                   @Valid @RequestBody AdminUpdateCareerPhaseRequest request) {
        return ApiResponse.success(adminService.updateCareerPhase(token, phaseId, request));
    }

    @DeleteMapping("/career-paths/phases/{phaseId}")
    public ApiResponse<String> deleteCareerPhase(@RequestHeader("X-Auth-Token") String token,
                                                 @PathVariable Long phaseId) {
        adminService.deleteCareerPhase(token, phaseId);
        return ApiResponse.success("Career phase deleted successfully");
    }

    @PostMapping("/career-paths/templates")
    public ApiResponse<AdminCareerGoalTemplateResponse> createCareerGoalTemplate(
            @RequestHeader("X-Auth-Token") String token,
            @Valid @RequestBody AdminCreateCareerGoalTemplateRequest request
    ) {
        return ApiResponse.success(adminService.createCareerGoalTemplate(token, request));
    }

    @PutMapping("/career-paths/templates/{templateId}")
    public ApiResponse<AdminCareerGoalTemplateResponse> updateCareerGoalTemplate(
            @RequestHeader("X-Auth-Token") String token,
            @PathVariable Long templateId,
            @Valid @RequestBody AdminUpdateCareerGoalTemplateRequest request
    ) {
        return ApiResponse.success(adminService.updateCareerGoalTemplate(token, templateId, request));
    }

    @DeleteMapping("/career-paths/templates/{templateId}")
    public ApiResponse<String> deleteCareerGoalTemplate(@RequestHeader("X-Auth-Token") String token,
                                                        @PathVariable Long templateId) {
        adminService.deleteCareerGoalTemplate(token, templateId);
        return ApiResponse.success("Career goal template deleted successfully");
    }

    @GetMapping("/technical-assessment/questions")
    public ApiResponse<List<AdminTechnicalAssessmentQuestionResponse>> listTechnicalAssessmentQuestions(
            @RequestHeader("X-Auth-Token") String token,
            @RequestParam String careerTrack
    ) {
        return ApiResponse.success(adminTechnicalAssessmentService.listQuestions(token, careerTrack));
    }

    @PostMapping("/technical-assessment/questions")
    public ApiResponse<AdminTechnicalAssessmentQuestionResponse> createTechnicalAssessmentQuestion(
            @RequestHeader("X-Auth-Token") String token,
            @Valid @RequestBody AdminCreateTechnicalAssessmentQuestionRequest request
    ) {
        return ApiResponse.success(adminTechnicalAssessmentService.createQuestion(token, request));
    }

    @PutMapping("/technical-assessment/questions/{questionId}")
    public ApiResponse<AdminTechnicalAssessmentQuestionResponse> updateTechnicalAssessmentQuestion(
            @RequestHeader("X-Auth-Token") String token,
            @PathVariable Long questionId,
            @Valid @RequestBody AdminUpdateTechnicalAssessmentQuestionRequest request
    ) {
        return ApiResponse.success(adminTechnicalAssessmentService.updateQuestion(token, questionId, request));
    }

    @DeleteMapping("/technical-assessment/questions/{questionId}")
    public ApiResponse<String> deleteTechnicalAssessmentQuestion(
            @RequestHeader("X-Auth-Token") String token,
            @PathVariable Long questionId
    ) {
        adminTechnicalAssessmentService.deleteQuestion(token, questionId);
        return ApiResponse.success("Technical assessment question deleted successfully");
    }
}
