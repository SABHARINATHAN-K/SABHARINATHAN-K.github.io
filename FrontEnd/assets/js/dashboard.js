const logoutBtn = document.getElementById("logoutBtn");
const welcomeText = document.getElementById("welcomeText");
const careerPill = document.getElementById("careerPill");
const templateList = document.getElementById("templateList");
const profileName = document.getElementById("profileName");
const profileEmail = document.getElementById("profileEmail");
const profileRole = document.getElementById("profileRole");
const profileCareerTrack = document.getElementById("profileCareerTrack");
const profileStatus = document.getElementById("profileStatus");

if (AppApi.requireAuth()) {
  initDashboard();
}

logoutBtn.addEventListener("click", () => {
  AppApi.clearToken();
  window.location.href = "./login.html";
});

async function initDashboard() {
  try {
    const [profileRes, goalsRes, templateMap] = await Promise.all([
      AppApi.request("/api/v1/users/me"),
      AppApi.request("/api/v1/goals"),
      AppApi.listGoalTemplates()
    ]);

    const profile = profileRes.data;
    const goals = goalsRes.data || [];

    welcomeText.textContent = `Welcome, ${profile.fullName}`;
    profileName.textContent = profile.fullName;
    profileEmail.textContent = profile.email;
    profileRole.textContent = AppUi.humanize(profile.role);
    profileCareerTrack.textContent = AppUi.humanize(profile.careerTrack);
    profileStatus.textContent = "Profile loaded successfully.";
    careerPill.textContent = AppUi.humanize(profile.careerTrack);

    renderStats(goals);
    renderTemplateActions(profile.careerTrack, templateMap);
  } catch (error) {
    profileStatus.textContent = `Could not load profile: ${error.message}`;
  }
}

function renderStats(goals) {
  const total = goals.length;
  const planned = goals.filter((goal) => goal.status === "PLANNED").length;
  const inProgress = goals.filter((goal) => goal.status === "IN_PROGRESS").length;
  const completed = goals.filter((goal) => goal.status === "COMPLETED").length;

  document.getElementById("statTotal").textContent = total;
  document.getElementById("statPlanned").textContent = planned;
  document.getElementById("statInProgress").textContent = inProgress;
  document.getElementById("statCompleted").textContent = completed;
}

function renderTemplateActions(careerTrack, templateMap) {
  const templates = templateMap[careerTrack] || [];

  if (!templates.length) {
    templateList.innerHTML = "<p class='muted'>No templates available.</p>";
    return;
  }

  templateList.innerHTML = templates
    .map((template) => {
      return `<div class="template-item">
        <p>${escapeHtml(template)}</p>
        <button class="template-add" data-template="${escapeAttr(template)}" type="button">Use</button>
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

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

function escapeAttr(value) {
  return escapeHtml(value).replaceAll("`", "");
}
