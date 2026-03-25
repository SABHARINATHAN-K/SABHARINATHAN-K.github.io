const logoutBtn = document.getElementById("logoutBtn");
const adminInitial = document.getElementById("adminInitial");
const adminName = document.getElementById("adminName");
const adminEmail = document.getElementById("adminEmail");
const studentProfileForm = document.getElementById("studentProfileForm");
const saveStudentBtn = document.getElementById("saveStudentBtn");

let studentId = null;
let studentDetail = null;

if (!AppApi.getToken()) {
  window.location.href = "./login.html?portal=admin";
} else {
  initAdminUserPage();
}

logoutBtn?.addEventListener("click", () => {
  AppApi.clearToken();
  window.location.href = "./login.html?portal=admin";
});

studentProfileForm?.addEventListener("submit", saveStudent);

async function initAdminUserPage() {
  const params = new URLSearchParams(window.location.search);
  studentId = Number(params.get("id"));

  if (Number.isNaN(studentId) || studentId <= 0) {
    AppUi.showToast("Invalid student id.", "error");
    window.location.href = "./admin.html";
    return;
  }

  try {
    const [profile, roles, tracks, detail] = await Promise.all([
      AppApi.getMe(),
      AppApi.listAdminRoles(),
      AppApi.listCareerTracks(),
      AppApi.getAdminUserDetail(studentId)
    ]);

    if (!(window.AppSession && typeof window.AppSession.isAdminProfile === "function" && window.AppSession.isAdminProfile(profile))) {
      window.location.href = "./home.html";
      return;
    }

    adminInitial.textContent = (profile.fullName || "A").charAt(0).toUpperCase();
    adminName.textContent = profile.fullName || "Admin";
    adminEmail.textContent = profile.email || "-";

    setSelectOptions(document.getElementById("studentRole"), roles || []);
    setSelectOptions(document.getElementById("studentCareerTrack"), tracks || []);

    studentDetail = detail;
    renderStudentDetail(detail);
    AppUi.applyReveal(document);
  } catch (error) {
    AppUi.showToast(error.message, "error");
  }
}

function setSelectOptions(select, values) {
  if (!select) {
    return;
  }
  select.innerHTML = (values || [])
    .map((value) => `<option value="${AppUi.escapeAttr(value)}">${AppUi.escapeHtml(AppUi.humanize(value))}</option>`)
    .join("");
}

function renderStudentDetail(detail) {
  const profile = detail && detail.profile ? detail.profile : null;
  const goals = detail && Array.isArray(detail.goals) ? detail.goals : [];
  const readiness = detail ? detail.technicalReadiness : null;

  if (!profile) {
    AppUi.showToast("Student profile not found.", "error");
    return;
  }

  document.getElementById("studentNameHeading").textContent = profile.fullName || "Student";
  document.getElementById("studentHeroText").textContent = `${profile.email || "No email"} · ${AppUi.humanize(profile.careerTrack || "no track")}`;
  document.getElementById("studentFullName").value = profile.fullName || "";
  document.getElementById("studentEmail").value = profile.email || "";
  document.getElementById("studentRole").value = profile.role || "";
  document.getElementById("studentCareerTrack").value = profile.careerTrack || "";
  document.getElementById("studentLocation").value = profile.location || "";
  document.getElementById("studentBio").value = profile.bio || "";
  document.getElementById("studentOnboardingCompleted").checked = Boolean(profile.onboardingCompleted);

  const onboardingChip = document.getElementById("studentOnboardingChip");
  onboardingChip.textContent = profile.onboardingCompleted ? "Setup complete" : "Setup pending";
  onboardingChip.className = `chip ${profile.onboardingCompleted ? "status-completed" : "status-planned"}`;

  const goalCount = goals.length;
  const completedGoalCount = goals.filter((goal) => String(goal.status || "") === "COMPLETED").length;
  const taskList = goals.flatMap((goal) => Array.isArray(goal.tasks) ? goal.tasks : []);
  const completedTaskCount = taskList.filter((task) => task && task.completed).length;

  setText("studentGoalCount", goalCount);
  setText("studentCompletedGoalCount", completedGoalCount);
  setText("studentTaskCount", taskList.length);
  setText("studentCompletedTaskCount", completedTaskCount);
  document.getElementById("studentPlanSummary").textContent = `${goalCount} goal${goalCount === 1 ? "" : "s"}`;

  renderReadiness(readiness, profile);
  renderGoals(goals);
}

