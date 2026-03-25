package com.careerplanning.backend.modules.career.service;

import com.careerplanning.backend.modules.auth.service.SimpleTokenService;
import com.careerplanning.backend.modules.career.dto.AssessmentAnswer;
import com.careerplanning.backend.modules.career.dto.SkillAreaScoreResponse;
import com.careerplanning.backend.modules.career.dto.SubmitTechnicalAssessmentRequest;
import com.careerplanning.backend.modules.career.dto.TechnicalAssessmentHistoryItemResponse;
import com.careerplanning.backend.modules.career.dto.TechnicalAssessmentOptionResponse;
import com.careerplanning.backend.modules.career.dto.TechnicalAssessmentProgressResponse;
import com.careerplanning.backend.modules.career.dto.TechnicalAssessmentQuestionResponse;
import com.careerplanning.backend.modules.career.dto.TechnicalAssessmentResultResponse;
import com.careerplanning.backend.modules.career.entity.TechnicalAssessmentOption;
import com.careerplanning.backend.modules.career.entity.TechnicalAssessmentQuestion;
import com.careerplanning.backend.modules.career.entity.TechnicalAssessmentResult;
import com.careerplanning.backend.modules.career.repository.TechnicalAssessmentOptionRepository;
import com.careerplanning.backend.modules.career.repository.TechnicalAssessmentQuestionRepository;
import com.careerplanning.backend.modules.career.repository.TechnicalAssessmentResultRepository;
import com.careerplanning.backend.modules.users.entity.User;
import com.careerplanning.backend.modules.users.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TechnicalAssessmentService {

    private static final int REASSESSMENT_INTERVAL_DAYS = 30;
    private static final TypeReference<List<SkillAreaSnapshot>> SKILL_AREA_TYPE = new TypeReference<>() {};

    private final SimpleTokenService simpleTokenService;
    private final UserRepository userRepository;
    private final TechnicalAssessmentResultRepository technicalAssessmentResultRepository;
    private final TechnicalAssessmentQuestionRepository technicalAssessmentQuestionRepository;
    private final TechnicalAssessmentOptionRepository technicalAssessmentOptionRepository;
    private final CareerTrackCatalogService careerTrackCatalogService;
    private final ObjectMapper objectMapper;

    public TechnicalAssessmentService(SimpleTokenService simpleTokenService,
                                      UserRepository userRepository,
                                      TechnicalAssessmentResultRepository technicalAssessmentResultRepository,
                                      TechnicalAssessmentQuestionRepository technicalAssessmentQuestionRepository,
                                      TechnicalAssessmentOptionRepository technicalAssessmentOptionRepository,
                                      CareerTrackCatalogService careerTrackCatalogService,
                                      ObjectMapper objectMapper) {
        this.simpleTokenService = simpleTokenService;
        this.userRepository = userRepository;
        this.technicalAssessmentResultRepository = technicalAssessmentResultRepository;
        this.technicalAssessmentQuestionRepository = technicalAssessmentQuestionRepository;
        this.technicalAssessmentOptionRepository = technicalAssessmentOptionRepository;
        this.careerTrackCatalogService = careerTrackCatalogService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<String> listSupportedTracks() {
        Set<String> availableTracks = new LinkedHashSet<>(technicalAssessmentQuestionRepository.findDistinctCareerTracksByActiveTrue());
        List<String> configuredTracks = careerTrackCatalogService.listTechnicalTracks();
        List<String> orderedTracks = configuredTracks.stream()
                .filter(availableTracks::contains)
                .toList();
        return orderedTracks.isEmpty() ? configuredTracks : orderedTracks;
    }

    @Transactional(readOnly = true)
    public List<TechnicalAssessmentQuestionResponse> getQuestions(String careerTrack) {
        String normalizedTrack = careerTrackCatalogService.validateTechnicalCareerTrack(careerTrack);
        List<TechnicalAssessmentQuestion> questions = technicalAssessmentQuestionRepository
                .findByCareerTrackAndActiveTrueOrderByDisplayOrderAscIdAsc(normalizedTrack);
        Map<Long, List<TechnicalAssessmentOption>> optionsByQuestionId = loadOptionsByQuestionId(questions.stream()
                .map(TechnicalAssessmentQuestion::getId)
                .toList());
        return questions.stream()
                .map(question -> toQuestionResponse(question, optionsByQuestionId.getOrDefault(question.getId(), List.of())))
                .toList();
    }

    @Transactional
    public TechnicalAssessmentResultResponse submitAssessment(String token, SubmitTechnicalAssessmentRequest request) {
        Long userId = simpleTokenService.getUserId(token);
        findUser(userId);

        String careerTrack = careerTrackCatalogService.validateTechnicalCareerTrack(request.careerTrack());
        List<TechnicalAssessmentQuestion> questions = technicalAssessmentQuestionRepository
                .findByCareerTrackAndActiveTrueOrderByDisplayOrderAscIdAsc(careerTrack);
        if (questions.isEmpty()) {
            throw new IllegalArgumentException("No technical assessment is configured for this role yet.");
        }

        Map<Long, List<TechnicalAssessmentOption>> optionsByQuestionId = loadOptionsByQuestionId(questions.stream()
                .map(TechnicalAssessmentQuestion::getId)
                .toList());
        Map<Long, TechnicalAssessmentOption> optionsById = optionsByQuestionId.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(TechnicalAssessmentOption::getId, Function.identity()));

        Map<Long, Long> answerByQuestion = new LinkedHashMap<>();
        for (AssessmentAnswer answer : request.answers()) {
            answerByQuestion.put(answer.questionId(), answer.optionId());
        }
        if (answerByQuestion.size() != request.answers().size()) {
            throw new IllegalArgumentException("Duplicate question answers are not allowed");
        }

        Set<Long> expectedQuestionIds = questions.stream()
                .map(TechnicalAssessmentQuestion::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (!answerByQuestion.keySet().equals(expectedQuestionIds)) {
            throw new IllegalArgumentException("Answer every technical question for the selected role.");
        }

        int totalScore = 0;
        int maxScore = 0;
        Map<String, Integer> skillScore = new LinkedHashMap<>();
        Map<String, Integer> skillMax = new LinkedHashMap<>();

        for (TechnicalAssessmentQuestion question : questions) {
            Long optionId = answerByQuestion.get(question.getId());
            TechnicalAssessmentOption option = optionsById.get(optionId);
            if (option == null || !question.getId().equals(option.getQuestionId())) {
                throw new IllegalArgumentException("Selected option does not match the question");
            }

            int questionPoints = question.getDifficulty().points();
            maxScore += questionPoints;
            skillMax.merge(question.getSkillArea(), questionPoints, Integer::sum);

            if (option.isCorrect()) {
                totalScore += questionPoints;
                skillScore.merge(question.getSkillArea(), questionPoints, Integer::sum);
            } else {
                skillScore.putIfAbsent(question.getSkillArea(), 0);
            }
        }

        int percentageScore = maxScore == 0 ? 0 : (int) Math.round((totalScore * 100.0) / maxScore);
        String proficiencyLevel = resolveProficiencyLevel(percentageScore);
        List<SkillAreaScoreResponse> skillAreas = buildSkillAreaResponses(skillScore, skillMax);

        Integer previousScore = technicalAssessmentResultRepository
                .findFirstByUserIdAndCareerTrackOrderByCreatedAtDesc(userId, careerTrack)
                .map(TechnicalAssessmentResult::getPercentageScore)
                .orElse(null);
        Integer improvement = previousScore == null ? null : percentageScore - previousScore;

        String summary = buildPerformanceSummary(careerTrack, proficiencyLevel, skillAreas, improvement);

        TechnicalAssessmentResult result = new TechnicalAssessmentResult();
        result.setUserId(userId);
        result.setCareerTrack(careerTrack);
        result.setProficiencyLevel(proficiencyLevel);
        result.setTotalScore(totalScore);
        result.setMaxScore(maxScore);
        result.setPercentageScore(percentageScore);
        result.setSkillAreaJson(writeSkillAreaJson(skillAreas));
        result.setSummary(summary);

        TechnicalAssessmentResult saved = technicalAssessmentResultRepository.save(result);
        return new TechnicalAssessmentResultResponse(
                careerTrack,
                proficiencyLevel,
                totalScore,
                maxScore,
                percentageScore,
                improvement,
                skillAreas,
                summary,
                saved.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public TechnicalAssessmentProgressResponse getProgress(String token) {
        Long userId = simpleTokenService.getUserId(token);
        return getProgressForUser(userId);
    }

    @Transactional(readOnly = true)
    public TechnicalAssessmentProgressResponse getProgressForUser(Long userId) {
        User user = findUser(userId);

        Optional<TechnicalAssessmentResult> latestForCurrentTrack = technicalAssessmentResultRepository
                .findFirstByUserIdAndCareerTrackOrderByCreatedAtDesc(userId, user.getCareerTrack());
        TechnicalAssessmentResult latest = latestForCurrentTrack
                .or(() -> technicalAssessmentResultRepository.findFirstByUserIdOrderByCreatedAtDesc(userId))
                .orElse(null);

        if (latest == null) {
            return new TechnicalAssessmentProgressResponse(
                    user.getCareerTrack(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    false,
                    "Take a role-specific technical benchmark to establish your baseline.",
                    List.of(),
                    List.of()
            );
        }

        List<TechnicalAssessmentResult> historyResults = technicalAssessmentResultRepository
                .findTop8ByUserIdAndCareerTrackOrderByCreatedAtDesc(userId, latest.getCareerTrack());
        List<TechnicalAssessmentHistoryItemResponse> history = historyResults.stream()
                .sorted(Comparator.comparing(TechnicalAssessmentResult::getCreatedAt))
                .map(result -> new TechnicalAssessmentHistoryItemResponse(
                        result.getCreatedAt(),
                        result.getPercentageScore(),
                        result.getProficiencyLevel()
                ))
                .toList();

        Integer previousScore = historyResults.size() > 1 ? historyResults.get(1).getPercentageScore() : null;
        Integer improvement = previousScore == null ? null : latest.getPercentageScore() - previousScore;
        Instant recommendedReassessmentAt = latest.getCreatedAt().plus(REASSESSMENT_INTERVAL_DAYS, ChronoUnit.DAYS);

        return new TechnicalAssessmentProgressResponse(
                latest.getCareerTrack(),
                latest.getProficiencyLevel(),
                latest.getPercentageScore(),
                previousScore,
                improvement,
                latest.getCreatedAt(),
                recommendedReassessmentAt,
                Instant.now().isAfter(recommendedReassessmentAt),
                latest.getSummary(),
                readSkillAreaJson(latest.getSkillAreaJson()),
                history
        );
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private Map<Long, List<TechnicalAssessmentOption>> loadOptionsByQuestionId(List<Long> questionIds) {
        if (questionIds.isEmpty()) {
            return Map.of();
        }
        return technicalAssessmentOptionRepository.findByQuestionIdInOrderByQuestionIdAscSortOrderAscIdAsc(questionIds)
                .stream()
                .collect(Collectors.groupingBy(TechnicalAssessmentOption::getQuestionId, LinkedHashMap::new, Collectors.toList()));
    }

    private TechnicalAssessmentQuestionResponse toQuestionResponse(TechnicalAssessmentQuestion question,
                                                                   List<TechnicalAssessmentOption> options) {
        return new TechnicalAssessmentQuestionResponse(
                question.getId(),
                question.getCareerTrack(),
                question.getSkillArea(),
                question.getDifficulty().name(),
                question.getQuestionText(),
                options.stream()
                        .map(option -> new TechnicalAssessmentOptionResponse(option.getId(), option.getOptionText()))
                        .toList()
        );
    }

    private List<SkillAreaScoreResponse> buildSkillAreaResponses(Map<String, Integer> skillScore,
                                                                 Map<String, Integer> skillMax) {
        return skillMax.entrySet().stream()
                .map(entry -> {
                    int score = skillScore.getOrDefault(entry.getKey(), 0);
                    int max = entry.getValue();
                    int percentage = max == 0 ? 0 : (int) Math.round((score * 100.0) / max);
                    return new SkillAreaScoreResponse(entry.getKey(), score, max, percentage);
                })
                .sorted(Comparator.comparing(SkillAreaScoreResponse::percentageScore).reversed())
                .toList();
    }

    private String resolveProficiencyLevel(int percentageScore) {
        if (percentageScore >= 85) {
            return "EXPERT";
        }
        if (percentageScore >= 70) {
            return "ADVANCED";
        }
        if (percentageScore >= 45) {
            return "INTERMEDIATE";
        }
        return "BEGINNER";
    }

    private String buildPerformanceSummary(String careerTrack,
                                           String proficiencyLevel,
                                           List<SkillAreaScoreResponse> skillAreas,
                                           Integer improvement) {
        if (skillAreas.isEmpty()) {
            return "No skill signals were captured for this benchmark.";
        }

        SkillAreaScoreResponse strongest = skillAreas.get(0);
        SkillAreaScoreResponse weakest = skillAreas.get(skillAreas.size() - 1);
        String recommendation = recommendationForArea(careerTrack, weakest.skillArea());

        StringBuilder summary = new StringBuilder();
        summary.append("Your current ")
                .append(humanize(careerTrack))
                .append(" benchmark is ")
                .append(humanize(proficiencyLevel))
                .append(". Strongest area: ")
                .append(strongest.skillArea())
                .append(" (")
                .append(strongest.percentageScore())
                .append("%). Focus next on ")
                .append(weakest.skillArea())
                .append(". ")
                .append(recommendation);

        if (improvement != null) {
            if (improvement > 0) {
                summary.append(" You improved by ").append(improvement).append(" percentage points since the last evaluation.");
            } else if (improvement < 0) {
                summary.append(" Your score is ").append(Math.abs(improvement)).append(" points below your previous attempt, so revisit core concepts before the next reassessment.");
            } else {
                summary.append(" Your score is unchanged from the previous benchmark, so increase the difficulty of your next study sprint.");
            }
        }

        return summary.toString();
    }

    private String recommendationForArea(String careerTrack, String skillArea) {
        String track = careerTrack == null ? "" : careerTrack.trim().toUpperCase(Locale.ROOT);
        String area = skillArea == null ? "" : skillArea.trim().toUpperCase(Locale.ROOT);

        return switch (track) {
            case "JAVA_BACKEND_DEVELOPER" -> switch (area) {
                case "SPRING ARCHITECTURE" -> "Practice designing service boundaries, transaction scope, and dependency injection with a multi-module Spring Boot service.";
                case "API DESIGN" -> "Review REST contracts, idempotency, validation, and error-handling conventions by redesigning one production-style endpoint.";
                case "PERSISTENCE" -> "Work on JPA fetch strategy, indexing, and query analysis with explain plans on a realistic relational schema.";
                case "SECURITY" -> "Strengthen authentication, authorization, and secure data-handling patterns around tokens, passwords, and server-side validation.";
                default -> "Build one backend feature end to end, then profile the slowest query path and document the tradeoffs you made.";
            };
            case "FRONTEND_DEVELOPER" -> switch (area) {
                case "PERFORMANCE" -> "Practice bundle splitting, caching, image strategy, and rendering cost analysis using real Lighthouse findings.";
                case "ACCESSIBILITY" -> "Audit one UI with keyboard-only navigation, labels, landmarks, and focus management until the main accessibility issues are gone.";
                case "STATE MANAGEMENT" -> "Model async UI states explicitly, then remove hidden coupling between form state, server responses, and rendering branches.";
                case "SECURITY" -> "Review XSS, token handling, and safe DOM updates with sanitized rendering and strict client-server boundaries.";
                default -> "Rebuild one dashboard screen with measurable improvements in layout stability, interaction handling, and render performance.";
            };
            case "FULL_STACK_DEVELOPER" -> switch (area) {
                case "SYSTEM DESIGN" -> "Trace one feature across frontend, API, database, and deployment so you can explain consistency, failure modes, and ownership boundaries.";
                case "DELIVERY" -> "Improve CI, test layering, and migration discipline so releases remain safe when UI, API, and schema evolve together.";
                case "OPERATIONS" -> "Correlate browser timing, API latency, logs, and query performance for one slow flow instead of debugging layers in isolation.";
                case "AUTHENTICATION" -> "Review session, token, and authorization flows end to end and verify where trust boundaries actually live.";
                default -> "Ship one small feature alone from schema to UI and capture every integration decision you had to make.";
            };
            case "DATA_SCIENTIST" -> switch (area) {
                case "EXPERIMENT DESIGN" -> "Revisit train-validation-test discipline, leakage prevention, and metric selection with one imbalanced business problem.";
                case "MODELING" -> "Compare simple and complex baselines, then document why the chosen model generalizes on unseen data.";
                case "DATA QUALITY" -> "Work on missing data handling, leakage checks, outlier strategy, and reproducible preprocessing pipelines.";
                case "MONITORING" -> "Define model drift, feature drift, and performance alerts for one deployed-style inference workflow.";
                default -> "Take one notebook and convert it into a reproducible pipeline with explicit validation and post-deployment monitoring assumptions.";
            };
            case "DEVOPS_ENGINEER" -> switch (area) {
                case "CI_CD" -> "Improve build gates, deployment rollback strategy, and pipeline observability so failures are caught before rollout.";
                case "KUBERNETES" -> "Practice readiness/liveness probes, rollout strategy, and resource tuning on a containerized service with failure drills.";
                case "SECURITY" -> "Tighten secrets handling, IAM least privilege, and auditability across automation and runtime infrastructure.";
                case "OBSERVABILITY" -> "Define actionable logs, metrics, traces, and SLO-driven alerts for one service instead of collecting telemetry without purpose.";
                default -> "Provision one environment as code, deploy through CI, and verify health, rollback, and alerting under a controlled failure scenario.";
            };
            default -> "Convert your weakest skill area into one scoped project with measurable acceptance criteria before the next reassessment.";
        };
    }

    private String writeSkillAreaJson(List<SkillAreaScoreResponse> skillAreas) {
        List<SkillAreaSnapshot> snapshots = skillAreas.stream()
                .map(skill -> new SkillAreaSnapshot(skill.skillArea(), skill.score(), skill.maxScore(), skill.percentageScore()))
                .toList();
        try {
            return objectMapper.writeValueAsString(snapshots);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Could not store technical assessment result");
        }
    }

    private List<SkillAreaScoreResponse> readSkillAreaJson(String skillAreaJson) {
        try {
            List<SkillAreaSnapshot> snapshots = objectMapper.readValue(skillAreaJson, SKILL_AREA_TYPE);
            return snapshots.stream()
                    .map(snapshot -> new SkillAreaScoreResponse(
                            snapshot.skillArea(),
                            snapshot.score(),
                            snapshot.maxScore(),
                            snapshot.percentageScore()
                    ))
                    .toList();
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Could not read technical assessment history", ex);
        }
    }

    private String humanize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return Arrays.stream(value.toLowerCase(Locale.ROOT).split("_"))
                .map(part -> part.isBlank() ? part : Character.toUpperCase(part.charAt(0)) + part.substring(1))
                .collect(Collectors.joining(" "));
    }

    private record SkillAreaSnapshot(String skillArea, int score, int maxScore, int percentageScore) {
    }
}
