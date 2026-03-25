package com.careerplanning.backend.modules.career.service;

import com.careerplanning.backend.modules.auth.service.SimpleTokenService;
import com.careerplanning.backend.modules.career.dto.AssessCareerRequest;
import com.careerplanning.backend.modules.career.dto.AssessCareerResponse;
import com.careerplanning.backend.modules.career.dto.AssessmentAnswer;
import com.careerplanning.backend.modules.career.entity.AssessmentOption;
import com.careerplanning.backend.modules.career.entity.AssessmentQuestion;
import com.careerplanning.backend.modules.career.entity.AssessmentResult;
import com.careerplanning.backend.modules.career.repository.AssessmentOptionRepository;
import com.careerplanning.backend.modules.career.repository.AssessmentQuestionRepository;
import com.careerplanning.backend.modules.career.repository.AssessmentResultRepository;
import com.careerplanning.backend.modules.career.repository.CareerGoalTemplateRepository;
import com.careerplanning.backend.modules.career.repository.CareerPhaseRepository;
import com.careerplanning.backend.modules.goals.repository.GoalRepository;
import com.careerplanning.backend.modules.users.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CareerServiceTest {

    @Mock
    private SimpleTokenService simpleTokenService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AssessmentQuestionRepository assessmentQuestionRepository;
    @Mock
    private AssessmentOptionRepository assessmentOptionRepository;
    @Mock
    private AssessmentResultRepository assessmentResultRepository;
    @Mock
    private CareerPhaseRepository careerPhaseRepository;
    @Mock
    private CareerGoalTemplateRepository careerGoalTemplateRepository;
    @Mock
    private GoalRepository goalRepository;
    @Mock
    private CareerTrackCatalogService careerTrackCatalogService;

    private CareerService careerService;

    @BeforeEach
    void setUp() {
        careerService = new CareerService(
                simpleTokenService,
                userRepository,
                assessmentQuestionRepository,
                assessmentOptionRepository,
                assessmentResultRepository,
                careerPhaseRepository,
                careerGoalTemplateRepository,
                goalRepository,
                new ObjectMapper(),
                careerTrackCatalogService
        );
    }

    @Test
    void scoresAssessmentAnswersAndPersistsResult() {
        AssessmentOption optionOne = option(501L, 1L, "{\"SOFTWARE_ENGINEERING\":3,\"DATA_SCIENCE\":1}");
        AssessmentOption optionTwo = option(502L, 2L, "{\"SOFTWARE_ENGINEERING\":2,\"FULL_STACK_DEVELOPER\":2}");
        AssessmentQuestion questionOne = question(1L, 1, true);
        AssessmentQuestion questionTwo = question(2L, 2, true);

        when(simpleTokenService.getUserId("token-123")).thenReturn(42L);
        when(assessmentOptionRepository.findAllById(eq(List.of(501L, 502L))))
                .thenReturn(List.of(optionOne, optionTwo));
        when(assessmentQuestionRepository.findAllById(eq(List.of(1L, 2L))))
                .thenReturn(List.of(questionOne, questionTwo));
        when(assessmentOptionRepository.findByQuestionIdInOrderByQuestionIdAscIdAsc(eq(List.of(1L, 2L))))
                .thenReturn(List.of(optionOne, optionTwo));

        AssessCareerResponse response = careerService.assess(
                "token-123",
                new AssessCareerRequest(List.of(
                        new AssessmentAnswer(1L, 501L),
                        new AssessmentAnswer(2L, 502L)
                ))
        );

        assertEquals("SOFTWARE_ENGINEERING", response.recommendedTrack());
        assertEquals("SOFTWARE_ENGINEERING", response.recommendedCareer());
        assertEquals("SOFTWARE_ENGINEERING", response.matchPercentages().get(0).careerTrack());
        assertFalse(response.explanation().isBlank());

        ArgumentCaptor<AssessmentResult> resultCaptor = ArgumentCaptor.forClass(AssessmentResult.class);
        verify(assessmentResultRepository).save(resultCaptor.capture());
        assertEquals(42L, resultCaptor.getValue().getUserId());
        assertEquals("SOFTWARE_ENGINEERING", resultCaptor.getValue().getRecommendedTrack());
    }

    @Test
    void rejectsOptionIfQuestionMismatch() {
        AssessmentOption option = option(801L, 99L, "{\"SOFTWARE_ENGINEERING\":2}");
        AssessmentQuestion question = question(1L, 1, true);
        when(assessmentOptionRepository.findAllById(eq(List.of(801L))))
                .thenReturn(List.of(option));
        when(assessmentQuestionRepository.findAllById(eq(List.of(1L))))
                .thenReturn(List.of(question));
        when(assessmentOptionRepository.findByQuestionIdInOrderByQuestionIdAscIdAsc(eq(List.of(1L))))
                .thenReturn(List.of());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                careerService.getTopTracks(
                        new AssessCareerRequest(List.of(new AssessmentAnswer(1L, 801L))),
                        2
                )
        );

        assertEquals("Selected option does not match the question", exception.getMessage());
    }

    @Test
    void differentAnswerProfilesProduceDifferentRecommendationsAndScores() {
        AssessmentQuestion q1 = question(11L, 1, true);
        AssessmentQuestion q2 = question(12L, 2, true);
        AssessmentQuestion q3 = questionWithCluster(13L, 3, true, "ENGINEERING_CLUSTER");

        AssessmentOption q1Engineering = option(1101L, 11L, "{\"LOGICAL\":5,\"DETAIL_ORIENTED\":3}");
        AssessmentOption q1Design = option(1102L, 11L, "{\"CREATIVE\":5,\"COMMUNICATION\":3}");
        AssessmentOption q2Engineering = option(1201L, 12L, "{\"LOGICAL\":5,\"OPERATIONS_ORIENTED\":4}");
        AssessmentOption q2Design = option(1202L, 12L, "{\"CREATIVE\":5,\"COMMUNICATION\":4}");
        AssessmentOption q3Engineering = option(1301L, 13L, "{\"LOGICAL\":5,\"DETAIL_ORIENTED\":4,\"SOFTWARE_ENGINEERING\":3}");
        AssessmentOption q3Design = option(1302L, 13L, "{\"CREATIVE\":5,\"COMMUNICATION\":4,\"UI_UX_DESIGNER\":3}");

        Map<Long, AssessmentQuestion> questionStore = Map.of(
                11L, q1,
                12L, q2,
                13L, q3
        );
        Map<Long, AssessmentOption> optionStore = Map.of(
                1101L, q1Engineering,
                1102L, q1Design,
                1201L, q2Engineering,
                1202L, q2Design,
                1301L, q3Engineering,
                1302L, q3Design
        );
        Map<Long, List<AssessmentOption>> optionsByQuestion = new HashMap<>();
        optionsByQuestion.put(11L, List.of(q1Engineering, q1Design));
        optionsByQuestion.put(12L, List.of(q2Engineering, q2Design));
        optionsByQuestion.put(13L, List.of(q3Engineering, q3Design));

        when(simpleTokenService.getUserId("profile-token")).thenReturn(99L);
        when(assessmentQuestionRepository.findAllById(any())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            List<Long> ids = (List<Long>) invocation.getArgument(0);
            List<AssessmentQuestion> found = new ArrayList<>();
            for (Long id : ids) {
                if (questionStore.containsKey(id)) {
                    found.add(questionStore.get(id));
                }
            }
            return found;
        });
        when(assessmentOptionRepository.findAllById(any())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            List<Long> ids = (List<Long>) invocation.getArgument(0);
            List<AssessmentOption> found = new ArrayList<>();
            for (Long id : ids) {
                if (optionStore.containsKey(id)) {
                    found.add(optionStore.get(id));
                }
            }
            return found;
        });
        when(assessmentOptionRepository.findByQuestionIdInOrderByQuestionIdAscIdAsc(any())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            List<Long> ids = (List<Long>) invocation.getArgument(0);
            List<AssessmentOption> found = new ArrayList<>();
            for (Long questionId : ids) {
                found.addAll(optionsByQuestion.getOrDefault(questionId, List.of()));
            }
            return found;
        });

        AssessCareerResponse engineeringProfile = careerService.assess(
                "profile-token",
                new AssessCareerRequest(List.of(
                        new AssessmentAnswer(11L, 1101L),
                        new AssessmentAnswer(12L, 1201L),
                        new AssessmentAnswer(13L, 1301L)
                ))
        );

        AssessCareerResponse designProfile = careerService.assess(
                "profile-token",
                new AssessCareerRequest(List.of(
                        new AssessmentAnswer(11L, 1102L),
                        new AssessmentAnswer(12L, 1202L),
                        new AssessmentAnswer(13L, 1302L)
                ))
        );

        assertNotEquals(engineeringProfile.recommendedTrack(), designProfile.recommendedTrack());
        assertNotEquals(
                engineeringProfile.matchPercentages().stream().limit(3).toList(),
                designProfile.matchPercentages().stream().limit(3).toList()
        );
    }

    private AssessmentOption option(Long optionId, Long questionId, String weightJson) {
        AssessmentOption option = new AssessmentOption();
        option.setOptionText("Option");
        option.setWeightJson(weightJson);

        ReflectionTestUtils.setField(option, "id", optionId);
        ReflectionTestUtils.setField(option, "questionId", questionId);
        return option;
    }

    private AssessmentQuestion question(Long questionId, int stage, boolean active) {
        AssessmentQuestion question = new AssessmentQuestion();
        question.setStage(stage);
        question.setIsActive(active);
        question.setQuestionText("Question");

        ReflectionTestUtils.setField(question, "id", questionId);
        return question;
    }

    private AssessmentQuestion questionWithCluster(Long questionId, int stage, boolean active, String clusterKey) {
        AssessmentQuestion question = question(questionId, stage, active);
        question.setClusterKey(clusterKey);
        return question;
    }
}
