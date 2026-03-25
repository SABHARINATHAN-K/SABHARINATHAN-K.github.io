const logoutBtn = document.getElementById("logoutBtn");
const welcomeText = document.getElementById("welcomeText");
const careerPill = document.getElementById("careerPill");
const templateList = document.getElementById("templateList");
const recentGoalsList = document.getElementById("recentGoalsList");
const profileName = document.getElementById("profileName");
const profileEmail = document.getElementById("profileEmail");
const profileRole = document.getElementById("profileRole");
const profileCareerTrack = document.getElementById("profileCareerTrack");
const profileStatus = document.getElementById("profileStatus");
const completionRing = document.getElementById("completionRing");
const completionRateValue = document.getElementById("completionRateValue");
const navUserInitial = document.getElementById("navUserInitial");
const navUserName = document.getElementById("navUserName");
const navUserEmail = document.getElementById("navUserEmail");
const generateRoadmapBtn = document.getElementById("generateRoadmapBtn");
const gapWarningsList = document.getElementById("gapWarningsList");
const nextBestActionPanel = document.getElementById("nextBestActionPanel");
const timelineList = document.getElementById("timelineList");
const workflowAlignmentText = document.getElementById("workflowAlignmentText");
const workflowGapText = document.getElementById("workflowGapText");
const focusTitle = document.getElementById("focusTitle");
const focusDescription = document.getElementById("focusDescription");
const focusActionBtn = document.getElementById("focusActionBtn");
const dueSoonBadge = document.getElementById("dueSoonBadge");
const dueSoonTasksList = document.getElementById("dueSoonTasksList");
const timelineFilterAll = document.getElementById("timelineFilterAll");
const timelineFilterPending = document.getElementById("timelineFilterPending");
const timelineFilterCompleted = document.getElementById("timelineFilterCompleted");
const technicalDueChip = document.getElementById("technicalDueChip");
const technicalTrackText = document.getElementById("technicalTrackText");
const technicalLevelText = document.getElementById("technicalLevelText");
const technicalScoreText = document.getElementById("technicalScoreText");
const technicalImprovementText = document.getElementById("technicalImprovementText");
const technicalSummaryText = document.getElementById("technicalSummaryText");
const technicalLastAssessedText = document.getElementById("technicalLastAssessedText");
const technicalReassessmentText = document.getElementById("technicalReassessmentText");
const technicalAssessmentBtn = document.getElementById("technicalAssessmentBtn");
const technicalHistoryCount = document.getElementById("technicalHistoryCount");
const technicalSkillList = document.getElementById("technicalSkillList");
const technicalHistoryList = document.getElementById("technicalHistoryList");
const discoveryNudgeText = document.getElementById("discoveryNudgeText");
const discoveryNudgeBtn = document.getElementById("discoveryNudgeBtn");

let timelineEntries = [];
let timelineFilter = "ALL";

if (AppApi.requireAuth()) {
  initDashboard();
}

logoutBtn.addEventListener("click", () => {
  AppApi.clearToken();
  window.location.href = "./login.html";
});

generateRoadmapBtn.addEventListener("click", generateRoadmap);
timelineFilterAll?.addEventListener("click", () => setTimelineFilter("ALL"));
timelineFilterPending?.addEventListener("click", () => setTimelineFilter("PENDING"));
timelineFilterCompleted?.addEventListener("click", () => setTimelineFilter("COMPLETED"));

