const logoutBtn = document.getElementById("logoutBtn");
const messageBox = document.getElementById("messageBox");

if (AppApi.requireAuth()) {
  initAnalyticsPage();
}

logoutBtn.addEventListener("click", () => {
  AppApi.clearToken();
  window.location.href = "./login.html";
});

async function initAnalyticsPage() {
  try {
    const [profile, stats, technicalProgress] = await Promise.all([
      AppApi.getMe(),
      AppApi.getGoalStats(),
      AppApi.getTechnicalAssessmentProgress().catch(() => null)
    ]);

    if (profile.onboardingCompleted === false) {
      window.location.href = "./technical-readiness.html";
      return;
    }

    renderMetrics(stats, technicalProgress);
    renderStatus(stats);
    renderCategoryBars(stats);
    renderPriorityBars(stats);
    renderSummary(stats);
    renderTechnicalAnalytics(profile, technicalProgress);
    AppUi.applyReveal(document);
    AppUi.setMessage(messageBox, "Analytics loaded.");
  } catch (error) {
    AppUi.setMessage(messageBox, error.message);
    AppUi.showToast(error.message, "error");
  }
}

function renderMetrics(stats, technicalProgress) {
  document.getElementById("metricTotal").textContent = String(stats.total || 0);
  document.getElementById("metricInProgress").textContent = String(stats.inProgress || 0);
  document.getElementById("metricCompleted").textContent = String(stats.completed || 0);
  document.getElementById("metricRate").textContent = `${Number(stats.completionRate || 0)}%`;
  document.getElementById("metricTechnicalLevel").textContent = AppUi.humanize(technicalProgress && technicalProgress.currentLevel ? technicalProgress.currentLevel : "-");
  document.getElementById("metricTechnicalScore").textContent = technicalProgress && technicalProgress.currentPercentageScore !== null && technicalProgress.currentPercentageScore !== undefined
    ? `${Number(technicalProgress.currentPercentageScore)}%`
    : "-";
  document.getElementById("metricTechnicalTrend").textContent = formatTrendMetric(technicalProgress && technicalProgress.improvementPercentagePoints);
  document.getElementById("metricTechnicalHistory").textContent = String(Array.isArray(technicalProgress && technicalProgress.history) ? technicalProgress.history.length : 0);
}

function renderStatus(stats) {
  const completionRate = Number(stats.completionRate || 0);
  const statusDonut = document.getElementById("statusDonut");
  const statusRateText = document.getElementById("statusRateText");
  const statusLegend = document.getElementById("statusLegend");

  statusDonut.style.setProperty("--progress", String(completionRate));
  statusRateText.textContent = `${completionRate}%`;

  const statusData = [
    { name: "Planned", value: Number(stats.planned || 0), color: "#60a5fa" },
    { name: "In Progress", value: Number(stats.inProgress || 0), color: "#f59e0b" },
    { name: "Completed", value: Number(stats.completed || 0), color: "#22c55e" }
  ];

  statusLegend.innerHTML = statusData
    .map((item) => {
      return `
        <div class="legend-item">
          <span class="name"><span class="swatch" style="background:${item.color}"></span>${item.name}</span>
          <strong>${item.value}</strong>
        </div>`;
    })
    .join("");
}

function renderCategoryBars(stats) {
  const categoryBars = document.getElementById("categoryBars");
  const categories = (stats.byCategory || []).filter((item) => Number(item.count || 0) > 0);
  const palette = ["#3b82f6", "#8b5cf6", "#ec4899", "#f59e0b", "#10b981", "#06b6d4"];

  if (!categories.length) {
    categoryBars.innerHTML = "<p class='text-muted'>No category data yet.</p>";
    return;
  }

  categoryBars.innerHTML = categories
    .map((item, index) => {
      const color = palette[index % palette.length];
      const count = Number(item.count || 0);
      const width = Math.max(8, Math.round((count / Math.max(1, Number(stats.total || 1))) * 100));

      return `
        <div class="bar-row">
          <div class="bar-head">
            <span>${AppUi.escapeHtml(AppUi.humanize(item.category || "CATEGORY"))}</span>
            <strong>${count}</strong>
          </div>
          <div class="bar-track"><div class="bar-fill" style="width:${width}%; background:${color}"></div></div>
        </div>`;
    })
    .join("");
}

