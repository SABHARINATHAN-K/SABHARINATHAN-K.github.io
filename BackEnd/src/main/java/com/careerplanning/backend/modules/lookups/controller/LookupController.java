package com.careerplanning.backend.modules.lookups.controller;

import com.careerplanning.backend.common.response.ApiResponse;
import com.careerplanning.backend.modules.users.entity.CareerTrack;
import com.careerplanning.backend.modules.users.entity.UserRole;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/lookups")
public class LookupController {

    @GetMapping("/roles")
    public ApiResponse<List<String>> listRoles() {
        return ApiResponse.success(UserRole.options());
    }

    @GetMapping("/career-tracks")
    public ApiResponse<List<String>> listCareerTracks() {
        return ApiResponse.success(CareerTrack.options());
    }

    @GetMapping("/goal-templates")
    public ApiResponse<Map<String, List<String>>> goalTemplates() {
        Map<String, List<String>> templates = new LinkedHashMap<>();
        templates.put(CareerTrack.JAVA_BACKEND_DEVELOPER.name(), List.of(
                "Master Spring Boot fundamentals",
                "Build and deploy a REST API",
                "Learn SQL and query optimization"
        ));
        templates.put(CareerTrack.FRONTEND_DEVELOPER.name(), List.of(
                "Build responsive interfaces with HTML/CSS",
                "Master JavaScript and API integration",
                "Create an accessible dashboard project"
        ));
        templates.put(CareerTrack.FULL_STACK_DEVELOPER.name(), List.of(
                "Build an end-to-end project",
                "Integrate frontend with backend APIs",
                "Deploy full stack app to cloud"
        ));
        templates.put(CareerTrack.DATA_ANALYST.name(), List.of(
                "Learn SQL for analytics",
                "Build dashboards with real datasets",
                "Practice statistical storytelling"
        ));
        templates.put(CareerTrack.DATA_SCIENTIST.name(), List.of(
                "Build machine learning pipeline",
                "Practice feature engineering",
                "Deploy a prediction model"
        ));
        templates.put(CareerTrack.DEVOPS_ENGINEER.name(), List.of(
                "Automate CI/CD pipeline",
                "Containerize services using Docker",
                "Set up monitoring and alerts"
        ));
        templates.put(CareerTrack.QA_ENGINEER.name(), List.of(
                "Design automated API tests",
                "Implement regression test suite",
                "Track bugs with reproducible reports"
        ));
        templates.put(CareerTrack.UI_UX_DESIGNER.name(), List.of(
                "Create wireframes for key flows",
                "Run usability tests with users",
                "Build high-fidelity design system"
        ));
        templates.put(CareerTrack.PRODUCT_MANAGER.name(), List.of(
                "Define product roadmap and milestones",
                "Write clear product requirement docs",
                "Track feature outcomes by metrics"
        ));
        templates.put(CareerTrack.CYBERSECURITY_ANALYST.name(), List.of(
                "Perform vulnerability assessment",
                "Build incident response checklist",
                "Practice secure coding standards"
        ));
        return ApiResponse.success(templates);
    }
}
