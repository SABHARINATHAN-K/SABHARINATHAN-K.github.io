package com.careerplanning.backend.modules.admin.controller;

import com.careerplanning.backend.common.exception.AccessDeniedException;
import com.careerplanning.backend.modules.admin.dto.AdminCareerPhaseResponse;
import com.careerplanning.backend.modules.admin.dto.AdminTechnicalAssessmentOptionResponse;
import com.careerplanning.backend.modules.admin.dto.AdminTechnicalAssessmentQuestionResponse;
import com.careerplanning.backend.modules.admin.dto.AdminUserDetailResponse;
import com.careerplanning.backend.modules.admin.dto.AdminUserSummaryResponse;
import com.careerplanning.backend.modules.admin.service.AdminService;
import com.careerplanning.backend.modules.admin.service.AdminTechnicalAssessmentService;
import com.careerplanning.backend.modules.career.dto.TechnicalAssessmentProgressResponse;
import com.careerplanning.backend.modules.users.dto.UserProfileResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
class AdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @MockBean
    private AdminTechnicalAssessmentService adminTechnicalAssessmentService;

    @Test
    void usersEndpointReturnsAdminUserSummaries() throws Exception {
        when(adminService.listUsers("admin-token")).thenReturn(List.of(
                new AdminUserSummaryResponse(
                        10L,
                        "Jane Admin",
                        "jane@example.com",
                        "STUDENT",
                        "FULL_STACK_DEVELOPER",
                        true,
                        Instant.parse("2026-03-24T00:00:00Z"),
                        3,
                        1,
                        12,
                        5
                )
        ));

        mockMvc.perform(get("/api/v1/admin/users")
                        .header("X-Auth-Token", "admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].email").value("jane@example.com"))
                .andExpect(jsonPath("$.data[0].careerTrack").value("FULL_STACK_DEVELOPER"))
                .andExpect(jsonPath("$.data[0].goalCount").value(3))
                .andExpect(jsonPath("$.data[0].completedTaskCount").value(5));
    }

    @Test
    void createCareerPhaseEndpointReturnsCreatedPhase() throws Exception {
        when(adminService.createCareerPhase(org.mockito.ArgumentMatchers.eq("admin-token"), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new AdminCareerPhaseResponse(7L, "CLOUD_ENGINEER", 1, "Foundation", "Learn cloud basics"));

        mockMvc.perform(post("/api/v1/admin/career-paths/phases")
                        .header("X-Auth-Token", "admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "careerTrack": "Cloud Engineer",
                                  "phaseOrder": 1,
                                  "phaseTitle": "Foundation",
                                  "description": "Learn cloud basics"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.careerTrack").value("CLOUD_ENGINEER"))
                .andExpect(jsonPath("$.data.phaseTitle").value("Foundation"));
    }

    @Test
    void technicalAssessmentQuestionsEndpointReturnsConfiguredQuestionSet() throws Exception {
        when(adminTechnicalAssessmentService.listQuestions("admin-token", "JAVA_BACKEND_DEVELOPER")).thenReturn(List.of(
                new AdminTechnicalAssessmentQuestionResponse(
                        1001L,
                        "JAVA_BACKEND_DEVELOPER",
                        "API Design",
                        "FOUNDATION",
                        "A POST /orders endpoint successfully creates a new order. Which response contract is most correct?",
                        null,
                        1,
                        true,
                        List.of(
                                new AdminTechnicalAssessmentOptionResponse(10011L, "200 OK with no metadata because the body is enough", false, 1),
                                new AdminTechnicalAssessmentOptionResponse(10012L, "201 Created with the new resource location and representation", true, 2)
                        )
                )
        ));

        mockMvc.perform(get("/api/v1/admin/technical-assessment/questions")
                        .header("X-Auth-Token", "admin-token")
                        .param("careerTrack", "JAVA_BACKEND_DEVELOPER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].careerTrack").value("JAVA_BACKEND_DEVELOPER"))
                .andExpect(jsonPath("$.data[0].options[1].correct").value(true));
    }

    @Test
    void userDetailEndpointReturnsTechnicalReadinessProgress() throws Exception {
        when(adminService.getUserDetail("admin-token", 44L)).thenReturn(new AdminUserDetailResponse(
                new UserProfileResponse(
                        44L,
                        "Learner One",
                        "learner@example.com",
                        "STUDENT",
                        "JAVA_BACKEND_DEVELOPER",
                        true,
                        "Focused on backend systems",
                        "Chennai",
                        Instant.parse("2026-03-20T00:00:00Z")
                ),
                List.of(),
                new TechnicalAssessmentProgressResponse(
                        "JAVA_BACKEND_DEVELOPER",
                        "INTERMEDIATE",
                        68,
                        54,
                        14,
                        Instant.parse("2026-03-24T00:00:00Z"),
                        Instant.parse("2026-04-23T00:00:00Z"),
                        false,
                        "Readiness is improving with stronger persistence fundamentals.",
                        List.of(),
                        List.of()
                )
        ));

        mockMvc.perform(get("/api/v1/admin/users/44")
                        .header("X-Auth-Token", "admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.profile.email").value("learner@example.com"))
                .andExpect(jsonPath("$.data.technicalReadiness.currentLevel").value("INTERMEDIATE"))
                .andExpect(jsonPath("$.data.technicalReadiness.currentPercentageScore").value(68));
    }

    @Test
    void usersEndpointReturnsForbiddenWhenNonAdminTokenIsUsed() throws Exception {
        when(adminService.listUsers("user-token")).thenThrow(new AccessDeniedException("Admin access required"));

        mockMvc.perform(get("/api/v1/admin/users")
                        .header("X-Auth-Token", "user-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Admin access required"));
    }
}
