package com.careerplanning.backend.modules.goals.service;

import com.careerplanning.backend.modules.goals.entity.Goal;
import com.careerplanning.backend.modules.goals.entity.GoalCategory;
import com.careerplanning.backend.modules.goals.entity.GoalTask;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class GoalTaskPlannerService {

    private static final int[] DEFAULT_WEIGHTS = {12, 18, 22, 22, 16, 10};
    private static final double[] MILESTONE_RATIOS = {0.12, 0.28, 0.45, 0.62, 0.82, 1.00};

    public List<GoalTask> buildInitialTasks(Goal goal) {
        LocalDate start = LocalDate.now();
        LocalDate end = normalizedEndDate(goal, start);
        PlanContext context = buildContext(goal, start, end);
        List<TaskBlueprint> blueprint = taskBlueprint(context);

        List<GoalTask> tasks = new ArrayList<>();
        for (int i = 0; i < blueprint.size(); i++) {
            TaskBlueprint item = blueprint.get(i);
            GoalTask task = new GoalTask();
            task.setGoalId(goal.getId());
            task.setTitle(item.title());
            task.setDetails(item.details());
            task.setWeight(item.weight());
            task.setSortOrder(i + 1);
            task.setDueDate(milestoneDate(start, end, milestoneRatio(i, blueprint.size())));
            task.setCompleted(false);
            tasks.add(task);
        }

        return tasks;
    }

    public void rescheduleTasks(Goal goal, List<GoalTask> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return;
        }

        LocalDate start = LocalDate.now();
        LocalDate end = normalizedEndDate(goal, start);
        List<GoalTask> sorted = tasks.stream()
                .sorted((left, right) -> Integer.compare(
                        left.getSortOrder() == null ? 999 : left.getSortOrder(),
                        right.getSortOrder() == null ? 999 : right.getSortOrder()
                ))
                .toList();

        for (int i = 0; i < sorted.size(); i++) {
            GoalTask task = sorted.get(i);
            double ratio = milestoneRatio(i, sorted.size());
            task.setDueDate(milestoneDate(start, end, ratio));
        }
    }

    private PlanContext buildContext(Goal goal, LocalDate start, LocalDate end) {
        GoalCategory category = goal.getCategory() == null ? GoalCategory.LEARNING : goal.getCategory();
        String title = goal.getTitle() == null ? "this goal" : goal.getTitle().trim();
        int weeks = Math.max(4, (int) Math.ceil(Math.max(1, ChronoUnit.DAYS.between(start, end)) / 7.0));
        int weeklyHours = switch (category) {
            case PROJECT -> 8;
            case CERTIFICATION -> 7;
            case SKILL_DEVELOPMENT -> 6;
            case CAREER_GROWTH -> 5;
            case NETWORKING -> 4;
            case LEARNING -> 5;
        };
        int totalHours = weeks * weeklyHours;
        int focusedSessions = Math.max(12, weeks * 3);
        return new PlanContext(category, title, weeks, weeklyHours, totalHours, focusedSessions);
    }

    private LocalDate normalizedEndDate(Goal goal, LocalDate start) {
        LocalDate target = goal.getTargetDate() == null ? start.plusWeeks(8) : goal.getTargetDate();
        LocalDate minimumEnd = start.plusDays(21);
        if (target.isBefore(minimumEnd)) {
            return minimumEnd;
        }
        return target;
    }

    private double milestoneRatio(int index, int size) {
        if (size <= 1) {
            return 1.0;
        }
        if (size == MILESTONE_RATIOS.length) {
            return MILESTONE_RATIOS[index];
        }
        return Math.min(1.0, (index + 1.0) / size);
    }

    private LocalDate milestoneDate(LocalDate start, LocalDate end, double ratio) {
        long days = Math.max(1, ChronoUnit.DAYS.between(start, end));
        long offset = Math.max(1, Math.round(days * ratio));
        LocalDate value = start.plusDays(offset);
        if (value.isAfter(end)) {
            return end;
        }
        return value;
    }

    private List<TaskBlueprint> taskBlueprint(PlanContext context) {
        Map<GoalCategory, List<TaskBlueprint>> templates = new LinkedHashMap<>();
        templates.put(GoalCategory.PROJECT, List.of(
                new TaskBlueprint("Initiate scope and acceptance criteria", "Define measurable outcomes and done-definition for " + context.title() + ".", DEFAULT_WEIGHTS[0]),
                new TaskBlueprint("Plan architecture and delivery cadence", "Create implementation plan with weekly checkpoints (" + context.weeks() + " weeks, " + context.weeklyHours() + " hrs/week).", DEFAULT_WEIGHTS[1]),
                new TaskBlueprint("Execute build iteration 1", "Complete first implementation cycle with evidence (commits, artifacts, or reviewed deliverable).", DEFAULT_WEIGHTS[2]),
                new TaskBlueprint("Execute build iteration 2 and integration", "Finish remaining core scope and integrate components end-to-end before quality gate.", DEFAULT_WEIGHTS[3]),
                new TaskBlueprint("Run validation and defect closure", "Execute tests/reviews, close critical defects, and document verification outcomes.", DEFAULT_WEIGHTS[4]),
                new TaskBlueprint("Release output and retrospective", "Publish demo/deployment and write a retrospective with concrete next improvements.", DEFAULT_WEIGHTS[5])
        ));
        templates.put(GoalCategory.SKILL_DEVELOPMENT, List.of(
                new TaskBlueprint("Run competency baseline assessment", "Benchmark current skill level and define a measurable target for " + context.title() + ".", DEFAULT_WEIGHTS[0]),
                new TaskBlueprint("Complete spaced learning block", "Finish at least " + context.focusedSessions() + " focused sessions across " + context.weeks() + " weeks (avoid cramming).", DEFAULT_WEIGHTS[1]),
                new TaskBlueprint("Practice retrieval and drills", "Run active recall quizzes/drills and capture weak areas from each session.", DEFAULT_WEIGHTS[2]),
                new TaskBlueprint("Build applied skill artifact", "Create one practical output where the skill is used in a realistic context.", DEFAULT_WEIGHTS[3]),
                new TaskBlueprint("Collect expert feedback and remediate", "Get mentor/peer review and complete targeted corrections based on feedback.", DEFAULT_WEIGHTS[4]),
                new TaskBlueprint("Demonstrate mastery evidence", "Publish final evidence (demo/report/portfolio) plus next-level improvement plan.", DEFAULT_WEIGHTS[5])
        ));
        templates.put(GoalCategory.CERTIFICATION, List.of(
                new TaskBlueprint("Create exam blueprint and schedule", "Break official syllabus into weekly checkpoints (" + context.weeks() + " weeks, " + context.totalHours() + " planned hours).", DEFAULT_WEIGHTS[0]),
                new TaskBlueprint("Complete core syllabus coverage", "Finish all major domains with concise notes and concept checks.", DEFAULT_WEIGHTS[1]),
                new TaskBlueprint("Attempt mock exam round 1", "Take one full timed mock and record section-wise score breakdown.", DEFAULT_WEIGHTS[2]),
                new TaskBlueprint("Remediate weak domains", "Run targeted drills for low-scoring topics and validate with mini-tests.", DEFAULT_WEIGHTS[3]),
                new TaskBlueprint("Attempt mock exam round 2", "Take second timed mock and reach your target threshold before final attempt.", DEFAULT_WEIGHTS[4]),
                new TaskBlueprint("Execute final exam readiness", "Attempt/Book final exam and document post-exam strengths and gaps.", DEFAULT_WEIGHTS[5])
        ));
        templates.put(GoalCategory.CAREER_GROWTH, List.of(
                new TaskBlueprint("Define role-gap matrix", "List target role requirements and map current gaps with measurable goals.", DEFAULT_WEIGHTS[0]),
                new TaskBlueprint("Build portfolio and profile evidence", "Prepare resume/portfolio artifacts showing quantified impact.", DEFAULT_WEIGHTS[1]),
                new TaskBlueprint("Run targeted outreach cycle", "Complete structured outreach to hiring managers/mentors with personalized context.", DEFAULT_WEIGHTS[2]),
                new TaskBlueprint("Complete interview readiness rounds", "Run mock interviews (technical + behavioral) and close repeated weak points.", DEFAULT_WEIGHTS[3]),
                new TaskBlueprint("Execute application sprint", "Submit high-quality applications with tracking sheet and follow-up plan.", DEFAULT_WEIGHTS[4]),
                new TaskBlueprint("Review conversion metrics and iterate", "Analyze response/interview rates and refine strategy for next cycle.", DEFAULT_WEIGHTS[5])
        ));
        templates.put(GoalCategory.NETWORKING, List.of(
                new TaskBlueprint("Map strategic network targets", "Identify priority communities and 15-20 relevant contacts for " + context.title() + ".", DEFAULT_WEIGHTS[0]),
                new TaskBlueprint("Prepare positioning and outreach assets", "Create concise intro, value proposition, and conversation intent.", DEFAULT_WEIGHTS[1]),
                new TaskBlueprint("Execute outreach wave 1", "Send personalized messages and track response quality, not just count.", DEFAULT_WEIGHTS[2]),
                new TaskBlueprint("Run informational conversations", "Conduct structured discussions and document actionable takeaways.", DEFAULT_WEIGHTS[3]),
                new TaskBlueprint("Contribute and follow through", "Provide value back (resources/referrals/help) and complete agreed follow-ups.", DEFAULT_WEIGHTS[4]),
                new TaskBlueprint("Systematize long-term relationship plan", "Maintain cadence tracker with next touchpoints and outcomes.", DEFAULT_WEIGHTS[5])
        ));
        templates.put(GoalCategory.LEARNING, List.of(
                new TaskBlueprint("Set measurable learning objective", "Define mastery criteria and evidence plan for " + context.title() + ".", DEFAULT_WEIGHTS[0]),
                new TaskBlueprint("Complete foundational study cycle", "Cover core content with at least " + context.focusedSessions() + " spaced sessions.", DEFAULT_WEIGHTS[1]),
                new TaskBlueprint("Run practice and retrieval checks", "Use quizzes/exercises to validate retention and concept transfer.", DEFAULT_WEIGHTS[2]),
                new TaskBlueprint("Build practical application output", "Apply learning to a mini-project or case study with real constraints.", DEFAULT_WEIGHTS[3]),
                new TaskBlueprint("Perform review and gap correction", "Get feedback and fix recurring errors with targeted revision.", DEFAULT_WEIGHTS[4]),
                new TaskBlueprint("Submit final demonstration", "Present final result with reflection on strengths, gaps, and next steps.", DEFAULT_WEIGHTS[5])
        ));

        return templates.getOrDefault(context.category(), templates.get(GoalCategory.LEARNING));
    }

    private record TaskBlueprint(String title, String details, Integer weight) {
    }

    private record PlanContext(
            GoalCategory category,
            String title,
            int weeks,
            int weeklyHours,
            int totalHours,
            int focusedSessions
    ) {
    }
}
