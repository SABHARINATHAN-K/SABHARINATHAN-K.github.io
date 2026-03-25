package com.careerplanning.backend.modules.career.service;

import com.careerplanning.backend.modules.auth.service.SimpleTokenService;
import com.careerplanning.backend.modules.career.dto.AssessmentAnswer;
import com.careerplanning.backend.modules.career.dto.SubmitTechnicalAssessmentRequest;
import com.careerplanning.backend.modules.career.dto.TechnicalAssessmentProgressResponse;
import com.careerplanning.backend.modules.career.dto.TechnicalAssessmentQuestionResponse;
import com.careerplanning.backend.modules.career.dto.TechnicalAssessmentResultResponse;
import com.careerplanning.backend.modules.career.entity.TechnicalAssessmentDifficulty;
import com.careerplanning.backend.modules.career.entity.TechnicalAssessmentOption;
import com.careerplanning.backend.modules.career.entity.TechnicalAssessmentQuestion;
import com.careerplanning.backend.modules.career.entity.TechnicalAssessmentResult;
import com.careerplanning.backend.modules.career.repository.TechnicalAssessmentOptionRepository;
import com.careerplanning.backend.modules.career.repository.TechnicalAssessmentQuestionRepository;
import com.careerplanning.backend.modules.career.repository.TechnicalAssessmentResultRepository;
import com.careerplanning.backend.modules.users.entity.User;
import com.careerplanning.backend.modules.users.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TechnicalAssessmentServiceTest {

    @Mock
    private SimpleTokenService simpleTokenService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TechnicalAssessmentResultRepository technicalAssessmentResultRepository;

    @Mock
    private TechnicalAssessmentQuestionRepository technicalAssessmentQuestionRepository;

    @Mock
    private TechnicalAssessmentOptionRepository technicalAssessmentOptionRepository;

    @Mock
    private CareerTrackCatalogService careerTrackCatalogService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private TechnicalAssessmentService technicalAssessmentService;

    @BeforeEach
    void setUp() {
        technicalAssessmentService = new TechnicalAssessmentService(
                simpleTokenService,
                userRepository,
                technicalAssessmentResultRepository,
                technicalAssessmentQuestionRepository,
                technicalAssessmentOptionRepository,
                careerTrackCatalogService,
                objectMapper
        );
    }

    @Test
    void getQuestionsReturnsFocusedTechnicalBenchmarkForSupportedTrack() {
        TechnicalAssessmentQuestion apiQuestion = question(1001L, "JAVA_BACKEND_DEVELOPER", "API Design", TechnicalAssessmentDifficulty.FOUNDATION,
                "A POST /orders endpoint successfully creates a new order. Which response contract is most correct?", 1);
        TechnicalAssessmentQuestion springQuestion = question(1002L, "JAVA_BACKEND_DEVELOPER", "Spring Architecture", TechnicalAssessmentDifficulty.APPLIED,
                "You publish a domain event only if the surrounding transaction commits. Which Spring mechanism is designed for that?", 2);

        when(careerTrackCatalogService.validateTechnicalCareerTrack("java_backend_developer")).thenReturn("JAVA_BACKEND_DEVELOPER");
        when(technicalAssessmentQuestionRepository.findByCareerTrackAndActiveTrueOrderByDisplayOrderAscIdAsc("JAVA_BACKEND_DEVELOPER"))
                .thenReturn(List.of(apiQuestion, springQuestion));
        when(technicalAssessmentOptionRepository.findByQuestionIdInOrderByQuestionIdAscSortOrderAscIdAsc(List.of(1001L, 1002L)))
                .thenReturn(List.of(
                        option(10011L, 1001L, "200 OK with no metadata because the body is enough", false, 1),
                        option(10012L, 1001L, "201 Created with the new resource location and representation", true, 2),
                        option(10021L, 1002L, "ApplicationReadyEvent", false, 1),
                        option(10022L, 1002L, "@TransactionalEventListener with AFTER_COMMIT phase", true, 2)
                ));

        List<TechnicalAssessmentQuestionResponse> questions = technicalAssessmentService.getQuestions("java_backend_developer");

        assertEquals(2, questions.size());
        assertEquals("JAVA_BACKEND_DEVELOPER", questions.get(0).careerTrack());
        assertEquals("API Design", questions.get(0).skillArea());
        assertEquals(2, questions.get(0).options().size());
        assertTrue(questions.get(1).questionText().contains("transaction"));
    }

    @Test
    void submitAssessmentScoresTechnicalAttemptAndPersistsBaseline() {
        User user = new User();
        user.setCareerTrack("JAVA_BACKEND_DEVELOPER");

        TechnicalAssessmentQuestion q1 = question(1001L, "JAVA_BACKEND_DEVELOPER", "API Design", TechnicalAssessmentDifficulty.FOUNDATION,
                "A POST /orders endpoint successfully creates a new order. Which response contract is most correct?", 1);
        TechnicalAssessmentQuestion q2 = question(1002L, "JAVA_BACKEND_DEVELOPER", "Persistence", TechnicalAssessmentDifficulty.APPLIED,
                "A JPA service loads 50 orders, then triggers one extra SQL query per order to read line items. What issue are you seeing?", 2);
        TechnicalAssessmentQuestion q3 = question(1003L, "JAVA_BACKEND_DEVELOPER", "Security", TechnicalAssessmentDifficulty.ARCHITECTURE,
                "A frontend hides admin buttons, but the backend also has admin endpoints. What must the backend still do on every protected request?", 3);

        when(simpleTokenService.getUserId("token-123")).thenReturn(42L);
        when(userRepository.findById(42L)).thenReturn(Optional.of(user));
        when(careerTrackCatalogService.validateTechnicalCareerTrack("JAVA_BACKEND_DEVELOPER")).thenReturn("JAVA_BACKEND_DEVELOPER");
        when(technicalAssessmentQuestionRepository.findByCareerTrackAndActiveTrueOrderByDisplayOrderAscIdAsc("JAVA_BACKEND_DEVELOPER"))
                .thenReturn(List.of(q1, q2, q3));
        when(technicalAssessmentOptionRepository.findByQuestionIdInOrderByQuestionIdAscSortOrderAscIdAsc(List.of(1001L, 1002L, 1003L)))
                .thenReturn(List.of(
                        option(10012L, 1001L, "201 Created with the new resource location and representation", true, 1),
                        option(10011L, 1001L, "200 OK with no metadata because the body is enough", false, 2),
                        option(10023L, 1002L, "The N+1 query problem caused by repeated lazy loads", true, 1),
                        option(10021L, 1002L, "Deadlock caused by optimistic locking", false, 2),
                        option(10033L, 1003L, "Enforce authorization server-side for every protected request", true, 1),
                        option(10031L, 1003L, "Trust that the UI already hid the admin buttons", false, 2)
                ));
        when(technicalAssessmentResultRepository.findFirstByUserIdAndCareerTrackOrderByCreatedAtDesc(42L, "JAVA_BACKEND_DEVELOPER"))
                .thenReturn(Optional.empty());
        when(technicalAssessmentResultRepository.save(any(TechnicalAssessmentResult.class))).thenAnswer(invocation -> {
            TechnicalAssessmentResult result = invocation.getArgument(0);
            ReflectionTestUtils.setField(result, "id", 901L);
            ReflectionTestUtils.setField(result, "createdAt", Instant.parse("2026-03-24T09:00:00Z"));
            return result;
        });

        TechnicalAssessmentResultResponse response = technicalAssessmentService.submitAssessment(
                "token-123",
                new SubmitTechnicalAssessmentRequest(
                        "JAVA_BACKEND_DEVELOPER",
                        List.of(
                                new AssessmentAnswer(1001L, 10012L),
                                new AssessmentAnswer(1002L, 10023L),
                                new AssessmentAnswer(1003L, 10033L)
                        )
                )
        );

        assertEquals("JAVA_BACKEND_DEVELOPER", response.careerTrack());
        assertEquals("EXPERT", response.proficiencyLevel());
        assertEquals(100, response.percentageScore());
        assertNull(response.improvementPercentagePoints());
        assertEquals(3, response.skillAreas().size());
        assertNotNull(response.assessedAt());
        assertTrue(response.performanceSummary().contains("Java Backend Developer"));
        assertTrue(response.performanceSummary().contains("Focus next on"));

        ArgumentCaptor<TechnicalAssessmentResult> captor = ArgumentCaptor.forClass(TechnicalAssessmentResult.class);
        verify(technicalAssessmentResultRepository).save(captor.capture());
        assertEquals(42L, captor.getValue().getUserId());
        assertEquals("JAVA_BACKEND_DEVELOPER", captor.getValue().getCareerTrack());
        assertEquals(45, captor.getValue().getMaxScore());
        assertEquals(100, captor.getValue().getPercentageScore());
    }

    @Test
    void getProgressReturnsImprovementAndChronologicalHistory() {
        User user = new User();
        user.setCareerTrack("DEVOPS_ENGINEER");
        Instant latestCreatedAt = Instant.now().minus(5, ChronoUnit.DAYS);
        Instant previousCreatedAt = latestCreatedAt.minus(34, ChronoUnit.DAYS);

        TechnicalAssessmentResult latest = technicalResult(
                1001L,
                55L,
                "DEVOPS_ENGINEER",
                "INTERMEDIATE",
                50,
                80,
                62,
                "Your observability baseline is improving.",
                latestCreatedAt,
                skillAreaJson(List.of(
                        Map.of("skillArea", "Security", "score", 15, "maxScore", 20, "percentageScore", 75),
                        Map.of("skillArea", "Observability", "score", 10, "maxScore", 20, "percentageScore", 50)
                ))
        );
        TechnicalAssessmentResult previous = technicalResult(
                1000L,
                55L,
                "DEVOPS_ENGINEER",
                "BEGINNER",
                39,
                80,
                49,
                "Initial baseline.",
                previousCreatedAt,
                skillAreaJson(List.of(
                        Map.of("skillArea", "Security", "score", 10, "maxScore", 20, "percentageScore", 50),
                        Map.of("skillArea", "Observability", "score", 8, "maxScore", 20, "percentageScore", 40)
                ))
        );

        when(simpleTokenService.getUserId("progress-token")).thenReturn(55L);
        when(userRepository.findById(55L)).thenReturn(Optional.of(user));
        when(technicalAssessmentResultRepository.findFirstByUserIdAndCareerTrackOrderByCreatedAtDesc(55L, "DEVOPS_ENGINEER"))
                .thenReturn(Optional.of(latest));
        when(technicalAssessmentResultRepository.findTop8ByUserIdAndCareerTrackOrderByCreatedAtDesc(55L, "DEVOPS_ENGINEER"))
                .thenReturn(List.of(latest, previous));

        TechnicalAssessmentProgressResponse response = technicalAssessmentService.getProgress("progress-token");

        assertEquals("DEVOPS_ENGINEER", response.careerTrack());
        assertEquals("INTERMEDIATE", response.currentLevel());
        assertEquals(62, response.currentPercentageScore());
        assertEquals(49, response.previousPercentageScore());
        assertEquals(13, response.improvementPercentagePoints());
        assertEquals(2, response.latestSkillAreas().size());
        assertEquals(2, response.history().size());
        assertEquals(49, response.history().get(0).percentageScore());
        assertEquals(62, response.history().get(1).percentageScore());
        assertFalse(response.reassessmentDue());
        assertEquals(latestCreatedAt.plus(30, ChronoUnit.DAYS), response.recommendedReassessmentAt());
    }

    private TechnicalAssessmentQuestion question(Long id,
                                                 String careerTrack,
                                                 String skillArea,
                                                 TechnicalAssessmentDifficulty difficulty,
                                                 String questionText,
                                                 int displayOrder) {
        TechnicalAssessmentQuestion question = new TechnicalAssessmentQuestion();
        question.setCareerTrack(careerTrack);
        question.setSkillArea(skillArea);
        question.setDifficulty(difficulty);
        question.setQuestionText(questionText);
        question.setDisplayOrder(displayOrder);
        question.setActive(true);
        ReflectionTestUtils.setField(question, "id", id);
        return question;
    }

    private TechnicalAssessmentOption option(Long id, Long questionId, String optionText, boolean correct, int sortOrder) {
        TechnicalAssessmentOption option = new TechnicalAssessmentOption();
        option.setQuestionId(questionId);
        option.setOptionText(optionText);
        option.setCorrect(correct);
        option.setSortOrder(sortOrder);
        ReflectionTestUtils.setField(option, "id", id);
        return option;
    }

    private TechnicalAssessmentResult technicalResult(Long id,
                                                      Long userId,
                                                      String careerTrack,
                                                      String proficiencyLevel,
                                                      int totalScore,
                                                      int maxScore,
                                                      int percentageScore,
                                                      String summary,
                                                      Instant createdAt,
                                                      String skillAreaJson) {
        TechnicalAssessmentResult result = new TechnicalAssessmentResult();
        result.setUserId(userId);
        result.setCareerTrack(careerTrack);
        result.setProficiencyLevel(proficiencyLevel);
        result.setTotalScore(totalScore);
        result.setMaxScore(maxScore);
        result.setPercentageScore(percentageScore);
        result.setSummary(summary);
        result.setSkillAreaJson(skillAreaJson);

        ReflectionTestUtils.setField(result, "id", id);
        ReflectionTestUtils.setField(result, "createdAt", createdAt);
        return result;
    }

    private String skillAreaJson(List<Map<String, Object>> snapshots) {
        try {
            return objectMapper.writeValueAsString(snapshots);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Could not create skill area json for test", ex);
        }
    }
}
