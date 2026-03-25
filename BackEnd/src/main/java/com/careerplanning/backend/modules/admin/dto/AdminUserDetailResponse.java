package com.careerplanning.backend.modules.admin.dto;

import com.careerplanning.backend.modules.career.dto.TechnicalAssessmentProgressResponse;
import com.careerplanning.backend.modules.goals.dto.GoalResponse;
import com.careerplanning.backend.modules.users.dto.UserProfileResponse;

import java.util.List;

public record AdminUserDetailResponse(
        UserProfileResponse profile,
        List<GoalResponse> goals,
        TechnicalAssessmentProgressResponse technicalReadiness
) {
}
