const createForm = document.getElementById("createForm");
const updateForm = document.getElementById("updateForm");
const createBtn = document.getElementById("createBtn");
const updateBtn = document.getElementById("updateBtn");
const refreshBtn = document.getElementById("refreshBtn");
const searchInput = document.getElementById("searchInput");
const statusFilter = document.getElementById("statusFilter");
const goalsBody = document.getElementById("goalsBody");
const messageBox = document.getElementById("messageBox");
const templateCareerTrack = document.getElementById("templateCareerTrack");
const templateButtons = document.getElementById("templateButtons");
const logoutBtn = document.getElementById("logoutBtn");

let allGoals = [];
let templateMap = {};

if (AppApi.requireAuth()) {
  initGoalsPage();
}

logoutBtn.addEventListener("click", () => {
  AppApi.clearToken();
  window.location.href = "./login.html";
});

refreshBtn.addEventListener("click", loadGoals);
searchInput.addEventListener("input", applyFilters);
statusFilter.addEventListener("change", applyFilters);
templateCareerTrack.addEventListener("change", renderTemplateButtons);

createForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  const title = document.getElementById("createTitle").value.trim();
  const description = document.getElementById("createDescription").value.trim();
  const targetDate = document.getElementById("createTargetDate").value || null;

  AppUi.setLoading(createBtn, true, "Creating...");

  try {
    await AppApi.request("/api/v1/goals", {
      method: "POST",
      body: { title, description, targetDate }
    });
    createForm.reset();
    AppUi.setMessage(messageBox, "Goal created successfully.");
    AppUi.showToast("Goal created");
    await loadGoals();
  } catch (error) {
    AppUi.setMessage(messageBox, error.message);
  } finally {
    AppUi.setLoading(createBtn, false);
  }
});

updateForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  const goalId = document.getElementById("updateId").value;
  const title = document.getElementById("updateTitle").value.trim();
  const description = document.getElementById("updateDescription").value.trim();
  const status = document.getElementById("updateStatus").value;
  const targetDate = document.getElementById("updateTargetDate").value || null;

  AppUi.setLoading(updateBtn, true, "Updating...");

  try {
    await AppApi.request(`/api/v1/goals/${goalId}`, {
      method: "PUT",
      body: { title, description, status, targetDate }
    });
    AppUi.setMessage(messageBox, `Goal ${goalId} updated.`);
    AppUi.showToast("Goal updated");
    await loadGoals();
  } catch (error) {
    AppUi.setMessage(messageBox, error.message);
  } finally {
    AppUi.setLoading(updateBtn, false);
  }
});

async function initGoalsPage() {
  prefillFromQuery();
  await loadTemplates();
  await loadGoals();
}

async function loadTemplates() {
  try {
    templateMap = await AppApi.listGoalTemplates();
    const trackOptions = Object.keys(templateMap)
      .map((track) => `<option value="${track}">${AppUi.humanize(track)}</option>`)
      .join("");

    templateCareerTrack.innerHTML = trackOptions || "<option value=''>No templates</option>";
    renderTemplateButtons();
  } catch (error) {
    AppUi.setMessage(messageBox, `Could not load templates: ${error.message}`);
  }
}

function renderTemplateButtons() {
  const selectedTrack = templateCareerTrack.value;
  const templates = templateMap[selectedTrack] || [];

  if (!templates.length) {
    templateButtons.innerHTML = "<p class='muted'>No templates for this track.</p>";
    return;
  }

  templateButtons.innerHTML = templates
    .map((template) => {
      return `<div class="template-item">
        <p>${escapeHtml(template)}</p>
        <button class="template-add" data-template="${escapeAttr(template)}" type="button">Apply</button>
      </div>`;
    })
    .join("");

  templateButtons.querySelectorAll("button[data-template]").forEach((button) => {
    button.addEventListener("click", () => {
      const template = button.getAttribute("data-template") || "";
      document.getElementById("createTitle").value = template;
      document.getElementById("createDescription").value = `Planned from template: ${template}`;
      AppUi.showToast("Template applied");
    });
  });
}

async function loadGoals() {
  try {
    const res = await AppApi.request("/api/v1/goals");
    allGoals = res.data || [];
    applyFilters();
    AppUi.setMessage(messageBox, `Loaded ${allGoals.length} goal(s).`);
  } catch (error) {
    AppUi.setMessage(messageBox, error.message);
  }
}

function applyFilters() {
  const query = searchInput.value.trim().toLowerCase();
  const status = statusFilter.value;

  const filteredGoals = allGoals.filter((goal) => {
    const matchesStatus = status === "ALL" || goal.status === status;
    const searchable = `${goal.title || ""} ${goal.description || ""}`.toLowerCase();
    const matchesSearch = !query || searchable.includes(query);
    return matchesStatus && matchesSearch;
  });

  renderGoals(filteredGoals);
}

function renderGoals(goals) {
  if (!goals.length) {
    goalsBody.innerHTML = "<tr><td colspan='6'>No matching goals found.</td></tr>";
    return;
  }

  goalsBody.innerHTML = goals
    .map((goal) => {
      return `<tr>
        <td>${goal.id}</td>
        <td>${escapeHtml(goal.title || "")}</td>
        <td>${escapeHtml(goal.description || "")}</td>
        <td>${escapeHtml(goal.status || "")}</td>
        <td>${escapeHtml(goal.targetDate || "-")}</td>
        <td>
          <div class="action-group">
            <button class="action-btn edit" data-edit-id="${goal.id}" type="button">Edit</button>
            <button class="action-btn delete" data-delete-id="${goal.id}" type="button">Delete</button>
          </div>
        </td>
      </tr>`;
    })
    .join("");

  goalsBody.querySelectorAll("button[data-edit-id]").forEach((button) => {
    button.addEventListener("click", () => {
      const goalId = Number(button.getAttribute("data-edit-id"));
      const goal = allGoals.find((item) => item.id === goalId);
      if (!goal) {
        return;
      }

      document.getElementById("updateId").value = goal.id;
      document.getElementById("updateTitle").value = goal.title || "";
      document.getElementById("updateDescription").value = goal.description || "";
      document.getElementById("updateStatus").value = goal.status || "PLANNED";
      document.getElementById("updateTargetDate").value = goal.targetDate || "";
      AppUi.showToast(`Goal ${goalId} loaded into update form`);
    });
  });

  goalsBody.querySelectorAll("button[data-delete-id]").forEach((button) => {
    button.addEventListener("click", async () => {
      const goalId = button.getAttribute("data-delete-id");
      try {
        await AppApi.request(`/api/v1/goals/${goalId}`, { method: "DELETE" });
        AppUi.setMessage(messageBox, `Goal ${goalId} deleted.`);
        AppUi.showToast("Goal deleted");
        await loadGoals();
      } catch (error) {
        AppUi.setMessage(messageBox, error.message);
      }
    });
  });
}

function prefillFromQuery() {
  const params = new URLSearchParams(window.location.search);
  const title = params.get("title");
  const description = params.get("description");

  if (title) {
    document.getElementById("createTitle").value = title;
  }
  if (description) {
    document.getElementById("createDescription").value = description;
  }
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