function renderReadiness(readiness, profile) {
  const history = readiness && Array.isArray(readiness.history) ? readiness.history : [];
  const skillAreas = readiness && Array.isArray(readiness.latestSkillAreas) ? readiness.latestSkillAreas : [];
  const hasBaseline = Boolean(readiness && readiness.currentLevel);

  document.getElementById("studentHistoryCount").textContent = `${history.length} checkpoint${history.length === 1 ? "" : "s"}`;
  document.getElementById("studentReadinessTrack").textContent = AppUi.humanize((readiness && readiness.careerTrack) || (profile && profile.careerTrack) || "-");
  document.getElementById("studentReadinessSummary").textContent = readiness && readiness.summary
    ? readiness.summary
    : "No readiness result recorded yet.";

  if (!hasBaseline) {
    document.getElementById("studentLevelMetric").textContent = "-";
    document.getElementById("studentScoreMetric").textContent = "-";
    document.getElementById("studentLastAssessed").textContent = "Not yet";
    document.getElementById("studentReassessment").textContent = "Baseline pending";
    document.getElementById("studentImprovementText").textContent = "No comparison available yet.";
    const chip = document.getElementById("studentReadinessChip");
    chip.textContent = "No baseline";
    chip.className = "chip status-planned";
    document.getElementById("studentSkillBars").innerHTML = "<p class='text-muted'>No skill breakdown available yet.</p>";
    document.getElementById("studentHistoryBars").innerHTML = "<p class='text-muted'>No readiness history recorded yet.</p>";
    return;
  }

  document.getElementById("studentLevelMetric").textContent = AppUi.humanize(readiness.currentLevel || "-");
  document.getElementById("studentScoreMetric").textContent = `${Number(readiness.currentPercentageScore || 0)}%`;
  document.getElementById("studentLastAssessed").textContent = AppUi.formatDate(readiness.lastAssessedAt);
  document.getElementById("studentReassessment").textContent = readiness.reassessmentDue
    ? `Due now (since ${AppUi.formatDate(readiness.recommendedReassessmentAt)})`
    : `Recommended by ${AppUi.formatDate(readiness.recommendedReassessmentAt)}`;
  document.getElementById("studentImprovementText").textContent = formatImprovement(readiness.improvementPercentagePoints);

  const chip = document.getElementById("studentReadinessChip");
  chip.textContent = readiness.reassessmentDue ? "Reassessment due" : "Tracking";
  chip.className = `chip ${readiness.reassessmentDue ? "priority-high" : "status-completed"}`;

  renderSkillBars(document.getElementById("studentSkillBars"), skillAreas);
  renderHistoryBars(document.getElementById("studentHistoryBars"), history);
}

