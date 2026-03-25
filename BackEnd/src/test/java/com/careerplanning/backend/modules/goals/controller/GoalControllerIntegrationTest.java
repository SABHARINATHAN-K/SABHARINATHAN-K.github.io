package com.careerplanning.backend.modules.goals.controller;

import com.careerplanning.backend.modules.goals.dto.GoalStatsResponse;
import com.careerplanning.backend.modules.goals.entity.GoalCategory;
import com.careerplanning.backend.modules.goals.entity.GoalPriority;
import com.careerplanning.backend.modules.goals.service.GoalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GoalController.class)
class GoalControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GoalService goalService;

    @Test
    void goalStatsEndpointIncludesExtendedCareerPlanningFields() throws Exception {
        GoalStatsResponse statsResponse = new GoalStatsResponse(
                6,
                2,
                2,
                2,
                List.of(new GoalStatsResponse.CategoryCount(GoalCategory.PROJECT, 3)),
                List.of(new GoalStatsResponse.PriorityCount(GoalPriority.HIGH, 2)),
                33,
                50,
                List.of("Add 1 more project goal(s) to meet software engineering baseline."),
                1,
                4,
                17,
                3,
                1,
                List.of(new GoalStatsResponse.DueTaskItem(100L, "Build API", 200L, "Implement auth", java.time.LocalDate.now().plusDays(2), false)),
                List.of(new GoalStatsResponse.TimelineItem("Build API", Instant.now().minusSeconds(3600), null)),
                new GoalStatsResponse.NextBestAction(100L, "Build full stack app", "Execution")
        );

        when(goalService.getGoalStats("stats-token")).thenReturn(statsResponse);

        mockMvc.perform(get("/api/v1/goals/stats")
                        .header("X-Auth-Token", "stats-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.alignmentScore").value(50))
                .andExpect(jsonPath("$.data.gapWarnings").isArray())
                .andExpect(jsonPath("$.data.goalsCompletedThisWeek").value(1))
                .andExpect(jsonPath("$.data.goalsUpdatedThisWeek").value(4))
                .andExpect(jsonPath("$.data.weeklyExecutionScore").value(17))
                .andExpect(jsonPath("$.data.dueSoonTaskCount").value(3))
                .andExpect(jsonPath("$.data.overdueTaskCount").value(1))
                .andExpect(jsonPath("$.data.dueSoonTasks").isArray())
                .andExpect(jsonPath("$.data.timeline").isArray())
                .andExpect(jsonPath("$.data.timeline[0].goalTitle").value("Build API"))
                .andExpect(jsonPath("$.data.nextBestAction.goalId").value(100))
                .andExpect(jsonPath("$.data.nextBestAction.phase").value("Execution"));
    }
}
