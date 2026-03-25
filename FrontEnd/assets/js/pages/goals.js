const logoutBtn = document.getElementById("logoutBtn");
const refreshBtn = document.getElementById("refreshBtn");
const searchInput = document.getElementById("searchInput");
const statusFilter = document.getElementById("statusFilter");
const priorityFilter = document.getElementById("priorityFilter");
const categoryFilter = document.getElementById("categoryFilter");
const blueprintFilter = document.getElementById("blueprintFilter");
const sortBy = document.getElementById("sortBy");
const clearFiltersBtn = document.getElementById("clearFiltersBtn");
const resultText = document.getElementById("resultText");
const goalsGrid = document.getElementById("goalsGrid");
const messageBox = document.getElementById("messageBox");

const toggleTemplatesBtn = document.getElementById("toggleTemplatesBtn");
const closeTemplatesBtn = document.getElementById("closeTemplatesBtn");
const templatesPanel = document.getElementById("templatesPanel");
const templateCareerTrack = document.getElementById("templateCareerTrack");
const templateButtons = document.getElementById("templateButtons");

const createModal = document.getElementById("createModal");
const newGoalBtn = document.getElementById("newGoalBtn");
const closeCreateModalBtn = document.getElementById("closeCreateModalBtn");
const cancelCreateBtn = document.getElementById("cancelCreateBtn");
const createForm = document.getElementById("createForm");
const createBtn = document.getElementById("createBtn");

const createTitle = document.getElementById("createTitle");
const createDescription = document.getElementById("createDescription");
const createTargetDate = document.getElementById("createTargetDate");
const createPriority = document.getElementById("createPriority");
const createCategory = document.getElementById("createCategory");
const createTags = document.getElementById("createTags");

let allGoals = [];
let templatesMap = {};
let careerTracks = [];
let priorities = [];
let categories = [];

if (window.__careerGoalsPageLoaded) {
  // Prevent duplicate initialization if compatibility bridge loads this script.
  console.debug("goals page script already initialized");
} else {
  window.__careerGoalsPageLoaded = true;
}

const hasRequiredMarkup = Boolean(
  logoutBtn &&
  refreshBtn &&
  searchInput &&
  statusFilter &&
  priorityFilter &&
  categoryFilter &&
  blueprintFilter &&
  sortBy &&
  clearFiltersBtn &&
  resultText &&
  goalsGrid &&
  toggleTemplatesBtn &&
  closeTemplatesBtn &&
  templatesPanel &&
  templateCareerTrack &&
  templateButtons &&
  createModal &&
  newGoalBtn &&
  closeCreateModalBtn &&
  cancelCreateBtn &&
  createForm &&
  createBtn &&
  createTitle &&
  createDescription &&
  createTargetDate &&
  createPriority &&
  createCategory &&
  createTags
);

if (!hasRequiredMarkup) {
  console.warn("Goals page markup is incomplete. Skipping page bootstrap.");
} else if (AppApi.requireAuth()) {
  initGoalsPage();
}

if (logoutBtn) {
  logoutBtn.addEventListener("click", () => {
    AppApi.clearToken();
    window.location.href = "./login.html";
  });
}

if (refreshBtn) {
  refreshBtn.addEventListener("click", loadGoals);
}
if (searchInput) {
  searchInput.addEventListener("input", applyFilters);
}
if (statusFilter) {
  statusFilter.addEventListener("change", applyFilters);
}
if (priorityFilter) {
  priorityFilter.addEventListener("change", applyFilters);
}
if (categoryFilter) {
  categoryFilter.addEventListener("change", applyFilters);
}
if (blueprintFilter) {
  blueprintFilter.addEventListener("change", applyFilters);
}
if (sortBy) {
  sortBy.addEventListener("change", applyFilters);
}
if (clearFiltersBtn) {
  clearFiltersBtn.addEventListener("click", clearFilters);
}

if (toggleTemplatesBtn && templatesPanel) {
  toggleTemplatesBtn.addEventListener("click", () => {
    templatesPanel.classList.toggle("hidden");
  });
}

if (closeTemplatesBtn && templatesPanel) {
  closeTemplatesBtn.addEventListener("click", () => {
    templatesPanel.classList.add("hidden");
  });
}

if (templateCareerTrack) {
  templateCareerTrack.addEventListener("change", renderTemplateButtons);
}

if (newGoalBtn) {
  newGoalBtn.addEventListener("click", () => openCreateModal());
}
if (closeCreateModalBtn) {
  closeCreateModalBtn.addEventListener("click", closeCreateModal);
}
if (cancelCreateBtn) {
  cancelCreateBtn.addEventListener("click", closeCreateModal);
}

if (createModal) {
  createModal.addEventListener("click", (event) => {
    if (event.target === createModal) {
      closeCreateModal();
    }
  });
}

if (createForm) {
  createForm.addEventListener("submit", handleCreateGoal);
}