async function initDashboard() {
  try {
    const [profile, stats, goals, templateMap, technicalProgress] = await Promise.all([
      AppApi.getMe(),
      AppApi.getGoalStats(),
      AppApi.listGoals(),
      AppApi.listGoalTemplates(),
      AppApi.getTechnicalAssessmentProgress().catch(() => null)
    ]);

    const firstName = (profile.fullName || "User").split(" ")[0];

    if (profile.onboardingCompleted === false) {
      window.location.href = "./technical-readiness.html";
      return;
    }

    welcomeText.textContent = `Welcome back, ${firstName}`;
    profileName.textContent = profile.fullName || "-";
    profileEmail.textContent = profile.email || "-";
    profileRole.textContent = AppUi.humanize(profile.role || "-");
    profileCareerTrack.textContent = AppUi.humanize(profile.careerTrack || "-");
    profileStatus.textContent = "Profile loaded successfully.";

    navUserInitial.textContent = (profile.fullName || "U").charAt(0).toUpperCase();
    navUserName.textContent = profile.fullName || "User";
    navUserEmail.textContent = profile.email || "-";

    careerPill.textContent = AppUi.humanize(profile.careerTrack || "CAREER");

    setStat("statTotal", stats.total || 0);
    setStat("statPlanned", stats.planned || 0);
    setStat("statInProgress", stats.inProgress || 0);
    setStat("statCompleted", stats.completed || 0);
    setStat("statAlignment", `${Number(stats.alignmentScore || 0)}%`);
    setStat("statWeekly", `${Number(stats.weeklyExecutionScore || 0)}%`);
    setStat("statDueSoon", Number(stats.dueSoonTaskCount || 0));
    setStat("statOverdue", Number(stats.overdueTaskCount || 0));

    const completionRate = Number(stats.completionRate || 0);
    completionRing.style.setProperty("--progress", String(completionRate));
    completionRateValue.textContent = `${completionRate}%`;

    renderRecentGoals(goals || []);
    renderGapWarnings(stats.gapWarnings || []);
    renderNextBestAction(stats.nextBestAction || null);
    renderWorkflowSummary(stats);
    timelineEntries = stats.timeline || [];
    applyTimelineFilterButtonState();
    renderTimeline();
    renderDueSoonTasks(stats.dueSoonTasks || [], Number(stats.dueSoonTaskCount || 0));
    renderTemplateActions(profile.careerTrack, templateMap || {});
    renderTechnicalProgress(profile, technicalProgress);
    AppUi.applyReveal(document);
  } catch (error) {
    profileStatus.textContent = `Could not load dashboard: ${error.message}`;
    AppUi.showToast(error.message, "error");
  }
}

function renderWorkflowSummary(stats) {
  if (workflowAlignmentText) {
    workflowAlignmentText.textContent = `Alignment: ${Number(stats.alignmentScore || 0)}%`;
  }

  const warningCount = Array.isArray(stats.gapWarnings) ? stats.gapWarnings.length : 0;
  if (workflowGapText) {
    workflowGapText.textContent = warningCount
      ? `${warningCount} category gap warning(s) need attention.`
      : "No category gaps detected. Your plan is well balanced.";
  }

  if (!focusTitle || !focusDescription || !focusActionBtn) {
    return;
  }

  if (stats.nextBestAction) {
    focusTitle.textContent = AppUi.humanize(stats.nextBestAction.title || "Next best action");
    focusDescription.textContent = `Phase: ${AppUi.humanize(stats.nextBestAction.phase || "blueprint")}. Complete this first to keep your roadmap on track.`;
    focusActionBtn.textContent = "Start Next Action";
    focusActionBtn.href = `./goal-detail.html?id=${stats.nextBestAction.goalId}`;
    return;
  }

  focusTitle.textContent = "All blueprint steps are completed";
  focusDescription.textContent = "Add advanced manual goals and keep momentum with your next execution cycle.";
  focusActionBtn.textContent = "Open Goals Workspace";
  focusActionBtn.href = "./goals.html";
}

