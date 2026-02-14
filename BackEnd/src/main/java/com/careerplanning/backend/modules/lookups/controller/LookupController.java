package com.careerplanning.backend.modules.lookups.controller;

import com.careerplanning.backend.common.response.ApiResponse;
import com.careerplanning.backend.modules.goals.entity.GoalCategory;
import com.careerplanning.backend.modules.goals.entity.GoalPriority;
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
        Map<String, List<String>> templates = new LinkedHashMap<>();
        templates.put(CareerTrack.SOFTWARE_ENGINEERING.name(), List.of(
                "Learn React Framework",
                "Build Portfolio Project",
                "Master Data Structures",
                "Contribute to Open Source",
                "Complete System Design Course",
                "Build Full-Stack Application"
        ));
        templates.put(CareerTrack.DATA_SCIENCE.name(), List.of(
                "Complete ML Course",
                "Build Kaggle Project",
                "Learn Python Advanced",
                "Master Statistical Analysis",
                "Create Data Pipeline",
                "Build Prediction Model"
        ));
        templates.put(CareerTrack.PRODUCT_MANAGEMENT.name(), List.of(
                "Define Product Roadmap",
                "Conduct User Research",
                "Learn Agile Methodology",
                "Create PRD Template",
                "Launch Product Feature",
                "Run A/B Testing"
        ));
        templates.put(CareerTrack.DESIGN.name(), List.of(
                "Master Figma",
                "Build Design System",
                "Learn UX Research",
                "Create Portfolio",
                "Design Mobile App",
                "Study Accessibility"
        ));
        templates.put(CareerTrack.MARKETING.name(), List.of(
                "Learn SEO Basics",
                "Run Ad Campaign",
                "Master Analytics",
                "Build Content Strategy",
                "Social Media Growth",
                "Email Marketing Setup"
        ));
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
