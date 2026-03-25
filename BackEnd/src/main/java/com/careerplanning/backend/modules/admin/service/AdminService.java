package com.careerplanning.backend.modules.admin.service;

import com.careerplanning.backend.modules.admin.dto.AdminCareerGoalTemplateResponse;
import com.careerplanning.backend.modules.admin.dto.AdminCareerPathResponse;
import com.careerplanning.backend.modules.admin.dto.AdminCareerPhaseResponse;
import com.careerplanning.backend.modules.admin.dto.AdminCreateCareerGoalTemplateRequest;
import com.careerplanning.backend.modules.admin.dto.AdminCreateCareerPhaseRequest;
import com.careerplanning.backend.modules.admin.dto.AdminUpdateCareerGoalTemplateRequest;
import com.careerplanning.backend.modules.admin.dto.AdminUpdateCareerPhaseRequest;
import com.careerplanning.backend.modules.admin.dto.AdminUpdateUserRequest;
import com.careerplanning.backend.modules.admin.dto.AdminUserDetailResponse;
import com.careerplanning.backend.modules.admin.dto.AdminUserSummaryResponse;
import com.careerplanning.backend.modules.auth.service.AccessControlService;
import com.careerplanning.backend.modules.career.entity.CareerGoalTemplate;
import com.careerplanning.backend.modules.career.entity.CareerPhase;
import com.careerplanning.backend.modules.career.repository.CareerGoalTemplateRepository;
import com.careerplanning.backend.modules.career.repository.CareerPhaseRepository;
import com.careerplanning.backend.modules.career.service.CareerTrackCatalogService;
import com.careerplanning.backend.modules.career.service.TechnicalAssessmentService;
import com.careerplanning.backend.modules.goals.dto.CreateGoalRequest;
import com.careerplanning.backend.modules.goals.dto.CreateGoalTaskRequest;
import com.careerplanning.backend.modules.goals.dto.GoalResponse;
import com.careerplanning.backend.modules.goals.dto.UpdateGoalRequest;
import com.careerplanning.backend.modules.goals.dto.UpdateGoalTaskRequest;
import com.careerplanning.backend.modules.goals.entity.Goal;
import com.careerplanning.backend.modules.goals.entity.GoalCategory;
import com.careerplanning.backend.modules.goals.entity.GoalPriority;
import com.careerplanning.backend.modules.goals.entity.GoalStatus;
import com.careerplanning.backend.modules.goals.entity.GoalTask;
import com.careerplanning.backend.modules.goals.repository.GoalRepository;
import com.careerplanning.backend.modules.goals.repository.GoalTaskRepository;
import com.careerplanning.backend.modules.goals.service.GoalService;
import com.careerplanning.backend.modules.users.dto.UserProfileResponse;
import com.careerplanning.backend.modules.users.entity.User;
import com.careerplanning.backend.modules.users.entity.UserRole;
import com.careerplanning.backend.modules.users.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final AccessControlService accessControlService;
    private final UserRepository userRepository;
    private final GoalRepository goalRepository;
    private final GoalTaskRepository goalTaskRepository;
    private final GoalService goalService;
    private final CareerPhaseRepository careerPhaseRepository;
    private final CareerGoalTemplateRepository careerGoalTemplateRepository;
    private final CareerTrackCatalogService careerTrackCatalogService;
    private final TechnicalAssessmentService technicalAssessmentService;

    public AdminService(AccessControlService accessControlService,
                        UserRepository userRepository,
                        GoalRepository goalRepository,
                        GoalTaskRepository goalTaskRepository,
                        GoalService goalService,
                        CareerPhaseRepository careerPhaseRepository,
                        CareerGoalTemplateRepository careerGoalTemplateRepository,
                        CareerTrackCatalogService careerTrackCatalogService,
                        TechnicalAssessmentService technicalAssessmentService) {
        this.accessControlService = accessControlService;
        this.userRepository = userRepository;
        this.goalRepository = goalRepository;
        this.goalTaskRepository = goalTaskRepository;
        this.goalService = goalService;
        this.careerPhaseRepository = careerPhaseRepository;
        this.careerGoalTemplateRepository = careerGoalTemplateRepository;
        this.careerTrackCatalogService = careerTrackCatalogService;
        this.technicalAssessmentService = technicalAssessmentService;
    }

    public List<String> listRoles(String token) {
        requireAdmin(token);
        return UserRole.options();
    }

    @Transactional(readOnly = true)
    public List<AdminUserSummaryResponse> listUsers(String token) {
        requireAdmin(token);

        List<User> users = userRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Goal> goals = goalRepository.findAll();
        Map<Long, List<Goal>> goalsByUserId = goals.stream()
                .collect(Collectors.groupingBy(Goal::getUserId, LinkedHashMap::new, Collectors.toList()));

        List<Long> goalIds = goals.stream().map(Goal::getId).toList();
        List<GoalTask> tasks = goalIds.isEmpty()
                ? List.of()
                : goalTaskRepository.findByGoalIdInOrderByGoalIdAscSortOrderAscIdAsc(goalIds);
        Map<Long, List<GoalTask>> tasksByGoalId = tasks.stream()
                .collect(Collectors.groupingBy(GoalTask::getGoalId, LinkedHashMap::new, Collectors.toList()));

        return users.stream()
                .map(user -> mapUserSummary(user, goalsByUserId.getOrDefault(user.getId(), List.of()), tasksByGoalId))
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminUserDetailResponse getUserDetail(String token, Long userId) {
        requireAdmin(token);
        User user = findUser(userId);
        return buildUserDetail(user);
    }

    @Transactional
    public AdminUserDetailResponse updateUser(String token, Long userId, AdminUpdateUserRequest request) {
        requireAdmin(token);
        User user = findUser(userId);

        if (request.fullName() != null) {
            user.setFullName(normalizeRequiredText(request.fullName(), "fullName"));
        }
        if (request.email() != null) {
            String normalizedEmail = normalizeRequiredText(request.email(), "email");
            if (userRepository.existsByEmailAndIdNot(normalizedEmail, userId)) {
                throw new IllegalArgumentException("Email is already in use");
            }
            user.setEmail(normalizedEmail);
        }
        if (request.bio() != null) {
            user.setBio(toNullableTrimmed(request.bio()));
        }
        if (request.location() != null) {
            user.setLocation(toNullableTrimmed(request.location()));
        }
        if (request.role() != null) {
            user.setRole(validateAdminRole(request.role()));
        }
        if (request.careerTrack() != null) {
            user.setCareerTrack(careerTrackCatalogService.validateKnownCareerTrack(request.careerTrack()));
        }
        if (request.onboardingCompleted() != null) {
            user.setOnboardingCompleted(request.onboardingCompleted());
        }

        User saved = userRepository.save(user);
        return buildUserDetail(saved);
    }

    public GoalResponse createGoalForUser(String token, Long userId, CreateGoalRequest request) {
        requireAdmin(token);
        findUser(userId);
        return goalService.createGoalForUser(userId, request);
    }

    public GoalResponse updateGoalForUser(String token, Long userId, Long goalId, UpdateGoalRequest request) {
        requireAdmin(token);
        findUser(userId);
        return goalService.updateGoalForUser(userId, goalId, request);
    }

    public void deleteGoalForUser(String token, Long userId, Long goalId) {
        requireAdmin(token);
        findUser(userId);
        goalService.deleteGoalForUser(userId, goalId);
    }

    public GoalResponse createGoalTaskForUser(String token, Long userId, Long goalId, CreateGoalTaskRequest request) {
        requireAdmin(token);
        findUser(userId);
        return goalService.createGoalTaskForUser(userId, goalId, request);
    }

    public GoalResponse updateGoalTaskForUser(String token, Long userId, Long goalId, Long taskId, UpdateGoalTaskRequest request) {
        requireAdmin(token);
        findUser(userId);
        return goalService.updateGoalTaskForUser(userId, goalId, taskId, request);
    }

    public GoalResponse deleteGoalTaskForUser(String token, Long userId, Long goalId, Long taskId) {
        requireAdmin(token);
        findUser(userId);
        return goalService.deleteGoalTaskForUser(userId, goalId, taskId);
    }

    @Transactional(readOnly = true)
    public List<AdminCareerPathResponse> listCareerPaths(String token) {
        requireAdmin(token);

        List<CareerPhase> phases = careerPhaseRepository.findAllByOrderByCareerTrackAscPhaseOrderAscIdAsc();
        List<CareerGoalTemplate> templates = careerGoalTemplateRepository.findAllByOrderByCareerTrackAscPhaseIdAscDefaultOrderAscIdAsc();
        Map<Long, CareerPhase> phaseById = phases.stream()
                .collect(Collectors.toMap(CareerPhase::getId, phase -> phase, (left, right) -> left, LinkedHashMap::new));

        Set<String> orderedTracks = new LinkedHashSet<>();
        phases.stream().map(CareerPhase::getCareerTrack).forEach(orderedTracks::add);
        templates.stream().map(CareerGoalTemplate::getCareerTrack).forEach(orderedTracks::add);

        List<AdminCareerPathResponse> responses = new ArrayList<>();
        for (String careerTrack : orderedTracks) {
            List<AdminCareerPhaseResponse> phaseResponses = phases.stream()
                    .filter(phase -> phase.getCareerTrack().equals(careerTrack))
                    .map(this::toCareerPhaseResponse)
                    .toList();

            List<AdminCareerGoalTemplateResponse> templateResponses = templates.stream()
                    .filter(template -> template.getCareerTrack().equals(careerTrack))
                    .map(template -> toCareerGoalTemplateResponse(template, phaseById.get(template.getPhaseId())))
                    .toList();

            responses.add(new AdminCareerPathResponse(careerTrack, phaseResponses, templateResponses));
        }
        return responses;
    }

    @Transactional
    public AdminCareerPhaseResponse createCareerPhase(String token, AdminCreateCareerPhaseRequest request) {
        requireAdmin(token);

        String careerTrack = normalizeCareerTrackKey(request.careerTrack());
        validateUniquePhaseOrder(careerTrack, request.phaseOrder(), null);

        CareerPhase phase = new CareerPhase();
        phase.setCareerTrack(careerTrack);
        phase.setPhaseOrder(request.phaseOrder());
        phase.setPhaseTitle(normalizeRequiredText(request.phaseTitle(), "phaseTitle"));
        phase.setDescription(toNullableTrimmed(request.description()));

        return toCareerPhaseResponse(careerPhaseRepository.save(phase));
    }

    @Transactional
    public AdminCareerPhaseResponse updateCareerPhase(String token, Long phaseId, AdminUpdateCareerPhaseRequest request) {
        requireAdmin(token);

        CareerPhase phase = findPhase(phaseId);
        String nextCareerTrack = request.careerTrack() == null
                ? phase.getCareerTrack()
                : normalizeCareerTrackKey(request.careerTrack());
        Integer nextPhaseOrder = request.phaseOrder() == null ? phase.getPhaseOrder() : request.phaseOrder();

        validateUniquePhaseOrder(nextCareerTrack, nextPhaseOrder, phaseId);

        phase.setCareerTrack(nextCareerTrack);
        phase.setPhaseOrder(nextPhaseOrder);
        if (request.phaseTitle() != null) {
            phase.setPhaseTitle(normalizeRequiredText(request.phaseTitle(), "phaseTitle"));
        }
        if (request.description() != null) {
            phase.setDescription(toNullableTrimmed(request.description()));
        }

        CareerPhase saved = careerPhaseRepository.save(phase);

        List<CareerGoalTemplate> affectedTemplates = careerGoalTemplateRepository.findByPhaseIdOrderByDefaultOrderAscIdAsc(phaseId);
        if (!affectedTemplates.isEmpty()) {
            affectedTemplates.forEach(template -> template.setCareerTrack(nextCareerTrack));
            careerGoalTemplateRepository.saveAll(affectedTemplates);
        }

        return toCareerPhaseResponse(saved);
    }

    @Transactional
    public void deleteCareerPhase(String token, Long phaseId) {
        requireAdmin(token);
        careerPhaseRepository.delete(findPhase(phaseId));
    }

    @Transactional
    public AdminCareerGoalTemplateResponse createCareerGoalTemplate(String token, AdminCreateCareerGoalTemplateRequest request) {
        requireAdmin(token);

        CareerPhase phase = findPhase(request.phaseId());
        String careerTrack = normalizeCareerTrackKey(request.careerTrack());
        validateTemplateTrackMatchesPhase(careerTrack, phase);

        CareerGoalTemplate template = new CareerGoalTemplate();
        template.setCareerTrack(careerTrack);
        template.setPhaseId(phase.getId());
        template.setTitle(normalizeRequiredText(request.title(), "title"));
        template.setDescription(toNullableTrimmed(request.description()));
        template.setCategory(parseGoalCategory(request.category()).name());
        template.setPriority(parseGoalPriority(request.priority()).name());
        template.setDefaultOrder(request.defaultOrder());

        return toCareerGoalTemplateResponse(careerGoalTemplateRepository.save(template), phase);
    }

    @Transactional
    public AdminCareerGoalTemplateResponse updateCareerGoalTemplate(String token,
                                                                    Long templateId,
                                                                    AdminUpdateCareerGoalTemplateRequest request) {
        requireAdmin(token);

        CareerGoalTemplate template = findTemplate(templateId);
        Long nextPhaseId = request.phaseId() == null ? template.getPhaseId() : request.phaseId();
        CareerPhase phase = findPhase(nextPhaseId);

        String nextCareerTrack;
        if (request.careerTrack() != null) {
            nextCareerTrack = normalizeCareerTrackKey(request.careerTrack());
        } else if (request.phaseId() != null) {
            nextCareerTrack = phase.getCareerTrack();
        } else {
            nextCareerTrack = template.getCareerTrack();
        }
        validateTemplateTrackMatchesPhase(nextCareerTrack, phase);

        template.setCareerTrack(nextCareerTrack);
        template.setPhaseId(phase.getId());
        if (request.title() != null) {
            template.setTitle(normalizeRequiredText(request.title(), "title"));
        }
        if (request.description() != null) {
            template.setDescription(toNullableTrimmed(request.description()));
        }
        if (request.category() != null) {
            template.setCategory(parseGoalCategory(request.category()).name());
        }
        if (request.priority() != null) {
            template.setPriority(parseGoalPriority(request.priority()).name());
        }
        if (request.defaultOrder() != null) {
            template.setDefaultOrder(request.defaultOrder());
        }

        return toCareerGoalTemplateResponse(careerGoalTemplateRepository.save(template), phase);
    }

    @Transactional
    public void deleteCareerGoalTemplate(String token, Long templateId) {
        requireAdmin(token);
        careerGoalTemplateRepository.delete(findTemplate(templateId));
    }

    private void requireAdmin(String token) {
        accessControlService.requireAdmin(token);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private CareerPhase findPhase(Long phaseId) {
        return careerPhaseRepository.findById(phaseId)
                .orElseThrow(() -> new IllegalArgumentException("Career phase not found"));
    }

    private CareerGoalTemplate findTemplate(Long templateId) {
        return careerGoalTemplateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Career goal template not found"));
    }

    private AdminUserSummaryResponse mapUserSummary(User user,
                                                    List<Goal> goals,
                                                    Map<Long, List<GoalTask>> tasksByGoalId) {
        int goalCount = goals.size();
        int completedGoalCount = (int) goals.stream()
                .filter(goal -> goal.getStatus() == GoalStatus.COMPLETED)
                .count();

        int taskCount = 0;
        int completedTaskCount = 0;
        for (Goal goal : goals) {
            List<GoalTask> tasks = tasksByGoalId.getOrDefault(goal.getId(), List.of());
            taskCount += tasks.size();
            completedTaskCount += (int) tasks.stream().filter(task -> Boolean.TRUE.equals(task.getCompleted())).count();
        }

        return new AdminUserSummaryResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getCareerTrack(),
                user.isOnboardingCompleted(),
                user.getCreatedAt(),
                goalCount,
                completedGoalCount,
                taskCount,
                completedTaskCount
        );
    }

    private AdminUserDetailResponse buildUserDetail(User user) {
        return new AdminUserDetailResponse(
                toUserProfileResponse(user),
                goalService.listGoalsForUser(user.getId()),
                technicalAssessmentService.getProgressForUser(user.getId())
        );
    }

    private UserProfileResponse toUserProfileResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getCareerTrack(),
                user.isOnboardingCompleted(),
                user.getBio(),
                user.getLocation(),
                user.getCreatedAt()
        );
    }

    private AdminCareerPhaseResponse toCareerPhaseResponse(CareerPhase phase) {
        return new AdminCareerPhaseResponse(
                phase.getId(),
                phase.getCareerTrack(),
                phase.getPhaseOrder(),
                phase.getPhaseTitle(),
                phase.getDescription()
        );
    }

    private AdminCareerGoalTemplateResponse toCareerGoalTemplateResponse(CareerGoalTemplate template, CareerPhase phase) {
        return new AdminCareerGoalTemplateResponse(
                template.getId(),
                template.getCareerTrack(),
                template.getPhaseId(),
                phase == null ? null : phase.getPhaseOrder(),
                phase == null ? null : phase.getPhaseTitle(),
                template.getTitle(),
                template.getDescription(),
                template.getCategory(),
                template.getPriority(),
                template.getDefaultOrder()
        );
    }

    private String normalizeCareerTrackKey(String careerTrack) {
        String normalized = careerTrackCatalogService.normalizeTrackKey(careerTrack);
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("careerTrack must not be blank");
        }
        return normalized;
    }

    private String validateAdminRole(String role) {
        String normalized = role == null ? "" : role.trim().toUpperCase();
        if (!UserRole.isValid(normalized)) {
            throw new IllegalArgumentException("Invalid role. Use one of: " + String.join(", ", UserRole.options()));
        }
        return normalized;
    }

    private void validateUniquePhaseOrder(String careerTrack, Integer phaseOrder, Long phaseIdToIgnore) {
        boolean exists = phaseIdToIgnore == null
                ? careerPhaseRepository.existsByCareerTrackAndPhaseOrder(careerTrack, phaseOrder)
                : careerPhaseRepository.existsByCareerTrackAndPhaseOrderAndIdNot(careerTrack, phaseOrder, phaseIdToIgnore);
        if (exists) {
            throw new IllegalArgumentException("A phase with this order already exists for the selected career track");
        }
    }

    private void validateTemplateTrackMatchesPhase(String careerTrack, CareerPhase phase) {
        if (!phase.getCareerTrack().equals(careerTrack)) {
            throw new IllegalArgumentException("careerTrack must match the selected phase");
        }
    }

    private GoalCategory parseGoalCategory(String category) {
        String normalized = normalizeRequiredText(category, "category").toUpperCase();
        try {
            return GoalCategory.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid category. Use one of: " + allowedGoalCategories());
        }
    }

    private GoalPriority parseGoalPriority(String priority) {
        String normalized = normalizeRequiredText(priority, "priority").toUpperCase();
        try {
            return GoalPriority.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid priority. Use one of: " + allowedGoalPriorities());
        }
    }

    private String normalizeRequiredText(String value, String fieldName) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }

    private String toNullableTrimmed(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String allowedGoalCategories() {
        return List.of(GoalCategory.values()).stream().map(Enum::name).collect(Collectors.joining(", "));
    }

    private String allowedGoalPriorities() {
        return List.of(GoalPriority.values()).stream().map(Enum::name).collect(Collectors.joining(", "));
    }
}