function renderTechnicalProgress(profile, progress) {
  const benchmarkTrack = (progress && progress.careerTrack) || (profile && profile.careerTrack) || "";
  const history = progress && Array.isArray(progress.history) ? progress.history : [];
  const skillAreas = progress && Array.isArray(progress.latestSkillAreas) ? progress.latestSkillAreas : [];
  const hasAssessment = Boolean(
    progress &&
    progress.currentLevel &&
    progress.currentPercentageScore !== null &&
    progress.currentPercentageScore !== undefined
  );

  if (technicalTrackText) {
    technicalTrackText.textContent = AppUi.humanize(benchmarkTrack || "-");
  }
  if (technicalHistoryCount) {
    technicalHistoryCount.textContent = `${history.length} checkpoint${history.length === 1 ? "" : "s"}`;
  }

  if (!progress) {
    setTechnicalStatusChip("Unavailable", "status-planned");
    setTechnicalSummary(
      "Technical assessment data is temporarily unavailable.",
      "The dashboard loaded without readiness history because that request failed."
    );
    setTechnicalMeta("Unknown", "Try again later");
    setTechnicalScoreState("Unavailable", "-");
    setTechnicalCta("Open Readiness Check");
    renderTechnicalSkillAreas([], false);
    renderTechnicalHistory([], false);
    setDiscoveryNudgeState("Open the technical readiness check to evaluate your current domain level once the service is available.", "Open Readiness Check");
    return;
  }

  renderTechnicalSkillAreas(skillAreas, hasAssessment);
  renderTechnicalHistory(history, hasAssessment);
  setTechnicalSummary(progress.summary || "No readiness insight available.", formatTechnicalImprovement(progress.improvementPercentagePoints));

  if (!hasAssessment) {
    setTechnicalStatusChip("No Baseline", "status-planned");
    setTechnicalMeta("Not yet", "Baseline pending");
    setTechnicalScoreState("Baseline Pending", "-");
    setTechnicalCta("Take Readiness Check");
    setDiscoveryNudgeState(
      "Take your first technical readiness check to establish a baseline, then revisit it every 30 days to compare improvement.",
      "Take Readiness Check"
    );
    return;
  }

  setTechnicalStatusChip(progress.reassessmentDue ? "Reassessment Due" : "Tracking", progress.reassessmentDue ? "priority-high" : "status-completed");
  setTechnicalMeta(
    AppUi.formatDate(progress.lastAssessedAt),
    progress.reassessmentDue
      ? `Due now (since ${AppUi.formatDate(progress.recommendedReassessmentAt)})`
      : `Recommended by ${AppUi.formatDate(progress.recommendedReassessmentAt)}`
  );
  setTechnicalScoreState(AppUi.humanize(progress.currentLevel || "-"), `${Number(progress.currentPercentageScore || 0)}%`);
  setTechnicalCta(progress.reassessmentDue ? "Reassess Now" : "Retake Readiness Check");
  setDiscoveryNudgeState(
    progress.reassessmentDue
      ? "Your next reassessment window is open. Retake the readiness check now and compare your score against your last checkpoint."
      : `Your latest readiness check is current. Reassess by ${AppUi.formatDate(progress.recommendedReassessmentAt)} to measure improvement over time.`,
    progress.reassessmentDue ? "Reassess Readiness Check" : "Retake Readiness Check"
  );

  if (progress.reassessmentDue) {
    profileStatus.textContent = `Latest readiness check for ${AppUi.humanize(progress.careerTrack || profile.careerTrack || "your track")} is due for reassessment.`;
  }
}

function setTechnicalSummary(summaryText, improvementText) {
  if (technicalSummaryText) {
    technicalSummaryText.textContent = summaryText;
  }
  if (technicalImprovementText) {
    technicalImprovementText.textContent = improvementText;
  }
}

function setTechnicalMeta(lastAssessed, reassessmentText) {
  if (technicalLastAssessedText) {
    technicalLastAssessedText.textContent = lastAssessed;
  }
  if (technicalReassessmentText) {
    technicalReassessmentText.textContent = reassessmentText;
  }
}

function setTechnicalScoreState(levelText, scoreText) {
  if (technicalLevelText) {
    technicalLevelText.textContent = levelText;
  }
  if (technicalScoreText) {
    technicalScoreText.textContent = scoreText;
  }
}

function setTechnicalStatusChip(label, statusClass) {
  if (!technicalDueChip) {
    return;
  }
  technicalDueChip.textContent = label;
  technicalDueChip.className = `chip ${statusClass}`.trim();
}

function setTechnicalCta(label) {
  if (technicalAssessmentBtn) {
    technicalAssessmentBtn.textContent = label;
  }
}

function setDiscoveryNudgeState(message, actionLabel) {
  if (discoveryNudgeText) {
    discoveryNudgeText.textContent = message;
  }
  if (discoveryNudgeBtn) {
    discoveryNudgeBtn.textContent = actionLabel;
  }
}

