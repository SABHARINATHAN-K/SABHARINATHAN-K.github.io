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
    const stats = await AppApi.getGoalStats();
    renderMetrics(stats);
    renderStatus(stats);
    renderCategoryBars(stats);
    renderPriorityBars(stats);
    renderSummary(stats);
    AppUi.setMessage(messageBox, "Analytics loaded.");
  } catch (error) {
    AppUi.setMessage(messageBox, error.message);
    AppUi.showToast(error.message, "error");
  }
}

function renderMetrics(stats) {
  document.getElementById("metricTotal").textContent = String(stats.total || 0);
  document.getElementById("metricInProgress").textContent = String(stats.inProgress || 0);
  document.getElementById("metricCompleted").textContent = String(stats.completed || 0);
  document.getElementById("metricRate").textContent = `${Number(stats.completionRate || 0)}%`;
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
