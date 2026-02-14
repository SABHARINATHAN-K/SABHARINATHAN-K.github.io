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

if (AppApi.requireAuth()) {
  initDashboard();
}

logoutBtn.addEventListener("click", () => {
  AppApi.clearToken();
  window.location.href = "./login.html";
});

async function initDashboard() {
  try {
    const [profile, stats, goals, templateMap] = await Promise.all([
      AppApi.getMe(),
      AppApi.getGoalStats(),
      AppApi.listGoals(),
      AppApi.listGoalTemplates()
    ]);

    const firstName = (profile.fullName || "User").split(" ")[0];

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

    const completionRate = Number(stats.completionRate || 0);
    completionRing.style.setProperty("--progress", String(completionRate));
    completionRateValue.textContent = `${completionRate}%`;

    renderRecentGoals(goals || []);
    renderTemplateActions(profile.careerTrack, templateMap || {});
  } catch (error) {
    profileStatus.textContent = `Could not load dashboard: ${error.message}`;
    AppUi.showToast(error.message, "error");
  }
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