function renderTechnicalSkillAreas(skillAreas, hasAssessment) {
  if (!technicalSkillList) {
    return;
  }

  if (!Array.isArray(skillAreas) || !skillAreas.length) {
    technicalSkillList.innerHTML = `<p class='text-muted'>${hasAssessment
      ? "No skill breakdown was stored for the latest readiness check."
      : "Skill-area analysis will appear after your first technical readiness check."}</p>`;
    return;
  }

  technicalSkillList.innerHTML = skillAreas
    .map((skill) => {
      const percentage = clampPercentage(skill.percentageScore);
      return `
        <div class="bar-row">
          <div class="bar-head">
            <span>${AppUi.escapeHtml(skill.skillArea || "Skill")}</span>
            <strong>${percentage}%</strong>
          </div>
          <div class="bar-track"><div class="bar-fill" style="${buildBarFillStyle(percentage)}"></div></div>
          <p class="text-muted">${Number(skill.score || 0)}/${Number(skill.maxScore || 0)} points</p>
        </div>`;
    })
    .join("");
  AppUi.applyReveal(technicalSkillList);
}

function renderTechnicalHistory(history, hasAssessment) {
  if (!technicalHistoryList) {
    return;
  }

  if (!Array.isArray(history) || !history.length) {
    technicalHistoryList.innerHTML = `<p class='text-muted'>${hasAssessment
      ? "Complete another readiness check after focused practice to start comparing checkpoint trends."
      : "Readiness history will appear here after your first completed attempt."}</p>`;
    return;
  }

  technicalHistoryList.innerHTML = history
    .map((item, index) => {
      const percentage = clampPercentage(item.percentageScore);
      return `
        <div class="bar-row">
          <div class="bar-head">
            <span>Checkpoint ${index + 1} · ${AppUi.escapeHtml(AppUi.formatDate(item.assessedAt))}</span>
            <strong>${percentage}%</strong>
          </div>
          <div class="bar-track"><div class="bar-fill" style="${buildBarFillStyle(percentage)}"></div></div>
          <p class="text-muted">${AppUi.escapeHtml(AppUi.humanize(item.proficiencyLevel || "UNKNOWN"))}</p>
        </div>`;
    })
    .join("");
  AppUi.applyReveal(technicalHistoryList);
}

function buildBarFillStyle(percentage) {
  const clamped = clampPercentage(percentage);
  const width = clamped <= 0 ? 0 : Math.max(8, clamped);
  return `width:${width}%; background:${resolveBarGradient(clamped)};`;
}

function resolveBarGradient(percentage) {
  if (percentage >= 80) {
    return "linear-gradient(135deg, #15803d, #22c55e)";
  }
  if (percentage >= 60) {
    return "linear-gradient(135deg, #0369a1, #38bdf8)";
  }
  if (percentage >= 45) {
    return "linear-gradient(135deg, #d97706, #fbbf24)";
  }
  return "linear-gradient(135deg, #ea580c, #fb7185)";
}

function clampPercentage(value) {
  const percentage = Number(value || 0);
  if (percentage < 0) {
    return 0;
  }
  if (percentage > 100) {
    return 100;
  }
  return Math.round(percentage);
}

function formatTechnicalImprovement(improvement) {
  if (improvement === null || improvement === undefined) {
    return "This is your baseline assessment for the selected role.";
  }
  if (improvement > 0) {
    return `Improved by ${improvement} percentage points versus your previous checkpoint.`;
  }
  if (improvement < 0) {
    return `Dropped by ${Math.abs(improvement)} percentage points versus your previous checkpoint.`;
  }
  return "Your score is unchanged from the previous checkpoint.";
}

function setStat(id, value) {
  const element = document.getElementById(id);
  if (element) {
    element.textContent = String(value);
  }
}

