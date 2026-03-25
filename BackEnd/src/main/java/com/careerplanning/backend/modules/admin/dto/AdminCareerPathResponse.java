package com.careerplanning.backend.modules.admin.dto;

import java.util.List;

public record AdminCareerPathResponse(
        String careerTrack,
        List<AdminCareerPhaseResponse> phases,
        List<AdminCareerGoalTemplateResponse> templates
) {
}
