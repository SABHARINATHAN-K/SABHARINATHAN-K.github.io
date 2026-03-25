package com.careerplanning.backend.modules.lookups.controller;

import com.careerplanning.backend.common.response.ApiResponse;
import com.careerplanning.backend.modules.career.repository.CareerGoalTemplateRepository;
import com.careerplanning.backend.modules.career.service.CareerTrackCatalogService;
import com.careerplanning.backend.modules.goals.entity.GoalCategory;
import com.careerplanning.backend.modules.goals.entity.GoalPriority;
import com.careerplanning.backend.modules.users.entity.UserRole;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/lookups")
public class LookupController {

    private final CareerTrackCatalogService careerTrackCatalogService;
    private final CareerGoalTemplateRepository careerGoalTemplateRepository;

    public LookupController(CareerTrackCatalogService careerTrackCatalogService,
                            CareerGoalTemplateRepository careerGoalTemplateRepository) {
        this.careerTrackCatalogService = careerTrackCatalogService;
        this.careerGoalTemplateRepository = careerGoalTemplateRepository;
    }

    @GetMapping("/roles")
    public ApiResponse<List<String>> listRoles() {
        return ApiResponse.success(UserRole.selfServiceOptions());
    }

    @GetMapping("/career-tracks")
    public ApiResponse<List<String>> listCareerTracks() {
        return ApiResponse.success(careerTrackCatalogService.listAvailableTracks());
    }

    @GetMapping("/goal-categories")
    public ApiResponse<List<String>> listGoalCategories() {
        return ApiResponse.success(List.of(
                GoalCategory.SKILL_DEVELOPMENT.name(),
                GoalCategory.CAREER_GROWTH.name(),
                GoalCategory.NETWORKING.name(),
                GoalCategory.CERTIFICATION.name(),
                GoalCategory.PROJECT.name(),
                GoalCategory.LEARNING.name()
        ));
    }

    @GetMapping("/goal-priorities")
    public ApiResponse<List<String>> listGoalPriorities() {
        return ApiResponse.success(List.of(
                GoalPriority.LOW.name(),
                GoalPriority.MEDIUM.name(),
                GoalPriority.HIGH.name(),
                GoalPriority.URGENT.name()
        ));
    }

    @GetMapping("/goal-templates")
    public ApiResponse<Map<String, List<String>>> goalTemplates() {
        Map<String, List<String>> templates = careerGoalTemplateRepository.findAllByOrderByCareerTrackAscPhaseIdAscDefaultOrderAscIdAsc()
                .stream()
                .collect(Collectors.groupingBy(
                        template -> template.getCareerTrack(),
                        LinkedHashMap::new,
                        Collectors.mapping(template -> template.getTitle(), Collectors.toList())
                ));
        return ApiResponse.success(templates);
    }
}