function renderRecentGoals(goals) {
  const sorted = [...goals]
    .sort((a, b) => new Date(b.createdAt || 0).getTime() - new Date(a.createdAt || 0).getTime())
    .slice(0, 3);

  if (!sorted.length) {
    recentGoalsList.innerHTML = `
      <div class="empty-state">
        <h4>No goals yet</h4>
        <p>Create your first goal to start tracking progress.</p>
        <a class="btn btn-primary" href="./goals.html" style="margin-top:0.7rem;">Create Goal</a>
      </div>`;
    return;
  }

  recentGoalsList.innerHTML = sorted
    .map((goal) => {
      return `
        <a class="goal-item" href="./goal-detail.html?id=${goal.id}">
          <div class="goal-title-row">
            <h4>${AppUi.escapeHtml(goal.title || "Untitled Goal")}</h4>
            <span class="status-pill ${statusClass(goal.status)}">${AppUi.escapeHtml(AppUi.humanize(goal.status || "PLANNED"))}</span>
          </div>
          <p class="text-muted">${AppUi.escapeHtml(goal.description || "No description")}</p>

          <div class="goal-meta">
            <span>${goal.targetDate ? AppUi.formatDate(goal.targetDate) : "No deadline"}</span>
            <span class="priority-pill ${priorityClass(goal.priority)}">${AppUi.escapeHtml(AppUi.humanize(goal.priority || "MEDIUM"))}</span>
          </div>

          <div class="progress-line">
            <div class="progress-track"><div class="progress-value" style="width:${Number(goal.progress || 0)}%"></div></div>
            <span>${Number(goal.progress || 0)}%</span>
          </div>
        </a>`;
    })
    .join("");
  AppUi.applyReveal(recentGoalsList);
}

function renderTemplateActions(careerTrack, templateMap) {
  const templates = templateMap[careerTrack] || [];

  if (!templates.length) {
    templateList.innerHTML = "<p class='text-muted'>No templates available for this track.</p>";
    return;
  }

  templateList.innerHTML = templates
    .slice(0, 6)
    .map((template) => {
      return `
        <div class="template-item">
          <p>${AppUi.escapeHtml(template)}</p>
          <button class="btn btn-ghost btn-small" data-template="${AppUi.escapeAttr(template)}" type="button">Use</button>
        </div>`;
    })
    .join("");

  templateList.querySelectorAll("button[data-template]").forEach((button) => {
    button.addEventListener("click", () => {
      const template = button.getAttribute("data-template") || "";
      const url = new URL("./goals.html", window.location.href);
      url.searchParams.set("title", template);
      url.searchParams.set("description", `Planned from template: ${template}`);
      window.location.href = url.toString();
    });
  });
  AppUi.applyReveal(templateList);
}

function renderGapWarnings(warnings) {
  if (!warnings.length) {
    gapWarningsList.innerHTML = "<p class='text-muted'>No gaps detected. Keep your momentum.</p>";
    return;
  }

  gapWarningsList.innerHTML = warnings
    .map((warning) => `<div class=\"warning-item\">${AppUi.escapeHtml(warning)}</div>`)
    .join("");
  AppUi.applyReveal(gapWarningsList);
}

function renderNextBestAction(nextBestAction) {
  if (!nextBestAction) {
    nextBestActionPanel.innerHTML = `
      <div class="empty-state" style="padding: 1rem;">
        <h4>All blueprint goals are completed</h4>
        <p>No pending guided action left in your current blueprint.</p>
      </div>`;
    return;
  }

  nextBestActionPanel.innerHTML = `
    <a class="goal-item" href="./goal-detail.html?id=${nextBestAction.goalId}">
      <div class="goal-title-row">
        <h4>${AppUi.escapeHtml(nextBestAction.title || "Next Action")}</h4>
        <span class="chip status-in-progress">${AppUi.escapeHtml(nextBestAction.phase || "Blueprint")}</span>
      </div>
      <p class="text-muted">Follow this step to stay aligned with your generated career blueprint.</p>
    </a>`;
  AppUi.applyReveal(nextBestActionPanel);
}