async function initGoalsPage() {
  try {
    await loadLookups();
    prefillFromQuery();
    await loadGoals();
  } catch (error) {
    AppUi.setMessage(messageBox, `Failed to initialize goals page: ${error.message}`);
    AppUi.showToast(error.message, "error");
  }
}

async function loadLookups() {
  const [tracks, priorityValues, categoryValues, templates] = await Promise.all([
    AppApi.listCareerTracks(),
    AppApi.listGoalPriorities(),
    AppApi.listGoalCategories(),
    AppApi.listGoalTemplates()
  ]);

  careerTracks = tracks || [];
  priorities = priorityValues || [];
  categories = categoryValues || [];
  templatesMap = templates || {};

  setOptions(templateCareerTrack, careerTracks);
  setOptions(createPriority, priorities, "MEDIUM");
  setOptions(createCategory, categories, "LEARNING");
  setFilterOptions(priorityFilter, priorities, "All Priorities");
  setFilterOptions(categoryFilter, categories, "All Categories");

  renderTemplateButtons();
}

function setOptions(selectElement, values, preferred) {
  selectElement.innerHTML = values
    .map((value) => `<option value="${AppUi.escapeAttr(value)}">${AppUi.escapeHtml(AppUi.humanize(value))}</option>`)
    .join("");

  if (preferred && values.includes(preferred)) {
    selectElement.value = preferred;
  }
}

function setFilterOptions(selectElement, values, allLabel) {
  selectElement.innerHTML = [
    `<option value="ALL">${allLabel}</option>`,
    ...values.map((value) => `<option value="${AppUi.escapeAttr(value)}">${AppUi.escapeHtml(AppUi.humanize(value))}</option>`)
  ].join("");
}

function renderTemplateButtons() {
  const selectedTrack = templateCareerTrack.value;
  const templates = templatesMap[selectedTrack] || [];

  if (!templates.length) {
    templateButtons.innerHTML = "<p class='text-muted'>No templates available for this track.</p>";
    return;
  }

  templateButtons.innerHTML = templates
    .map((template) => {
      return `
        <div class="template-item">
          <p>${AppUi.escapeHtml(template)}</p>
          <button class="btn btn-ghost btn-small" data-template="${AppUi.escapeAttr(template)}" type="button">Apply</button>
        </div>`;
    })
    .join("");

  templateButtons.querySelectorAll("button[data-template]").forEach((button) => {
    button.addEventListener("click", () => {
      const template = button.getAttribute("data-template") || "";
      createTitle.value = template;
      createDescription.value = `Planned from template: ${template}`;
      openCreateModal();
      templatesPanel.classList.add("hidden");
      AppUi.showToast("Template applied");
    });
  });

  AppUi.applyReveal(templateButtons);
}

async function loadGoals() {
  try {
    allGoals = await AppApi.listGoals();
    applyFilters();
    AppUi.setMessage(messageBox, `Loaded ${allGoals.length} goal(s).`);
  } catch (error) {
    AppUi.setMessage(messageBox, error.message);
    AppUi.showToast(error.message, "error");
  }
}

function clearFilters() {
  searchInput.value = "";
  statusFilter.value = "ALL";
  priorityFilter.value = "ALL";
  categoryFilter.value = "ALL";
  blueprintFilter.value = "ALL";
  sortBy.value = "UPDATED_DESC";
  applyFilters();
}

function applyFilters() {
  const searchQuery = searchInput.value.trim().toLowerCase();
  const status = statusFilter.value;
  const priority = priorityFilter.value;
  const category = categoryFilter.value;
  const blueprint = blueprintFilter.value;

  const filtered = allGoals.filter((goal) => {
    const matchesStatus = status === "ALL" || goal.status === status;
    const matchesPriority = priority === "ALL" || goal.priority === priority;
    const matchesCategory = category === "ALL" || goal.category === category;
    const matchesBlueprint = blueprint === "ALL"
      || (blueprint === "BLUEPRINT" && goal.blueprintGoal)
      || (blueprint === "MANUAL" && !goal.blueprintGoal);

    const searchable = `${goal.title || ""} ${goal.description || ""}`.toLowerCase();
    const matchesSearch = !searchQuery || searchable.includes(searchQuery);

    return matchesStatus && matchesPriority && matchesCategory && matchesBlueprint && matchesSearch;
  });

  const sorted = sortGoals(filtered, sortBy.value);
  resultText.textContent = `Showing ${sorted.length} of ${allGoals.length} goals`;
  renderGoals(sorted);
}