function renderPriorityBars(stats) {
  const priorityBars = document.getElementById("priorityBars");
  const priorities = stats.byPriority || [];

  const colorMap = {
    LOW: "#94a3b8",
    MEDIUM: "#60a5fa",
    HIGH: "#f59e0b",
    URGENT: "#ef4444"
  };

  priorityBars.innerHTML = priorities
    .map((item) => {
      const count = Number(item.count || 0);
      const width = Math.max(4, Math.round((count / Math.max(1, Number(stats.total || 1))) * 100));
      const color = colorMap[item.priority] || "#3b82f6";

      return `
        <div class="bar-row">
          <div class="bar-head">
            <span>${AppUi.escapeHtml(AppUi.humanize(item.priority || "MEDIUM"))}</span>
            <strong>${count}</strong>
          </div>
          <div class="bar-track"><div class="bar-fill" style="width:${width}%; background:${color}"></div></div>
        </div>`;
    })
    .join("");
}

function renderSummary(stats) {
  const total = Number(stats.total || 0);
  const completed = Number(stats.completed || 0);
  const inProgress = Number(stats.inProgress || 0);
  const planned = Number(stats.planned || 0);
  const activeCategories = (stats.byCategory || []).filter((item) => Number(item.count || 0) > 0).length;

  document.getElementById("summaryActiveGoals").textContent = String(planned + inProgress);
  document.getElementById("summaryAchievementRate").textContent = `${Number(stats.completionRate || 0)}%`;
  document.getElementById("summaryRemaining").textContent = String(Math.max(0, total - completed));
  document.getElementById("summaryCategories").textContent = String(activeCategories);
}

function renderTechnicalAnalytics(profile, technicalProgress) {
  const technicalTrackText = document.getElementById("technicalTrackText");
  const technicalLastAssessedText = document.getElementById("technicalLastAssessedText");
  const technicalReassessmentText = document.getElementById("technicalReassessmentText");
  const technicalStatusText = document.getElementById("technicalStatusText");
  const technicalSummaryText = document.getElementById("technicalSummaryText");
  const technicalAnalyticsAction = document.getElementById("technicalAnalyticsAction");
  const technicalSkillBars = document.getElementById("technicalSkillBars");
  const technicalHistoryBars = document.getElementById("technicalHistoryBars");

  const benchmarkTrack = technicalProgress && technicalProgress.careerTrack
    ? technicalProgress.careerTrack
    : profile.careerTrack;

  technicalTrackText.textContent = AppUi.humanize(benchmarkTrack || "-");

  if (!technicalProgress || !technicalProgress.currentLevel) {
    technicalLastAssessedText.textContent = "Not yet";
    technicalReassessmentText.textContent = "Baseline pending";
    technicalStatusText.textContent = "No baseline";
    technicalSummaryText.textContent = technicalProgress && technicalProgress.summary
      ? technicalProgress.summary
      : "Take your first technical readiness check to establish a baseline for this analytics view.";
    technicalAnalyticsAction.textContent = "Take Readiness Check";
    technicalSkillBars.innerHTML = "<p class='text-muted'>Skill analysis appears after your first completed readiness check.</p>";
    technicalHistoryBars.innerHTML = "<p class='text-muted'>Checkpoint history appears after your first completed readiness check.</p>";
    return;
  }

  technicalLastAssessedText.textContent = AppUi.formatDate(technicalProgress.lastAssessedAt);
  technicalReassessmentText.textContent = technicalProgress.reassessmentDue
    ? `Due now (since ${AppUi.formatDate(technicalProgress.recommendedReassessmentAt)})`
    : `Recommended by ${AppUi.formatDate(technicalProgress.recommendedReassessmentAt)}`;
  technicalStatusText.textContent = technicalProgress.reassessmentDue ? "Reassessment due" : "Tracking";
  technicalSummaryText.textContent = technicalProgress.summary || "Technical readiness summary unavailable.";
  technicalAnalyticsAction.textContent = technicalProgress.reassessmentDue ? "Reassess Readiness Check" : "Retake Readiness Check";

  renderTechnicalSkillBars(technicalSkillBars, technicalProgress.latestSkillAreas || []);
  renderTechnicalHistoryBars(technicalHistoryBars, technicalProgress.history || []);
}

function renderTechnicalSkillBars(root, skillAreas) {
  if (!Array.isArray(skillAreas) || !skillAreas.length) {
    root.innerHTML = "<p class='text-muted'>No technical skill breakdown available.</p>";
    return;
  }

  root.innerHTML = skillAreas
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
}

function renderTechnicalHistoryBars(root, history) {
  if (!Array.isArray(history) || !history.length) {
    root.innerHTML = "<p class='text-muted'>No technical readiness checkpoints recorded yet.</p>";
    return;
  }

  root.innerHTML = history
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

function formatTrendMetric(improvement) {
  if (improvement === null || improvement === undefined) {
    return "Baseline";
  }
  if (improvement > 0) {
    return `+${improvement} pts`;
  }
  if (improvement < 0) {
    return `${improvement} pts`;
  }
  return "0 pts";
}