function renderSkillBars(root, skillAreas) {
  if (!Array.isArray(skillAreas) || !skillAreas.length) {
    root.innerHTML = "<p class='text-muted'>No skill breakdown available.</p>";
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

function renderHistoryBars(root, history) {
  if (!Array.isArray(history) || !history.length) {
    root.innerHTML = "<p class='text-muted'>No readiness history recorded yet.</p>";
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
          <p class="text-muted">${AppUi.escapeHtml(AppUi.humanize(item.proficiencyLevel || "BEGINNER"))}</p>
        </div>`;
    })
    .join("");
}

function renderGoals(goals) {
  const root = document.getElementById("studentGoalsList");

  if (!Array.isArray(goals) || !goals.length) {
    root.innerHTML = "<p class='text-muted'>This student does not have any goals yet.</p>";
    return;
  }

  root.innerHTML = goals
    .map((goal) => {
      const tasks = Array.isArray(goal.tasks) ? goal.tasks : [];
      const tasksMarkup = tasks.length
        ? tasks.map((task) => `
            <div class="task-item">
              <div>
                <strong>${AppUi.escapeHtml(task.title || "Task")}</strong>
                <p class="text-muted">${AppUi.escapeHtml(task.details || "No details")}</p>
              </div>
              <div class="task-item-actions">
                <span class="chip ${task.completed ? "status-completed" : "status-in-progress"}">${task.completed ? "Completed" : "Pending"}</span>
                <span class="text-muted">${task.dueDate ? `Due ${AppUi.escapeHtml(AppUi.formatDate(task.dueDate))}` : "No due date"}</span>
              </div>
            </div>`).join("")
        : "<p class='text-muted'>No tasks for this goal yet.</p>";

      return `
        <article class="section-card admin-goal-card">
          <div class="section-header">
            <div>
              <h4>${AppUi.escapeHtml(goal.title || "Goal")}</h4>
              <p class="text-muted">${AppUi.escapeHtml(goal.description || "No description")}</p>
            </div>
            <div class="toolbar-actions">
              <span class="chip ${goal.status === "COMPLETED" ? "status-completed" : goal.status === "IN_PROGRESS" ? "status-in-progress" : "status-planned"}">${AppUi.escapeHtml(AppUi.humanize(goal.status || "PLANNED"))}</span>
              <span class="chip ${priorityClass(goal.priority)}">${AppUi.escapeHtml(AppUi.humanize(goal.priority || "MEDIUM"))}</span>
            </div>
          </div>
          <div class="admin-user-meta-grid" style="margin-bottom:0.8rem;">
            <span>Category: <strong>${AppUi.escapeHtml(AppUi.humanize(goal.category || "GENERAL"))}</strong></span>
            <span>Progress: <strong>${Number(goal.progress || 0)}%</strong></span>
            <span>Target: <strong>${goal.targetDate ? AppUi.escapeHtml(AppUi.formatDate(goal.targetDate)) : "Not set"}</strong></span>
            <span>Phase: <strong>${AppUi.escapeHtml(goal.blueprintPhaseTitle || "Manual")}</strong></span>
          </div>
          <div class="task-list">${tasksMarkup}</div>
        </article>`;
    })
    .join("");

  AppUi.applyReveal(root);
}

async function saveStudent(event) {
  event.preventDefault();

  if (!studentId) {
    return;
  }

  const payload = {
    fullName: document.getElementById("studentFullName").value.trim(),
    email: document.getElementById("studentEmail").value.trim(),
    role: document.getElementById("studentRole").value,
    careerTrack: document.getElementById("studentCareerTrack").value,
    location: document.getElementById("studentLocation").value.trim() || null,
    bio: document.getElementById("studentBio").value.trim() || null,
    onboardingCompleted: document.getElementById("studentOnboardingCompleted").checked
  };

  AppUi.setLoading(saveStudentBtn, true, "Saving...");

  try {
    studentDetail = await AppApi.updateAdminUser(studentId, payload);
    renderStudentDetail(studentDetail);
    AppUi.showToast("Student updated");
  } catch (error) {
    AppUi.showToast(error.message, "error");
  } finally {
    AppUi.setLoading(saveStudentBtn, false);
  }
}

function setText(id, value) {
  const element = document.getElementById(id);
  if (element) {
    element.textContent = String(value);
  }
}

function formatImprovement(improvement) {
  if (improvement === null || improvement === undefined) {
    return "This is the learner's baseline readiness result.";
  }
  if (improvement > 0) {
    return `Improved by ${improvement} percentage points from the previous attempt.`;
  }
  if (improvement < 0) {
    return `Dropped by ${Math.abs(improvement)} percentage points from the previous attempt.`;
  }
  return "Score unchanged from the previous attempt.";
}

function clampPercentage(value) {
  const numeric = Number(value || 0);
  return Math.max(0, Math.min(100, Math.round(numeric)));
}

function buildBarFillStyle(percentage) {
  const width = Math.max(6, clampPercentage(percentage));
  const hue = width >= 85 ? "#16a34a" : width >= 65 ? "#2563eb" : width >= 45 ? "#f59e0b" : "#dc2626";
  return `width:${width}%; background:${hue}`;
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