function sortGoals(goals, mode) {
  const sorted = [...goals];

  if (mode === "TARGET_ASC") {
    sorted.sort((a, b) => {
      const aTime = a.targetDate ? new Date(a.targetDate).getTime() : Number.MAX_SAFE_INTEGER;
      const bTime = b.targetDate ? new Date(b.targetDate).getTime() : Number.MAX_SAFE_INTEGER;
      return aTime - bTime;
    });
    return sorted;
  }

  if (mode === "PROGRESS_DESC") {
    sorted.sort((a, b) => Number(b.progress || 0) - Number(a.progress || 0));
    return sorted;
  }

  if (mode === "TITLE_ASC") {
    sorted.sort((a, b) => String(a.title || "").localeCompare(String(b.title || "")));
    return sorted;
  }

  sorted.sort((a, b) => new Date(b.updatedAt || 0).getTime() - new Date(a.updatedAt || 0).getTime());
  return sorted;
}

function renderGoals(goals) {
  if (!goals.length) {
    goalsGrid.innerHTML = `
      <div class="empty-state" style="grid-column: 1 / -1;">
        <h3>No goals found</h3>
        <p>${allGoals.length ? "Try adjusting your filters." : "Start by creating your first goal."}</p>
        <button class="btn btn-primary" id="emptyCreateBtn" type="button" style="margin-top:0.65rem;">Create Goal</button>
      </div>`;

    const emptyCreateBtn = document.getElementById("emptyCreateBtn");
    if (emptyCreateBtn) {
      emptyCreateBtn.addEventListener("click", openCreateModal);
    }
    return;
  }

  goalsGrid.innerHTML = goals
    .map((goal) => {
      const taskTotal = Array.isArray(goal.tasks) ? goal.tasks.length : 0;
      const taskCompleted = taskTotal
        ? goal.tasks.filter((task) => task && task.completed).length
        : 0;

      return `
        <article class="goal-card">
          <div class="top">
            <span class="status-pill ${statusClass(goal.status)}">${AppUi.escapeHtml(AppUi.humanize(goal.status || "PLANNED"))}</span>
            <span class="priority-pill ${priorityClass(goal.priority)}">${AppUi.escapeHtml(AppUi.humanize(goal.priority || "MEDIUM"))}</span>
          </div>

          <h3>${AppUi.escapeHtml(goal.title || "Untitled Goal")}</h3>
          <p class="text-muted">${AppUi.escapeHtml(goal.description || "No description")}</p>

          <div class="progress-line">
            <div class="progress-track"><div class="progress-value" style="width:${Number(goal.progress || 0)}%"></div></div>
            <strong>${Number(goal.progress || 0)}%</strong>
          </div>

          <div class="goal-meta">
            <span>${goal.targetDate ? AppUi.formatDate(goal.targetDate) : "No deadline"}</span>
            <span class="chip">${AppUi.escapeHtml(AppUi.humanize(goal.category || "LEARNING"))}</span>
            <span class="chip">${taskCompleted}/${taskTotal} tasks</span>
          </div>

          <div class="actions">
            <a class="btn btn-secondary btn-small" href="./goal-detail.html?id=${goal.id}">Open Goal</a>
            <button class="btn btn-danger btn-small" data-delete-id="${goal.id}" type="button">Remove</button>
          </div>
        </article>`;
    })
    .join("");

  goalsGrid.querySelectorAll("button[data-delete-id]").forEach((button) => {
    button.addEventListener("click", async () => {
      const goalId = button.getAttribute("data-delete-id");
      if (!goalId) {
        return;
      }

      if (!window.confirm("Are you sure you want to delete this goal?")) {
        return;
      }

      try {
        await AppApi.deleteGoal(goalId);
        AppUi.showToast("Goal deleted");
        await loadGoals();
      } catch (error) {
        AppUi.setMessage(messageBox, error.message);
        AppUi.showToast(error.message, "error");
      }
    });
  });

  AppUi.applyReveal(goalsGrid);
}

async function handleCreateGoal(event) {
  event.preventDefault();

  const payload = {
    title: createTitle.value.trim(),
    description: createDescription.value.trim() || null,
    targetDate: createTargetDate.value || null,
    priority: createPriority.value || null,
    category: createCategory.value || null,
    tags: AppUi.parseTagText(createTags.value)
  };

  AppUi.setLoading(createBtn, true, "Creating...");

  try {
    await AppApi.createGoal(payload);
    createForm.reset();

    if (priorities.includes("MEDIUM")) {
      createPriority.value = "MEDIUM";
    }
    if (categories.includes("LEARNING")) {
      createCategory.value = "LEARNING";
    }

    closeCreateModal();
    AppUi.showToast("Goal created successfully");
    await loadGoals();
  } catch (error) {
    AppUi.setMessage(messageBox, error.message);
    AppUi.showToast(error.message, "error");
  } finally {
    AppUi.setLoading(createBtn, false);
  }
}

function openCreateModal() {
  createModal.classList.add("show");
}

function closeCreateModal() {
  createModal.classList.remove("show");
}

function prefillFromQuery() {
  const params = new URLSearchParams(window.location.search);
  const title = params.get("title");
  const description = params.get("description");

  if (title) {
    createTitle.value = title;
    createDescription.value = description || "";
    openCreateModal();
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
