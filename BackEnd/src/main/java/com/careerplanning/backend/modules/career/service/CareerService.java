package com.careerplanning.backend.modules.career.service;

import com.careerplanning.backend.modules.auth.service.SimpleTokenService;
import com.careerplanning.backend.modules.career.dto.AssessCareerRequest;
import com.careerplanning.backend.modules.career.dto.AssessCareerResponse;
import com.careerplanning.backend.modules.career.dto.AssessmentAnswer;
import com.careerplanning.backend.modules.career.dto.AssessmentOptionResponse;
import com.careerplanning.backend.modules.career.dto.AssessmentQuestionResponse;
import com.careerplanning.backend.modules.career.dto.AssessmentTopTracksResponse;
import com.careerplanning.backend.modules.career.dto.CareerMatchPercentage;
import com.careerplanning.backend.modules.career.dto.CareerTrackConfirmationResponse;
import com.careerplanning.backend.modules.career.dto.ConfirmCareerTrackRequest;
import com.careerplanning.backend.modules.career.dto.GenerateRoadmapResponse;
import com.careerplanning.backend.modules.career.entity.AssessmentOption;
import com.careerplanning.backend.modules.career.entity.AssessmentQuestion;
import com.careerplanning.backend.modules.career.entity.AssessmentResult;
import com.careerplanning.backend.modules.career.entity.CareerGoalTemplate;
import com.careerplanning.backend.modules.career.entity.CareerPhase;
import com.careerplanning.backend.modules.career.repository.AssessmentOptionRepository;
import com.careerplanning.backend.modules.career.repository.AssessmentQuestionRepository;
import com.careerplanning.backend.modules.career.repository.AssessmentResultRepository;
import com.careerplanning.backend.modules.career.repository.CareerGoalTemplateRepository;
import com.careerplanning.backend.modules.career.repository.CareerPhaseRepository;
import com.careerplanning.backend.modules.goals.entity.Goal;
import com.careerplanning.backend.modules.goals.entity.GoalCategory;
import com.careerplanning.backend.modules.goals.entity.GoalPriority;
import com.careerplanning.backend.modules.goals.entity.GoalStatus;
import com.careerplanning.backend.modules.goals.repository.GoalRepository;
import com.careerplanning.backend.modules.users.entity.CareerTrack;
import com.careerplanning.backend.modules.users.entity.User;
import com.careerplanning.backend.modules.users.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CareerService {

    private static final TypeReference<Map<String, Integer>> WEIGHT_TYPE = new TypeReference<>() {};

    private static final Set<String> DIMENSION_KEYS = Set.of(
            "LOGICAL",
            "ANALYTICAL",
            "CREATIVE",
            "COMMUNICATION",
            "STRATEGIC",
            "SECURITY_ORIENTED",
            "OPERATIONS_ORIENTED",
            "DETAIL_ORIENTED"
    );

    private static final double STAGE_1_MULTIPLIER = 1.0;
    private static final double STAGE_2_MULTIPLIER = 1.15;
    private static final double STAGE_3_MULTIPLIER = 1.75;
    private static final double STAGE_3_CLUSTER_BONUS_MULTIPLIER = 0.35;

    private static final double CLUSTER_COMPONENT_WEIGHT = 0.55;
    private static final double TRACK_COMPONENT_WEIGHT = 0.45;
    private static final double RELATIVE_SPREAD_BLEND = 0.30;
    private static final double MIN_SPREAD_FOR_REBALANCE = 6.0;

    private static final String CLUSTER_ENGINEERING = "ENGINEERING_CLUSTER";
    private static final String CLUSTER_DATA = "DATA_CLUSTER";
    private static final String CLUSTER_PRODUCT_BUSINESS = "PRODUCT_BUSINESS_CLUSTER";
    private static final String CLUSTER_DESIGN = "DESIGN_CLUSTER";
    private static final String CLUSTER_INFRA_SECURITY = "INFRA_SECURITY_CLUSTER";

    private static final Map<String, String> TRACK_CLUSTER = buildTrackClusterMap();
    private static final Map<String, List<String>> CLUSTER_TRACKS = buildClusterTracksMap();
    private static final Map<String, Map<String, Double>> CLUSTER_DIMENSION_WEIGHTS = buildClusterDimensionWeights();
    private static final Map<String, Map<String, Double>> TRACK_DIMENSION_WEIGHTS = buildTrackDimensionWeights();

    private static final Map<String, String> DIMENSION_HUMAN_LABELS = Map.of(
            "LOGICAL", "logical problem solving",
            "ANALYTICAL", "analytical reasoning",
            "CREATIVE", "creative thinking",
            "COMMUNICATION", "communication skills",
            "STRATEGIC", "strategic decision making",
            "SECURITY_ORIENTED", "security awareness",
            "OPERATIONS_ORIENTED", "operations mindset",
            "DETAIL_ORIENTED", "attention to detail"
    );

    private final SimpleTokenService simpleTokenService;
    private final UserRepository userRepository;
    private final AssessmentQuestionRepository assessmentQuestionRepository;
    private final AssessmentOptionRepository assessmentOptionRepository;
    private final AssessmentResultRepository assessmentResultRepository;
    private final CareerPhaseRepository careerPhaseRepository;
    private final CareerGoalTemplateRepository careerGoalTemplateRepository;
    private final GoalRepository goalRepository;
    private final ObjectMapper objectMapper;
    private final CareerTrackCatalogService careerTrackCatalogService;

    public CareerService(SimpleTokenService simpleTokenService,
                         UserRepository userRepository,
                         AssessmentQuestionRepository assessmentQuestionRepository,
                         AssessmentOptionRepository assessmentOptionRepository,
                         AssessmentResultRepository assessmentResultRepository,
                         CareerPhaseRepository careerPhaseRepository,
                         CareerGoalTemplateRepository careerGoalTemplateRepository,
                         GoalRepository goalRepository,
                         ObjectMapper objectMapper,
                         CareerTrackCatalogService careerTrackCatalogService) {
        this.simpleTokenService = simpleTokenService;
        this.userRepository = userRepository;
        this.assessmentQuestionRepository = assessmentQuestionRepository;
        this.assessmentOptionRepository = assessmentOptionRepository;
        this.assessmentResultRepository = assessmentResultRepository;
        this.careerPhaseRepository = careerPhaseRepository;
        this.careerGoalTemplateRepository = careerGoalTemplateRepository;
        this.goalRepository = goalRepository;
        this.objectMapper = objectMapper;
        this.careerTrackCatalogService = careerTrackCatalogService;
    }

    public List<AssessmentQuestionResponse> getAssessmentQuestions(Integer stage,
                                                                   List<String> focusTracks,
                                                                   List<String> focusClusters) {
        if (stage != null && (stage < 1 || stage > 3)) {
            throw new IllegalArgumentException("stage must be between 1 and 3");
        }

        List<AssessmentQuestion> questions = stage == null
                ? assessmentQuestionRepository.findAllByIsActiveTrueOrderByStageAscIdAsc()
                : assessmentQuestionRepository.findByStageAndIsActiveTrueOrderByIdAsc(stage);

        if (questions.isEmpty()) {
            return List.of();
        }

        if (stage != null && stage == 3) {
            Set<String> prioritizedClusters = normalizeFocusClusters(focusClusters);
            if (prioritizedClusters.isEmpty() && focusTracks != null && !focusTracks.isEmpty()) {
                prioritizedClusters = new LinkedHashSet<>(topClustersForTracks(normalizeFocusTracks(focusTracks), 3));
            }

            if (!prioritizedClusters.isEmpty()) {
                questions = selectAdaptiveScenarioQuestions(questions, prioritizedClusters);
            } else {
                questions = questions.stream().limit(3).toList();
            }
        }

        List<Long> questionIds = questions.stream().map(AssessmentQuestion::getId).toList();
        List<AssessmentOption> options = assessmentOptionRepository.findByQuestionIdInOrderByQuestionIdAscIdAsc(questionIds);

        Map<Long, List<AssessmentOption>> optionsByQuestionId = options.stream()
                .collect(Collectors.groupingBy(AssessmentOption::getQuestionId, LinkedHashMap::new, Collectors.toList()));

        return questions.stream()
                .map(question -> new AssessmentQuestionResponse(
                        question.getId(),
                        question.getQuestionText(),
                        question.getStage(),
                        mapOptions(optionsByQuestionId.get(question.getId()))
                ))
                .toList();
    }

    public AssessmentTopTracksResponse getTopTracks(AssessCareerRequest request, int limit) {
        AssessmentComputation computation = computeAssessment(request.answers());
        List<CareerMatchPercentage> matchPercentages = computation.matchPercentages();

        List<String> topTracks = matchPercentages.stream()
                .limit(Math.max(1, limit))
                .map(CareerMatchPercentage::careerTrack)
                .toList();

        return new AssessmentTopTracksResponse(topTracks, computation.topClusters(), matchPercentages);
    }

    public AssessCareerResponse assess(String token, AssessCareerRequest request) {
        Long userId = simpleTokenService.getUserId(token);

        AssessmentComputation computation = computeAssessment(request.answers());
        persistAssessmentResult(userId, computation.matchPercentages(), computation.recommendedTrack());

        return new AssessCareerResponse(
                computation.matchPercentages(),
                computation.recommendedTrack(),
                computation.recommendedTrack(),
                computation.confidenceLevel(),
                computation.explanation()
        );
    }

    public CareerTrackConfirmationResponse confirmCareerTrack(String token, ConfirmCareerTrackRequest request) {
        Long userId = simpleTokenService.getUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String normalizedTrack = careerTrackCatalogService.validateKnownCareerTrack(request.careerTrack());

        user.setCareerTrack(normalizedTrack);
        user.setOnboardingCompleted(true);
        userRepository.save(user);
        return new CareerTrackConfirmationResponse(normalizedTrack);
    }

    public GenerateRoadmapResponse generateRoadmap(String token) {
        Long userId = simpleTokenService.getUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String careerTrack = user.getCareerTrack();
        List<CareerGoalTemplate> templates = careerGoalTemplateRepository.findOrderedByCareerTrack(careerTrack);

        if (templates.isEmpty()) {
            return new GenerateRoadmapResponse(careerTrack, 0, 0, 0,
                    "No blueprint templates configured for this career track yet.");
        }

        Map<Long, CareerPhase> phaseById = careerPhaseRepository.findByCareerTrackOrderByPhaseOrderAsc(careerTrack)
                .stream()
                .collect(Collectors.toMap(CareerPhase::getId, phase -> phase));

        List<Goal> existingBlueprintGoals = goalRepository.findByUserIdAndBlueprintGoalTrue(userId);
        Set<Long> existingTemplateIds = existingBlueprintGoals.stream()
                .map(Goal::getBlueprintTemplateId)
                .filter(value -> value != null)
                .collect(Collectors.toSet());

        String warning = buildTrackChangeWarning(careerTrack, existingBlueprintGoals);

        int skipped = 0;
        List<Goal> goalsToCreate = new ArrayList<>();
        for (CareerGoalTemplate template : templates) {
            if (existingTemplateIds.contains(template.getId())) {
                skipped++;
                continue;
            }

            CareerPhase phase = phaseById.get(template.getPhaseId());

            Goal goal = new Goal();
            goal.setUserId(userId);
            goal.setTitle(template.getTitle());
            goal.setDescription(template.getDescription());
            goal.setCategory(parseGoalCategory(template.getCategory()));
            goal.setPriority(parseGoalPriority(template.getPriority()));
            goal.setStatus(GoalStatus.PLANNED);
            goal.setProgress(0);
            goal.setBlueprintGoal(true);
            goal.setBlueprintTemplateId(template.getId());
            goal.setBlueprintDefaultOrder(template.getDefaultOrder());
            goal.setBlueprintPhaseOrder(phase == null ? null : phase.getPhaseOrder());
            goal.setBlueprintPhaseTitle(phase == null ? null : phase.getPhaseTitle());
            goal.setTags(new ArrayList<>());

            goalsToCreate.add(goal);
        }

        if (!goalsToCreate.isEmpty()) {
            goalRepository.saveAll(goalsToCreate);
        }

        if (warning == null && skipped > 0 && goalsToCreate.isEmpty()) {
            warning = "Blueprint goals already exist for this career track. No duplicates were created.";
        }

        return new GenerateRoadmapResponse(
                careerTrack,
                templates.size(),
                goalsToCreate.size(),
                skipped,
                warning
        );
    }

    private AssessmentComputation computeAssessment(List<AssessmentAnswer> answers) {
        if (answers == null || answers.isEmpty()) {
            throw new IllegalArgumentException("answers must not be empty");
        }
        if (answers.size() > 15) {
            throw new IllegalArgumentException("Assessment supports a maximum of 15 answers");
        }

        Map<Long, Long> answerByQuestion = new LinkedHashMap<>();
        for (AssessmentAnswer answer : answers) {
            answerByQuestion.put(answer.questionId(), answer.optionId());
        }
        if (answerByQuestion.size() != answers.size()) {
            throw new IllegalArgumentException("Duplicate question answers are not allowed");
        }

        List<Long> optionIds = new ArrayList<>(answerByQuestion.values());
        Map<Long, AssessmentOption> optionsById = assessmentOptionRepository.findAllById(optionIds)
                .stream()
                .collect(Collectors.toMap(AssessmentOption::getId, option -> option));

        if (optionsById.size() != optionIds.size()) {
            throw new IllegalArgumentException("One or more selected options are invalid");
        }

        List<Long> questionIds = new ArrayList<>(answerByQuestion.keySet());
        Map<Long, AssessmentQuestion> questionsById = assessmentQuestionRepository.findAllById(questionIds)
                .stream()
                .collect(Collectors.toMap(AssessmentQuestion::getId, question -> question));

        if (questionsById.size() != questionIds.size()) {
            throw new IllegalArgumentException("One or more selected questions are invalid");
        }

        for (Long questionId : questionIds) {
            AssessmentQuestion question = questionsById.get(questionId);
            if (question == null || !Boolean.TRUE.equals(question.getIsActive())) {
                throw new IllegalArgumentException("Assessment questions have changed. Refresh and try again.");
            }
        }

        List<AssessmentOption> optionsForAnsweredQuestions = assessmentOptionRepository
                .findByQuestionIdInOrderByQuestionIdAscIdAsc(questionIds);
        Map<Long, List<AssessmentOption>> optionsByQuestionId = optionsForAnsweredQuestions.stream()
                .collect(Collectors.groupingBy(AssessmentOption::getQuestionId, LinkedHashMap::new, Collectors.toList()));

        Map<String, Double> dimensionScores = initDimensionMap();
        Map<String, Double> maxDimensionScores = initDimensionMap();
        Map<String, Double> directTrackScores = initTrackDoubleMap();
        Map<String, Double> directTrackMaxScores = initTrackDoubleMap();
        Map<String, Double> stageThreeClusterBonusScores = initTrackDoubleMap();
        Map<String, Double> stageThreeClusterBonusMaxScores = initTrackDoubleMap();

        for (Map.Entry<Long, Long> answerEntry : answerByQuestion.entrySet()) {
            Long questionId = answerEntry.getKey();
            Long optionId = answerEntry.getValue();

            AssessmentQuestion question = questionsById.get(questionId);
            AssessmentOption selectedOption = optionsById.get(optionId);

            if (selectedOption == null || !questionId.equals(selectedOption.getQuestionId())) {
                throw new IllegalArgumentException("Selected option does not match the question");
            }

            double stageMultiplier = stageMultiplier(question.getStage());
            Map<String, Integer> selectedWeights = parseWeightJson(selectedOption.getWeightJson());
            applySelectedWeights(selectedWeights, stageMultiplier, dimensionScores, directTrackScores);

            List<AssessmentOption> candidateOptions = optionsByQuestionId.getOrDefault(questionId, List.of());
            updateQuestionMaximums(candidateOptions, stageMultiplier, maxDimensionScores, directTrackMaxScores);
            applyStageThreeClusterBonus(question,
                    selectedWeights,
                    candidateOptions,
                    stageMultiplier,
                    stageThreeClusterBonusScores,
                    stageThreeClusterBonusMaxScores);
        }

        Map<String, Double> clusterScores = weightedScores(CLUSTER_DIMENSION_WEIGHTS, dimensionScores);
        Map<String, Double> clusterMaxScores = weightedScores(CLUSTER_DIMENSION_WEIGHTS, maxDimensionScores);

        Map<String, Double> refinementScores = weightedScores(TRACK_DIMENSION_WEIGHTS, dimensionScores);
        Map<String, Double> refinementMaxScores = weightedScores(TRACK_DIMENSION_WEIGHTS, maxDimensionScores);

        Map<String, Double> normalizedCareerScores = new LinkedHashMap<>();
        Map<String, Double> rawCareerScores = new LinkedHashMap<>();

        for (CareerTrack track : CareerTrack.values()) {
            String trackName = track.name();
            String cluster = TRACK_CLUSTER.get(trackName);
            double clusterScore = clusterScores.getOrDefault(cluster, 0.0);
            double clusterMax = clusterMaxScores.getOrDefault(cluster, 0.0);

            double refinementScore = refinementScores.getOrDefault(trackName, 0.0);
            double refinementMax = refinementMaxScores.getOrDefault(trackName, 0.0);

            double directTrackScore = directTrackScores.getOrDefault(trackName, 0.0);
            double directTrackMax = directTrackMaxScores.getOrDefault(trackName, 0.0);
            double stageThreeBonusScore = stageThreeClusterBonusScores.getOrDefault(trackName, 0.0);
            double stageThreeBonusMax = stageThreeClusterBonusMaxScores.getOrDefault(trackName, 0.0);

            double rawScore = (clusterScore * CLUSTER_COMPONENT_WEIGHT)
                    + (refinementScore * TRACK_COMPONENT_WEIGHT)
                    + directTrackScore
                    + stageThreeBonusScore;

            double maxPossible = (clusterMax * CLUSTER_COMPONENT_WEIGHT)
                    + (refinementMax * TRACK_COMPONENT_WEIGHT)
                    + directTrackMax
                    + stageThreeBonusMax;

            rawCareerScores.put(trackName, rawScore);

            double percentage = maxPossible <= 0
                    ? 0
                    : Math.max(0, Math.min(100, (rawScore / maxPossible) * 100));
            normalizedCareerScores.put(trackName, percentage);
        }

        normalizedCareerScores = applyAdaptiveScoreSpread(normalizedCareerScores, rawCareerScores);

        List<CareerMatchPercentage> matchPercentages = toMatchPercentages(normalizedCareerScores, rawCareerScores);
        String recommendedTrack = matchPercentages.get(0).careerTrack();

        String confidenceLevel = calculateConfidenceLevel(matchPercentages);
        String explanation = buildExplanation(dimensionScores, matchPercentages, confidenceLevel);

        List<String> topClusters = clusterScores.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry<String, Double>::getValue).reversed())
                .map(Map.Entry::getKey)
                .limit(3)
                .toList();

        return new AssessmentComputation(matchPercentages, recommendedTrack, topClusters, confidenceLevel, explanation);
    }

    private List<AssessmentQuestion> selectAdaptiveScenarioQuestions(List<AssessmentQuestion> stageThreeQuestions,
                                                                     Set<String> prioritizedClusters) {
        if (stageThreeQuestions.isEmpty()) {
            return List.of();
        }

        List<String> priorityClusters = prioritizedClusters.stream().limit(3).toList();
        if (priorityClusters.isEmpty()) {
            return stageThreeQuestions.stream().limit(3).toList();
        }

        Map<String, AssessmentQuestion> byCluster = new LinkedHashMap<>();
        for (AssessmentQuestion question : stageThreeQuestions) {
            String clusterKey = normalizeText(question.getClusterKey());
            if (!clusterKey.isEmpty() && !byCluster.containsKey(clusterKey)) {
                byCluster.put(clusterKey, question);
            }
        }

        List<AssessmentQuestion> selected = new ArrayList<>();
        for (String cluster : priorityClusters) {
            AssessmentQuestion question = byCluster.get(cluster);
            if (question != null) {
                selected.add(question);
            }
        }

        if (selected.size() < 3) {
            Set<Long> selectedIds = selected.stream().map(AssessmentQuestion::getId).collect(Collectors.toSet());
            for (AssessmentQuestion question : stageThreeQuestions) {
                if (selectedIds.contains(question.getId())) {
                    continue;
                }
                selected.add(question);
                if (selected.size() >= 3) {
                    break;
                }
            }
        }

        return selected.stream().limit(3).toList();
    }

    private Set<String> normalizeFocusTracks(List<String> focusTracks) {
        Set<String> normalized = new LinkedHashSet<>();
        for (String rawValue : focusTracks) {
            if (rawValue == null || rawValue.isBlank()) {
                continue;
            }
            String[] parts = rawValue.split(",");
            for (String part : parts) {
                String track = normalizeText(part);
                if (CareerTrack.isValid(track)) {
                    normalized.add(track);
                }
            }
        }
        return normalized;
    }

    private Set<String> normalizeFocusClusters(List<String> focusClusters) {
        Set<String> normalized = new LinkedHashSet<>();
        if (focusClusters == null) {
            return normalized;
        }

        Set<String> validClusters = CLUSTER_DIMENSION_WEIGHTS.keySet();
        for (String rawValue : focusClusters) {
            if (rawValue == null || rawValue.isBlank()) {
                continue;
            }

            String[] parts = rawValue.split(",");
            for (String part : parts) {
                String cluster = normalizeText(part);
                if (validClusters.contains(cluster)) {
                    normalized.add(cluster);
                }
            }
        }
        return normalized;
    }

    private List<String> topClustersForTracks(Set<String> tracks, int limit) {
        LinkedHashSet<String> clusters = new LinkedHashSet<>();
        for (String track : tracks) {
            String cluster = TRACK_CLUSTER.get(track);
            if (cluster != null) {
                clusters.add(cluster);
            }
        }
        return clusters.stream().limit(Math.max(1, limit)).toList();
    }

    private void applyStageThreeClusterBonus(AssessmentQuestion question,
                                             Map<String, Integer> selectedWeights,
                                             List<AssessmentOption> candidateOptions,
                                             double stageMultiplier,
                                             Map<String, Double> bonusScores,
                                             Map<String, Double> bonusMaxScores) {
        if (question == null || question.getStage() == null || question.getStage() != 3) {
            return;
        }

        String clusterKey = normalizeText(question.getClusterKey());
        if (clusterKey.isEmpty() || !CLUSTER_TRACKS.containsKey(clusterKey)) {
            return;
        }

        double selectedAffinity = clusterAffinity(clusterKey, selectedWeights) * stageMultiplier * STAGE_3_CLUSTER_BONUS_MULTIPLIER;
        double maxAffinity = 0;
        for (AssessmentOption option : candidateOptions) {
            double optionAffinity = clusterAffinity(clusterKey, parseWeightJson(option.getWeightJson())) * stageMultiplier * STAGE_3_CLUSTER_BONUS_MULTIPLIER;
            maxAffinity = Math.max(maxAffinity, optionAffinity);
        }

        if (selectedAffinity <= 0 && maxAffinity <= 0) {
            return;
        }

        final double finalMaxAffinity = maxAffinity;
        for (String trackName : CLUSTER_TRACKS.getOrDefault(clusterKey, List.of())) {
            bonusScores.compute(trackName, (ignored, value) -> value + selectedAffinity);
            bonusMaxScores.compute(trackName, (ignored, value) -> value + finalMaxAffinity);
        }
    }

    private double clusterAffinity(String clusterKey, Map<String, Integer> selectedWeights) {
        Map<String, Double> clusterWeights = CLUSTER_DIMENSION_WEIGHTS.get(clusterKey);
        if (clusterWeights == null || clusterWeights.isEmpty() || selectedWeights == null || selectedWeights.isEmpty()) {
            return 0;
        }

        double affinity = 0;
        for (Map.Entry<String, Double> weight : clusterWeights.entrySet()) {
            int selected = Math.max(0, selectedWeights.getOrDefault(weight.getKey(), 0));
            affinity += selected * weight.getValue();
        }
        return affinity;
    }

    private Map<String, Double> applyAdaptiveScoreSpread(Map<String, Double> normalizedCareerScores,
                                                         Map<String, Double> rawCareerScores) {
        if (normalizedCareerScores.isEmpty() || rawCareerScores.isEmpty()) {
            return normalizedCareerScores;
        }

        double maxNormalized = normalizedCareerScores.values().stream().mapToDouble(Double::doubleValue).max().orElse(0);
        double minNormalized = normalizedCareerScores.values().stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double normalizedSpread = maxNormalized - minNormalized;

        if (normalizedSpread >= MIN_SPREAD_FOR_REBALANCE) {
            return normalizedCareerScores;
        }

        double maxRaw = rawCareerScores.values().stream().mapToDouble(Double::doubleValue).max().orElse(0);
        double minRaw = rawCareerScores.values().stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double rawSpread = maxRaw - minRaw;
        if (rawSpread <= 0) {
            return normalizedCareerScores;
        }

        Map<String, Double> adjusted = new LinkedHashMap<>();
        for (String trackName : normalizedCareerScores.keySet()) {
            double base = normalizedCareerScores.getOrDefault(trackName, 0.0);
            double relative = ((rawCareerScores.getOrDefault(trackName, 0.0) - minRaw) / rawSpread) * 100.0;
            double blended = (base * (1 - RELATIVE_SPREAD_BLEND)) + (relative * RELATIVE_SPREAD_BLEND);
            adjusted.put(trackName, Math.max(0, Math.min(100, blended)));
        }
        return adjusted;
    }

    private void applySelectedWeights(Map<String, Integer> selectedWeights,
                                      double stageMultiplier,
                                      Map<String, Double> dimensionScores,
                                      Map<String, Double> directTrackScores) {
        for (Map.Entry<String, Integer> entry : selectedWeights.entrySet()) {
            String key = normalizeText(entry.getKey());
            double value = Math.max(0, entry.getValue() == null ? 0 : entry.getValue()) * stageMultiplier;

            if (value <= 0) {
                continue;
            }

            if (DIMENSION_KEYS.contains(key)) {
                dimensionScores.compute(key, (ignored, score) -> score + value);
                continue;
            }

            if (CareerTrack.isValid(key)) {
                directTrackScores.compute(key, (ignored, score) -> score + value);
            }
        }
    }

    private void updateQuestionMaximums(List<AssessmentOption> candidateOptions,
                                        double stageMultiplier,
                                        Map<String, Double> maxDimensionScores,
                                        Map<String, Double> directTrackMaxScores) {
        List<Map<String, Integer>> parsedOptionWeights = candidateOptions.stream()
                .map(option -> parseWeightJson(option.getWeightJson()))
                .toList();

        for (String dimension : DIMENSION_KEYS) {
            double maxForDimension = 0;
            for (Map<String, Integer> optionWeights : parsedOptionWeights) {
                int rawValue = optionWeights.getOrDefault(dimension, 0);
                maxForDimension = Math.max(maxForDimension, Math.max(0, rawValue) * stageMultiplier);
            }
            maxDimensionScores.put(dimension, maxDimensionScores.getOrDefault(dimension, 0.0) + maxForDimension);
        }

        for (CareerTrack track : CareerTrack.values()) {
            String trackName = track.name();
            double maxForTrack = 0;
            for (Map<String, Integer> optionWeights : parsedOptionWeights) {
                int rawValue = optionWeights.getOrDefault(trackName, 0);
                maxForTrack = Math.max(maxForTrack, Math.max(0, rawValue) * stageMultiplier);
            }
            directTrackMaxScores.put(trackName, directTrackMaxScores.getOrDefault(trackName, 0.0) + maxForTrack);
        }
    }

    private List<CareerMatchPercentage> toMatchPercentages(Map<String, Double> normalizedCareerScores,
                                                           Map<String, Double> rawCareerScores) {
        List<String> sortedTracks = normalizedCareerScores.keySet().stream()
                .sorted(Comparator
                        .comparing((String trackName) -> normalizedCareerScores.get(trackName)).reversed()
                        .thenComparing(trackName -> rawCareerScores.getOrDefault(trackName, 0.0), Comparator.reverseOrder())
                        .thenComparing(Comparator.naturalOrder()))
                .toList();

        return sortedTracks.stream()
                .map(trackName -> new CareerMatchPercentage(
                        trackName,
                        (int) Math.round(normalizedCareerScores.getOrDefault(trackName, 0.0))
                ))
                .toList();
    }

    private String calculateConfidenceLevel(List<CareerMatchPercentage> matchPercentages) {
        if (matchPercentages.isEmpty()) {
            return "LOW";
        }

        int top = matchPercentages.get(0).percentage();
        int second = matchPercentages.size() > 1 ? matchPercentages.get(1).percentage() : 0;
        int gap = top - second;

        if (gap > 15) {
            return "HIGH";
        }
        if (gap >= 5) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private String buildExplanation(Map<String, Double> dimensionScores,
                                    List<CareerMatchPercentage> matchPercentages,
                                    String confidenceLevel) {
        List<String> strongestDimensions = dimensionScores.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry<String, Double>::getValue).reversed())
                .limit(3)
                .map(entry -> DIMENSION_HUMAN_LABELS.getOrDefault(entry.getKey(), entry.getKey().toLowerCase()))
                .toList();

        String topCareer = matchPercentages.isEmpty() ? "this path" : humanize(matchPercentages.get(0).careerTrack());
        String secondCareer = matchPercentages.size() > 1 ? humanize(matchPercentages.get(1).careerTrack()) : null;

        String dimensionsText = switch (strongestDimensions.size()) {
            case 0 -> "balanced capabilities";
            case 1 -> strongestDimensions.get(0);
            case 2 -> strongestDimensions.get(0) + " and " + strongestDimensions.get(1);
            default -> strongestDimensions.get(0) + ", " + strongestDimensions.get(1) + ", and " + strongestDimensions.get(2);
        };

        if ("LOW".equals(confidenceLevel) && secondCareer != null) {
            return "You demonstrated strong " + dimensionsText
                    + ". Your profile fits multiple paths, with close alignment between "
                    + topCareer + " and " + secondCareer
                    + ". Review both tracks before confirming your final choice.";
        }

        return "You demonstrated strong " + dimensionsText
                + ". These strengths align most with " + topCareer
                + " based on your assessment responses.";
    }

    private Map<String, Double> weightedScores(Map<String, Map<String, Double>> weightModel,
                                               Map<String, Double> dimensions) {
        Map<String, Double> scores = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, Double>> entry : weightModel.entrySet()) {
            double score = 0;
            for (Map.Entry<String, Double> weight : entry.getValue().entrySet()) {
                score += dimensions.getOrDefault(weight.getKey(), 0.0) * weight.getValue();
            }
            scores.put(entry.getKey(), score);
        }
        return scores;
    }

    private Map<String, Double> initDimensionMap() {
        Map<String, Double> map = new LinkedHashMap<>();
        for (String key : DIMENSION_KEYS) {
            map.put(key, 0.0);
        }
        return map;
    }

    private Map<String, Double> initTrackDoubleMap() {
        Map<String, Double> map = new LinkedHashMap<>();
        for (CareerTrack track : CareerTrack.values()) {
            map.put(track.name(), 0.0);
        }
        return map;
    }

    private double stageMultiplier(Integer stage) {
        if (stage == null) {
            return STAGE_1_MULTIPLIER;
        }
        return switch (stage) {
            case 2 -> STAGE_2_MULTIPLIER;
            case 3 -> STAGE_3_MULTIPLIER;
            default -> STAGE_1_MULTIPLIER;
        };
    }

    private String normalizeText(String text) {
        return text == null ? "" : text.trim().toUpperCase();
    }

    private String humanize(String value) {
        return Arrays.stream(normalizeText(value).split("_"))
                .filter(part -> !part.isBlank())
                .map(part -> part.charAt(0) + part.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }

    private GoalCategory parseGoalCategory(String value) {
        try {
            return GoalCategory.valueOf(value);
        } catch (Exception ignored) {
            throw new IllegalArgumentException("Invalid goal category in blueprint template: " + value);
        }
    }

    private GoalPriority parseGoalPriority(String value) {
        try {
            return GoalPriority.valueOf(value);
        } catch (Exception ignored) {
            throw new IllegalArgumentException("Invalid goal priority in blueprint template: " + value);
        }
    }

    private String buildTrackChangeWarning(String currentTrack, List<Goal> existingBlueprintGoals) {
        if (existingBlueprintGoals.isEmpty()) {
            return null;
        }

        Set<Long> templateIds = existingBlueprintGoals.stream()
                .map(Goal::getBlueprintTemplateId)
                .filter(value -> value != null)
                .collect(Collectors.toSet());

        if (templateIds.isEmpty()) {
            return null;
        }

        Set<String> existingTracks = careerGoalTemplateRepository.findAllById(templateIds)
                .stream()
                .map(CareerGoalTemplate::getCareerTrack)
                .collect(Collectors.toSet());

        boolean hasPreviousTrackGoals = existingTracks.stream().anyMatch(track -> !track.equals(currentTrack));
        if (hasPreviousTrackGoals) {
            return "Existing blueprint goals from previous career tracks were kept. Nothing was deleted.";
        }

        return null;
    }

    private void persistAssessmentResult(Long userId,
                                         List<CareerMatchPercentage> matchPercentages,
                                         String recommendedTrack) {
        Map<String, Integer> scoreMap = new LinkedHashMap<>();
        for (CareerMatchPercentage matchPercentage : matchPercentages) {
            scoreMap.put(matchPercentage.careerTrack(), matchPercentage.percentage());
        }

        AssessmentResult result = new AssessmentResult();
        result.setUserId(userId);
        result.setRecommendedTrack(recommendedTrack);
        result.setScoreJson(writeJson(scoreMap));

        assessmentResultRepository.save(result);
    }

    private List<AssessmentOptionResponse> mapOptions(List<AssessmentOption> options) {
        if (options == null) {
            return List.of();
        }
        return options.stream()
                .map(option -> new AssessmentOptionResponse(option.getId(), option.getOptionText()))
                .toList();
    }

    private Map<String, Integer> parseWeightJson(String json) {
        try {
            Map<String, Integer> parsed = objectMapper.readValue(json, WEIGHT_TYPE);
            Map<String, Integer> normalized = new LinkedHashMap<>();
            for (Map.Entry<String, Integer> entry : parsed.entrySet()) {
                String key = normalizeText(entry.getKey());
                normalized.put(key, entry.getValue());
            }
            return normalized;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid assessment weight configuration");
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Could not store assessment result");
        }
    }

    private static Map<String, String> buildTrackClusterMap() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put(CareerTrack.SOFTWARE_ENGINEERING.name(), CLUSTER_ENGINEERING);
        map.put(CareerTrack.JAVA_BACKEND_DEVELOPER.name(), CLUSTER_ENGINEERING);
        map.put(CareerTrack.FRONTEND_DEVELOPER.name(), CLUSTER_ENGINEERING);
        map.put(CareerTrack.FULL_STACK_DEVELOPER.name(), CLUSTER_ENGINEERING);

        map.put(CareerTrack.DATA_SCIENCE.name(), CLUSTER_DATA);
        map.put(CareerTrack.DATA_SCIENTIST.name(), CLUSTER_DATA);
        map.put(CareerTrack.DATA_ANALYST.name(), CLUSTER_DATA);
        map.put(CareerTrack.AI_ML_ENGINEER.name(), CLUSTER_DATA);

        map.put(CareerTrack.PRODUCT_MANAGEMENT.name(), CLUSTER_PRODUCT_BUSINESS);
        map.put(CareerTrack.PRODUCT_MANAGER.name(), CLUSTER_PRODUCT_BUSINESS);
        map.put(CareerTrack.BUSINESS_ANALYST.name(), CLUSTER_PRODUCT_BUSINESS);
        map.put(CareerTrack.MARKETING.name(), CLUSTER_PRODUCT_BUSINESS);

        map.put(CareerTrack.UI_UX_DESIGNER.name(), CLUSTER_DESIGN);
        map.put(CareerTrack.DESIGN.name(), CLUSTER_DESIGN);

        map.put(CareerTrack.DEVOPS_ENGINEER.name(), CLUSTER_INFRA_SECURITY);
        map.put(CareerTrack.CYBERSECURITY_ANALYST.name(), CLUSTER_INFRA_SECURITY);
        map.put(CareerTrack.QA_ENGINEER.name(), CLUSTER_INFRA_SECURITY);
        return map;
    }

    private static Map<String, List<String>> buildClusterTracksMap() {
        Map<String, List<String>> map = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : TRACK_CLUSTER.entrySet()) {
            String track = entry.getKey();
            String cluster = entry.getValue();
            map.computeIfAbsent(cluster, ignored -> new ArrayList<>()).add(track);
        }
        return map;
    }

    private static Map<String, Map<String, Double>> buildClusterDimensionWeights() {
        Map<String, Map<String, Double>> map = new LinkedHashMap<>();
        map.put(CLUSTER_ENGINEERING, weights(
                1.20, 0.80, 0.35, 0.35, 0.45, 0.45, 0.65, 1.00
        ));
        map.put(CLUSTER_DATA, weights(
                1.05, 1.35, 0.20, 0.50, 0.60, 0.25, 0.30, 1.00
        ));
        map.put(CLUSTER_PRODUCT_BUSINESS, weights(
                0.45, 0.85, 0.60, 1.20, 1.25, 0.20, 0.25, 0.55
        ));
        map.put(CLUSTER_DESIGN, weights(
                0.35, 0.45, 1.40, 0.95, 0.65, 0.15, 0.15, 0.85
        ));
        map.put(CLUSTER_INFRA_SECURITY, weights(
                0.95, 0.90, 0.10, 0.35, 0.50, 1.45, 1.25, 1.05
        ));
        return map;
    }

    private static Map<String, Map<String, Double>> buildTrackDimensionWeights() {
        Map<String, Map<String, Double>> map = new LinkedHashMap<>();
        map.put(CareerTrack.SOFTWARE_ENGINEERING.name(), weights(1.30, 0.90, 0.20, 0.30, 0.40, 0.30, 0.60, 1.00));
        map.put(CareerTrack.JAVA_BACKEND_DEVELOPER.name(), weights(1.40, 0.90, 0.10, 0.20, 0.30, 0.40, 0.80, 1.10));
        map.put(CareerTrack.FRONTEND_DEVELOPER.name(), weights(1.00, 0.60, 1.00, 0.60, 0.30, 0.20, 0.40, 0.90));
        map.put(CareerTrack.FULL_STACK_DEVELOPER.name(), weights(1.20, 0.80, 0.60, 0.50, 0.40, 0.30, 0.80, 1.00));

        map.put(CareerTrack.DATA_SCIENCE.name(), weights(1.10, 1.40, 0.20, 0.40, 0.60, 0.20, 0.30, 1.00));
        map.put(CareerTrack.DATA_SCIENTIST.name(), weights(1.10, 1.50, 0.20, 0.30, 0.50, 0.20, 0.30, 1.00));
        map.put(CareerTrack.DATA_ANALYST.name(), weights(0.80, 1.20, 0.30, 0.70, 0.70, 0.20, 0.30, 1.10));
        map.put(CareerTrack.AI_ML_ENGINEER.name(), weights(1.20, 1.50, 0.20, 0.30, 0.50, 0.20, 0.70, 1.00));

        map.put(CareerTrack.DEVOPS_ENGINEER.name(), weights(1.00, 0.80, 0.10, 0.30, 0.40, 0.90, 1.40, 1.00));
        map.put(CareerTrack.CYBERSECURITY_ANALYST.name(), weights(0.90, 1.00, 0.10, 0.40, 0.50, 1.60, 0.70, 1.00));
        map.put(CareerTrack.QA_ENGINEER.name(), weights(1.00, 0.90, 0.10, 0.50, 0.30, 0.50, 0.50, 1.40));

        map.put(CareerTrack.UI_UX_DESIGNER.name(), weights(0.30, 0.50, 1.50, 1.00, 0.60, 0.10, 0.10, 0.90));
        map.put(CareerTrack.DESIGN.name(), weights(0.30, 0.40, 1.40, 0.90, 0.50, 0.10, 0.10, 0.80));

        map.put(CareerTrack.PRODUCT_MANAGEMENT.name(), weights(0.40, 0.90, 0.40, 1.20, 1.40, 0.10, 0.30, 0.50));
        map.put(CareerTrack.PRODUCT_MANAGER.name(), weights(0.40, 0.90, 0.40, 1.20, 1.40, 0.10, 0.30, 0.50));
        map.put(CareerTrack.MARKETING.name(), weights(0.20, 0.70, 0.90, 1.20, 1.00, 0.10, 0.20, 0.50));
        map.put(CareerTrack.BUSINESS_ANALYST.name(), weights(0.70, 1.20, 0.20, 1.00, 0.90, 0.10, 0.20, 0.80));
        return map;
    }

    private static Map<String, Double> weights(double logical,
                                               double analytical,
                                               double creative,
                                               double communication,
                                               double strategic,
                                               double security,
                                               double operations,
                                               double detail) {
        Map<String, Double> map = new LinkedHashMap<>();
        map.put("LOGICAL", logical);
        map.put("ANALYTICAL", analytical);
        map.put("CREATIVE", creative);
        map.put("COMMUNICATION", communication);
        map.put("STRATEGIC", strategic);
        map.put("SECURITY_ORIENTED", security);
        map.put("OPERATIONS_ORIENTED", operations);
        map.put("DETAIL_ORIENTED", detail);
        return map;
    }

    private record AssessmentComputation(
            List<CareerMatchPercentage> matchPercentages,
            String recommendedTrack,
            List<String> topClusters,
            String confidenceLevel,
            String explanation
    ) {}
}
