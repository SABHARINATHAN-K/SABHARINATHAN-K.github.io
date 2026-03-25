package com.careerplanning.backend.modules.career.controller;

import com.careerplanning.backend.modules.career.dto.GenerateRoadmapResponse;
import com.careerplanning.backend.modules.career.dto.SkillAreaScoreResponse;
import com.careerplanning.backend.modules.career.dto.TechnicalAssessmentHistoryItemResponse;
import com.careerplanning.backend.modules.career.dto.TechnicalAssessmentOptionResponse;
import com.careerplanning.backend.modules.career.dto.TechnicalAssessmentProgressResponse;
import com.careerplanning.backend.modules.career.dto.TechnicalAssessmentQuestionResponse;
import com.careerplanning.backend.modules.career.dto.TechnicalAssessmentResultResponse;
import com.careerplanning.backend.modules.career.service.CareerService;
import com.careerplanning.backend.modules.career.service.TechnicalAssessmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CareerController.class)
class CareerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CareerService careerService;

    @MockBean
    private TechnicalAssessmentService technicalAssessmentService;

    @Test
    void generateRoadmapEndpointReturnsBlueprintSummary() throws Exception {
        GenerateRoadmapResponse response = new GenerateRoadmapResponse(
                "FULL_STACK_DEVELOPER",
                4,
                3,
                1,
                "Existing blueprint goals from previous career tracks were kept. Nothing was deleted."
        );

        when(careerService.generateRoadmap("test-token")).thenReturn(response);

        mockMvc.perform(post("/api/v1/career/generate-roadmap")
                        .header("X-Auth-Token", "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.careerTrack").value("FULL_STACK_DEVELOPER"))
                .andExpect(jsonPath("$.data.templatesFound").value(4))
                .andExpect(jsonPath("$.data.goalsCreated").value(3))
                .andExpect(jsonPath("$.data.warning").isNotEmpty());
    }

    @Test
    void technicalAssessmentTracksEndpointReturnsFocusedTrackList() throws Exception {
        when(technicalAssessmentService.listSupportedTracks()).thenReturn(List.of(
                "JAVA_BACKEND_DEVELOPER",
                "FRONTEND_DEVELOPER",
                "DEVOPS_ENGINEER"
        ));

        mockMvc.perform(get("/api/v1/career/technical-assessment/tracks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0]").value("JAVA_BACKEND_DEVELOPER"))
                .andExpect(jsonPath("$.data[2]").value("DEVOPS_ENGINEER"));
    }

    @Test
    void technicalAssessmentQuestionsEndpointReturnsRoleQuestionSet() throws Exception {
        when(technicalAssessmentService.getQuestions("JAVA_BACKEND_DEVELOPER")).thenReturn(List.of(
                new TechnicalAssessmentQuestionResponse(
                        1001L,
                        "JAVA_BACKEND_DEVELOPER",
                        "API Design",
                        "FOUNDATION",
                        "A POST /orders endpoint successfully creates a new order. Which response contract is most correct?",
                        List.of(
                                new TechnicalAssessmentOptionResponse(10011L, "200 OK with no metadata because the body is enough"),
                                new TechnicalAssessmentOptionResponse(10012L, "201 Created with the new resource location and representation")
                        )
                )
        ));

        mockMvc.perform(get("/api/v1/career/technical-assessment/questions")
                        .param("careerTrack", "JAVA_BACKEND_DEVELOPER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].careerTrack").value("JAVA_BACKEND_DEVELOPER"))
                .andExpect(jsonPath("$.data[0].skillArea").value("API Design"))
                .andExpect(jsonPath("$.data[0].options[1].optionText").value("201 Created with the new resource location and representation"));
    }

    @Test
    void submitTechnicalAssessmentEndpointReturnsScoredBenchmarkResult() throws Exception {
        TechnicalAssessmentResultResponse response = new TechnicalAssessmentResultResponse(
                "JAVA_BACKEND_DEVELOPER",
                "ADVANCED",
                80,
                90,
                89,
                14,
                List.of(
                        new SkillAreaScoreResponse("Spring Foundations", 20, 20, 100),
                        new SkillAreaScoreResponse("Performance", 15, 20, 75)
                ),
                "Strong Spring fundamentals with room to sharpen backend performance tuning.",
                Instant.parse("2026-03-24T08:30:00Z")
        );

        when(technicalAssessmentService.submitAssessment(eq("test-token"), org.mockito.ArgumentMatchers.any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/career/technical-assessment/submit")
                        .header("X-Auth-Token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new TechnicalRequestBody(
                                        "JAVA_BACKEND_DEVELOPER",
                                        List.of(
                                                new AnswerBody(101L, 1012L),
                                                new AnswerBody(102L, 1021L)
                                        )
                                )
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.careerTrack").value("JAVA_BACKEND_DEVELOPER"))
                .andExpect(jsonPath("$.data.proficiencyLevel").value("ADVANCED"))
                .andExpect(jsonPath("$.data.percentageScore").value(89))
                .andExpect(jsonPath("$.data.skillAreas[0].skillArea").value("Spring Foundations"))
                .andExpect(jsonPath("$.data.improvementPercentagePoints").value(14));
    }

    @Test
    void technicalAssessmentProgressEndpointReturnsHistoryAndReassessmentState() throws Exception {
        TechnicalAssessmentProgressResponse response = new TechnicalAssessmentProgressResponse(
                "DEVOPS_ENGINEER",
                "INTERMEDIATE",
                62,
                49,
                13,
                Instant.parse("2026-03-20T07:00:00Z"),
                Instant.parse("2026-04-19T07:00:00Z"),
                false,
                "Your cloud security baseline improved but observability is still the weakest area.",
                List.of(
                        new SkillAreaScoreResponse("Cloud Security", 15, 20, 75),
                        new SkillAreaScoreResponse("Observability", 10, 20, 50)
                ),
                List.of(
                        new TechnicalAssessmentHistoryItemResponse(
                                Instant.parse("2026-02-15T07:00:00Z"),
                                49,
                                "BEGINNER"
                        ),
                        new TechnicalAssessmentHistoryItemResponse(
                                Instant.parse("2026-03-20T07:00:00Z"),
                                62,
                                "INTERMEDIATE"
                        )
                )
        );

        when(technicalAssessmentService.getProgress("test-token")).thenReturn(response);

        mockMvc.perform(get("/api/v1/career/technical-assessment/progress")
                        .header("X-Auth-Token", "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.careerTrack").value("DEVOPS_ENGINEER"))
                .andExpect(jsonPath("$.data.currentLevel").value("INTERMEDIATE"))
                .andExpect(jsonPath("$.data.improvementPercentagePoints").value(13))
                .andExpect(jsonPath("$.data.history[1].percentageScore").value(62))
                .andExpect(jsonPath("$.data.reassessmentDue").value(false));
    }

    private record TechnicalRequestBody(String careerTrack, List<AnswerBody> answers) {}

    private record AnswerBody(Long questionId, Long optionId) {}
}
