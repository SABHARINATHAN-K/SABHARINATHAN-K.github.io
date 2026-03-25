package com.careerplanning.backend.modules.admin.service;

import com.careerplanning.backend.modules.admin.dto.AdminCreateTechnicalAssessmentQuestionRequest;
import com.careerplanning.backend.modules.admin.dto.AdminTechnicalAssessmentOptionRequest;
import com.careerplanning.backend.modules.admin.dto.AdminTechnicalAssessmentOptionResponse;
import com.careerplanning.backend.modules.admin.dto.AdminTechnicalAssessmentQuestionResponse;
import com.careerplanning.backend.modules.admin.dto.AdminUpdateTechnicalAssessmentQuestionRequest;
import com.careerplanning.backend.modules.auth.service.AccessControlService;
import com.careerplanning.backend.modules.career.entity.TechnicalAssessmentDifficulty;
import com.careerplanning.backend.modules.career.entity.TechnicalAssessmentOption;
import com.careerplanning.backend.modules.career.entity.TechnicalAssessmentQuestion;
import com.careerplanning.backend.modules.career.repository.TechnicalAssessmentOptionRepository;
import com.careerplanning.backend.modules.career.repository.TechnicalAssessmentQuestionRepository;
import com.careerplanning.backend.modules.career.service.CareerTrackCatalogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminTechnicalAssessmentService {

    private final AccessControlService accessControlService;
    private final TechnicalAssessmentQuestionRepository technicalAssessmentQuestionRepository;
    private final TechnicalAssessmentOptionRepository technicalAssessmentOptionRepository;
    private final CareerTrackCatalogService careerTrackCatalogService;

    public AdminTechnicalAssessmentService(AccessControlService accessControlService,
                                           TechnicalAssessmentQuestionRepository technicalAssessmentQuestionRepository,
                                           TechnicalAssessmentOptionRepository technicalAssessmentOptionRepository,
                                           CareerTrackCatalogService careerTrackCatalogService) {
        this.accessControlService = accessControlService;
        this.technicalAssessmentQuestionRepository = technicalAssessmentQuestionRepository;
        this.technicalAssessmentOptionRepository = technicalAssessmentOptionRepository;
        this.careerTrackCatalogService = careerTrackCatalogService;
    }

    @Transactional(readOnly = true)
    public List<AdminTechnicalAssessmentQuestionResponse> listQuestions(String token, String careerTrack) {
        requireAdmin(token);
        String normalizedTrack = careerTrackCatalogService.validateTechnicalCareerTrack(careerTrack);
        List<TechnicalAssessmentQuestion> questions = technicalAssessmentQuestionRepository
                .findByCareerTrackOrderByDisplayOrderAscIdAsc(normalizedTrack);
        Map<Long, List<TechnicalAssessmentOption>> optionsByQuestionId = loadOptions(questions);
        return questions.stream()
                .map(question -> toResponse(question, optionsByQuestionId.getOrDefault(question.getId(), List.of())))
                .toList();
    }

    @Transactional
    public AdminTechnicalAssessmentQuestionResponse createQuestion(String token,
                                                                   AdminCreateTechnicalAssessmentQuestionRequest request) {
        requireAdmin(token);
        String careerTrack = careerTrackCatalogService.validateTechnicalCareerTrack(request.careerTrack());
        ensureDisplayOrderAvailable(careerTrack, request.displayOrder(), null);
        List<AdminTechnicalAssessmentOptionRequest> optionRequests = validateOptions(request.options());

        TechnicalAssessmentQuestion question = new TechnicalAssessmentQuestion();
        question.setCareerTrack(careerTrack);
        question.setSkillArea(normalizeRequiredText(request.skillArea(), "skillArea"));
        question.setDifficulty(parseDifficulty(request.difficulty()));
        question.setQuestionText(normalizeRequiredText(request.questionText(), "questionText"));
        question.setExplanation(toNullableTrimmed(request.explanation()));
        question.setDisplayOrder(request.displayOrder());
        question.setActive(request.active() == null || request.active());

        TechnicalAssessmentQuestion saved = technicalAssessmentQuestionRepository.save(question);
        List<TechnicalAssessmentOption> options = saveOptions(saved.getId(), optionRequests);
        return toResponse(saved, options);
    }

    @Transactional
    public AdminTechnicalAssessmentQuestionResponse updateQuestion(String token,
                                                                   Long questionId,
                                                                   AdminUpdateTechnicalAssessmentQuestionRequest request) {
        requireAdmin(token);
        TechnicalAssessmentQuestion question = technicalAssessmentQuestionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Technical assessment question not found"));

        String updatedTrack = question.getCareerTrack();
        if (request.careerTrack() != null) {
            updatedTrack = careerTrackCatalogService.validateTechnicalCareerTrack(request.careerTrack());
        }

        Integer updatedDisplayOrder = request.displayOrder() != null ? request.displayOrder() : question.getDisplayOrder();
        ensureDisplayOrderAvailable(updatedTrack, updatedDisplayOrder, questionId);

        question.setCareerTrack(updatedTrack);
        question.setDisplayOrder(updatedDisplayOrder);
        if (request.skillArea() != null) {
            question.setSkillArea(normalizeRequiredText(request.skillArea(), "skillArea"));
        }
        if (request.difficulty() != null) {
            question.setDifficulty(parseDifficulty(request.difficulty()));
        }
        if (request.questionText() != null) {
            question.setQuestionText(normalizeRequiredText(request.questionText(), "questionText"));
        }
        if (request.explanation() != null) {
            question.setExplanation(toNullableTrimmed(request.explanation()));
        }
        if (request.active() != null) {
            question.setActive(request.active());
        }

        TechnicalAssessmentQuestion saved = technicalAssessmentQuestionRepository.save(question);

        if (request.options() != null) {
            List<AdminTechnicalAssessmentOptionRequest> optionRequests = validateOptions(request.options());
            technicalAssessmentOptionRepository.deleteByQuestionId(questionId);
            saveOptions(questionId, optionRequests);
        }

        List<TechnicalAssessmentOption> options = technicalAssessmentOptionRepository.findByQuestionIdOrderBySortOrderAscIdAsc(questionId);
        return toResponse(saved, options);
    }

    @Transactional
    public void deleteQuestion(String token, Long questionId) {
        requireAdmin(token);
        TechnicalAssessmentQuestion question = technicalAssessmentQuestionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Technical assessment question not found"));
        technicalAssessmentQuestionRepository.delete(question);
    }

    private void requireAdmin(String token) {
        accessControlService.requireAdmin(token);
    }

    private void ensureDisplayOrderAvailable(String careerTrack, Integer displayOrder, Long questionId) {
        boolean exists = questionId == null
                ? technicalAssessmentQuestionRepository.existsByCareerTrackAndDisplayOrder(careerTrack, displayOrder)
                : technicalAssessmentQuestionRepository.existsByCareerTrackAndDisplayOrderAndIdNot(careerTrack, displayOrder, questionId);
        if (exists) {
            throw new IllegalArgumentException("Another technical benchmark question already uses this display order for the selected role");
        }
    }

    private List<AdminTechnicalAssessmentOptionRequest> validateOptions(List<AdminTechnicalAssessmentOptionRequest> options) {
        if (options == null || options.size() != 4) {
            throw new IllegalArgumentException("Each technical benchmark question must include exactly 4 options");
        }
        long correctCount = options.stream().filter(option -> Boolean.TRUE.equals(option.correct())).count();
        if (correctCount != 1) {
            throw new IllegalArgumentException("Each technical benchmark question must have exactly 1 correct option");
        }
        return options;
    }

    private List<TechnicalAssessmentOption> saveOptions(Long questionId, List<AdminTechnicalAssessmentOptionRequest> optionRequests) {
        List<TechnicalAssessmentOption> options = java.util.stream.IntStream.range(0, optionRequests.size())
                .mapToObj(index -> {
                    AdminTechnicalAssessmentOptionRequest request = optionRequests.get(index);
                    TechnicalAssessmentOption option = new TechnicalAssessmentOption();
                    option.setQuestionId(questionId);
                    option.setOptionText(normalizeRequiredText(request.optionText(), "optionText"));
                    option.setCorrect(Boolean.TRUE.equals(request.correct()));
                    option.setSortOrder(index + 1);
                    return option;
                })
                .toList();
        return technicalAssessmentOptionRepository.saveAll(options);
    }

    private Map<Long, List<TechnicalAssessmentOption>> loadOptions(List<TechnicalAssessmentQuestion> questions) {
        List<Long> questionIds = questions.stream().map(TechnicalAssessmentQuestion::getId).toList();
        if (questionIds.isEmpty()) {
            return Map.of();
        }
        return technicalAssessmentOptionRepository.findByQuestionIdInOrderByQuestionIdAscSortOrderAscIdAsc(questionIds)
                .stream()
                .collect(Collectors.groupingBy(TechnicalAssessmentOption::getQuestionId, LinkedHashMap::new, Collectors.toList()));
    }

    private AdminTechnicalAssessmentQuestionResponse toResponse(TechnicalAssessmentQuestion question,
                                                                List<TechnicalAssessmentOption> options) {
        return new AdminTechnicalAssessmentQuestionResponse(
                question.getId(),
                question.getCareerTrack(),
                question.getSkillArea(),
                question.getDifficulty().name(),
                question.getQuestionText(),
                question.getExplanation(),
                question.getDisplayOrder(),
                question.isActive(),
                options.stream()
                        .map(option -> new AdminTechnicalAssessmentOptionResponse(
                                option.getId(),
                                option.getOptionText(),
                                option.isCorrect(),
                                option.getSortOrder()
                        ))
                        .toList()
        );
    }

    private TechnicalAssessmentDifficulty parseDifficulty(String value) {
        String normalized = normalizeRequiredText(value, "difficulty").toUpperCase(Locale.ROOT);
        try {
            return TechnicalAssessmentDifficulty.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid difficulty. Use one of: FOUNDATION, APPLIED, ARCHITECTURE");
        }
    }

    private String normalizeRequiredText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }

    private String toNullableTrimmed(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