function renderDueSoonTasks(tasks, totalCount) {
  if (dueSoonBadge) {
    dueSoonBadge.textContent = String(totalCount || 0);
    dueSoonBadge.className = `chip ${tasks.some((task) => task && task.overdue) ? "priority-urgent" : ""}`.trim();
  }

  if (!dueSoonTasksList) {
    return;
  }

  if (!Array.isArray(tasks) || !tasks.length) {
    dueSoonTasksList.innerHTML = "<p class='text-muted'>No pending tasks due in the next 7 days.</p>";
    return;
  }

  dueSoonTasksList.innerHTML = tasks
    .map((task) => {
      const dueDate = task.dueDate ? AppUi.formatDate(task.dueDate) : "No due date";
      const badgeClass = task.overdue ? "priority-urgent" : "status-in-progress";
      const badgeLabel = task.overdue ? "Overdue" : "Due Soon";
      return `
        <a class="goal-item" href="./goal-detail.html?id=${task.goalId}">
          <div class="goal-title-row">
            <h4>${AppUi.escapeHtml(task.taskTitle || "Task")}</h4>
            <span class="chip ${badgeClass}">${AppUi.escapeHtml(badgeLabel)}</span>
          </div>
          <p class="text-muted">${AppUi.escapeHtml(task.goalTitle || "Goal")}</p>
          <div class="goal-meta">
            <span>Due: ${AppUi.escapeHtml(dueDate)}</span>
          </div>
        </a>`;
    })
    .join("");
  AppUi.applyReveal(dueSoonTasksList);
}

function renderTimeline() {
  const filtered = timelineEntries.filter((item) => {
    if (timelineFilter === "COMPLETED") {
      return Boolean(item.completedDate);
    }
    if (timelineFilter === "PENDING") {
      return !item.completedDate;
    }
    return true;
  });

  const entries = [...filtered]
    .sort((a, b) => new Date(a.createdDate || 0).getTime() - new Date(b.createdDate || 0).getTime())
    .slice(0, 12);

  if (!entries.length) {
    const emptyMessage = timelineEntries.length
      ? "No timeline entries for the selected filter."
      : "No timeline data yet.";
    timelineList.innerHTML = `<p class='text-muted'>${emptyMessage}</p>`;
    return;
  }

  timelineList.innerHTML = entries
    .map((item) => {
      const created = item.createdDate ? AppUi.formatDate(item.createdDate) : "Unknown";
      const completed = item.completedDate ? AppUi.formatDate(item.completedDate) : "Pending";

      return `
        <div class="timeline-item">
          <div>
            <h4>${AppUi.escapeHtml(item.goalTitle || "Untitled Goal")}</h4>
            <p class="text-muted">Created: ${AppUi.escapeHtml(created)}</p>
          </div>
          <span class="chip ${item.completedDate ? "status-completed" : "status-in-progress"}">${AppUi.escapeHtml(completed)}</span>
        </div>`;
    })
    .join("");
  AppUi.applyReveal(timelineList);
}

function setTimelineFilter(filter) {
  timelineFilter = filter;
  applyTimelineFilterButtonState();
  renderTimeline();
}

function applyTimelineFilterButtonState() {
  const allButtons = [
    [timelineFilterAll, "ALL"],
    [timelineFilterPending, "PENDING"],
    [timelineFilterCompleted, "COMPLETED"]
  ];

  allButtons.forEach(([button, filter]) => {
    if (!button) {
      return;
    }
    button.classList.toggle("btn-toggle-active", timelineFilter === filter);
  });
}

async function generateRoadmap() {
  AppUi.setLoading(generateRoadmapBtn, true, "Generating...");

  try {
    const response = await AppApi.generateRoadmap();
    const warning = response.warning ? ` ${response.warning}` : "";
    AppUi.showToast(`Plan updated. Created ${response.goalsCreated} goal(s).`);
    profileStatus.textContent = `Blueprint generated for ${AppUi.humanize(response.careerTrack)}.${warning}`;
    await initDashboard();
  } catch (error) {
    profileStatus.textContent = `Could not generate plan: ${error.message}`;
    AppUi.showToast(error.message, "error");
  } finally {
    AppUi.setLoading(generateRoadmapBtn, false);
  }
}

function statusClass(status) {
  if (status === "COMPLETED") {
    return "status-completed";
  }
  if (status === "IN_PROGRESS") {
    return "status-in-progress";
  }
  return "status-planned";
}

function priorityClass(priority) {
  if (priority === "URGENT") {
    return "priority-urgent";
  }
  if (priority === "HIGH") {
    return "priority-high";
  }
  if (priority === "LOW") {
    return "priority-low";
  }
  return "priority-medium";
}
