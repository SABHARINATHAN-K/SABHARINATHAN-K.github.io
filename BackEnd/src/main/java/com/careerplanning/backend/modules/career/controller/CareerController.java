package com.careerplanning.backend.modules.career.controller;

import com.careerplanning.backend.common.response.ApiResponse;
import com.careerplanning.backend.modules.career.dto.CareerTrackConfirmationResponse;
import com.careerplanning.backend.modules.career.dto.ConfirmCareerTrackRequest;
import com.careerplanning.backend.modules.career.dto.GenerateRoadmapResponse;
import com.careerplanning.backend.modules.career.dto.SubmitTechnicalAssessmentRequest;
import com.careerplanning.backend.modules.career.dto.TechnicalAssessmentProgressResponse;
import com.careerplanning.backend.modules.career.dto.TechnicalAssessmentQuestionResponse;
import com.careerplanning.backend.modules.career.dto.TechnicalAssessmentResultResponse;
import com.careerplanning.backend.modules.career.service.CareerService;
import com.careerplanning.backend.modules.career.service.TechnicalAssessmentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/career")
public class CareerController {

    private final CareerService careerService;
    private final TechnicalAssessmentService technicalAssessmentService;

    public CareerController(CareerService careerService, TechnicalAssessmentService technicalAssessmentService) {
        this.careerService = careerService;
        this.technicalAssessmentService = technicalAssessmentService;
    }

    @GetMapping("/technical-assessment/tracks")
    public ApiResponse<List<String>> listTechnicalAssessmentTracks() {
        return ApiResponse.success(technicalAssessmentService.listSupportedTracks());
    }

    @GetMapping("/technical-assessment/questions")
    public ApiResponse<List<TechnicalAssessmentQuestionResponse>> listTechnicalAssessmentQuestions(
            @RequestParam String careerTrack
    ) {
        return ApiResponse.success(technicalAssessmentService.getQuestions(careerTrack));
    }

    @PostMapping("/technical-assessment/submit")
    public ApiResponse<TechnicalAssessmentResultResponse> submitTechnicalAssessment(
            @RequestHeader("X-Auth-Token") String token,
            @Valid @RequestBody SubmitTechnicalAssessmentRequest request
    ) {
        return ApiResponse.success(technicalAssessmentService.submitAssessment(token, request));
    }

    @GetMapping("/technical-assessment/progress")
    public ApiResponse<TechnicalAssessmentProgressResponse> getTechnicalAssessmentProgress(
            @RequestHeader("X-Auth-Token") String token
    ) {
        return ApiResponse.success(technicalAssessmentService.getProgress(token));
    }

    @PostMapping("/confirm-track")
    public ApiResponse<CareerTrackConfirmationResponse> confirmCareerTrack(
            @RequestHeader("X-Auth-Token") String token,
            @Valid @RequestBody ConfirmCareerTrackRequest request
    ) {
        return ApiResponse.success(careerService.confirmCareerTrack(token, request));
    }

    @PostMapping("/generate-roadmap")
    public ApiResponse<GenerateRoadmapResponse> generateRoadmap(
            @RequestHeader("X-Auth-Token") String token
    ) {
        return ApiResponse.success(careerService.generateRoadmap(token));
    }
}
