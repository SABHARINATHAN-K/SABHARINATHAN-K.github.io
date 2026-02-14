const logoutBtn = document.getElementById("logoutBtn");
const messageBox = document.getElementById("messageBox");
const progressRange = document.getElementById("progressRange");
const progressValue = document.getElementById("progressValue");
const saveProgressBtn = document.getElementById("saveProgressBtn");
const saveNotesBtn = document.getElementById("saveNotesBtn");
const notesInput = document.getElementById("notesInput");
const deleteGoalBtn = document.getElementById("deleteGoalBtn");

const statusBadge = document.getElementById("statusBadge");
const priorityBadge = document.getElementById("priorityBadge");
const goalTitle = document.getElementById("goalTitle");
const goalDescription = document.getElementById("goalDescription");
const metaCategory = document.getElementById("metaCategory");
const metaTargetDate = document.getElementById("metaTargetDate");
const metaCreatedDate = document.getElementById("metaCreatedDate");
const metaProgress = document.getElementById("metaProgress");
const tagsPanel = document.getElementById("tagsPanel");
const tagsList = document.getElementById("tagsList");

const statusButtons = Array.from(document.querySelectorAll("button[data-status]"));

let goalId = null;
let goal = null;

if (AppApi.requireAuth()) {
  initGoalDetailPage();
}

logoutBtn.addEventListener("click", () => {
  AppApi.clearToken();
  window.location.href = "./login.html";
});

progressRange.addEventListener("input", () => {
  progressValue.textContent = `${progressRange.value}%`;
});

saveProgressBtn.addEventListener("click", saveProgress);
saveNotesBtn.addEventListener("click", saveNotes);
deleteGoalBtn.addEventListener("click", deleteGoal);

statusButtons.forEach((button) => {
  button.addEventListener("click", () => changeStatus(button.dataset.status));
});

async function initGoalDetailPage() {
  const params = new URLSearchParams(window.location.search);
  goalId = params.get("id");

  if (!goalId) {
    window.location.href = "./goals.html";
    return;
  }

  await loadGoal();
}

async function loadGoal() {
  try {
    goal = await AppApi.getGoal(goalId);
    renderGoal(goal);
    AppUi.setMessage(messageBox, `Loaded goal #${goal.id}`);
  } catch (error) {
    AppUi.setMessage(messageBox, error.message);
    AppUi.showToast(error.message, "error");
    setTimeout(() => {
      window.location.href = "./goals.html";
    }, 700);
  }
}

function renderGoal(data) {
  goalTitle.textContent = data.title || "Untitled Goal";
  goalDescription.textContent = data.description || "No description";

  statusBadge.className = `status-pill ${statusClass(data.status)}`;
  statusBadge.textContent = AppUi.humanize(data.status || "PLANNED");

  priorityBadge.className = `priority-pill ${priorityClass(data.priority)}`;
  priorityBadge.textContent = `${AppUi.humanize(data.priority || "MEDIUM")} Priority`;

  metaCategory.textContent = AppUi.humanize(data.category || "LEARNING");
  metaTargetDate.textContent = data.targetDate ? AppUi.formatDate(data.targetDate) : "Not set";
  metaCreatedDate.textContent = data.createdAt ? AppUi.formatDate(data.createdAt) : "-";
  metaProgress.textContent = `${Number(data.progress || 0)}%`;

  progressRange.value = String(Number(data.progress || 0));
  progressValue.textContent = `${Number(data.progress || 0)}%`;
  notesInput.value = data.notes || "";

  if (data.tags && data.tags.length) {
    tagsPanel.classList.remove("hidden");
    tagsList.innerHTML = data.tags
      .map((tag) => `<span class="chip">${AppUi.escapeHtml(tag)}</span>`)
      .join("");
  } else {
    tagsPanel.classList.add("hidden");
    tagsList.innerHTML = "";
  }

  renderStatusButtons(data.status);
}

function renderStatusButtons(currentStatus) {
  statusButtons.forEach((button) => {
    const isActive = button.dataset.status === currentStatus;
    button.classList.toggle("btn-primary", isActive);
    button.classList.toggle("btn-ghost", !isActive);
  });
}

async function saveProgress() {
  AppUi.setLoading(saveProgressBtn, true, "Saving...");

  try {
    goal = await AppApi.updateGoal(goalId, {
      progress: Number(progressRange.value)
    });

    renderGoal(goal);
    AppUi.showToast("Progress updated");
  } catch (error) {
    AppUi.setMessage(messageBox, error.message);
    AppUi.showToast(error.message, "error");
  } finally {
    AppUi.setLoading(saveProgressBtn, false);
  }
}

async function changeStatus(status) {
  try {
    goal = await AppApi.updateGoal(goalId, { status });
    renderGoal(goal);
    AppUi.showToast(`Status changed to ${AppUi.humanize(status)}`);
  } catch (error) {
    AppUi.setMessage(messageBox, error.message);
    AppUi.showToast(error.message, "error");
  }
}

async function saveNotes() {
  AppUi.setLoading(saveNotesBtn, true, "Saving...");

  try {
    goal = await AppApi.updateGoal(goalId, {
      notes: notesInput.value.trim() || null
    });
    renderGoal(goal);
    AppUi.showToast("Notes saved");
  } catch (error) {
    AppUi.setMessage(messageBox, error.message);
    AppUi.showToast(error.message, "error");
  } finally {
    AppUi.setLoading(saveNotesBtn, false);
  }
}

async function deleteGoal() {
  if (!window.confirm("Are you sure you want to delete this goal?")) {
    return;
  }

  try {
    await AppApi.deleteGoal(goalId);
    AppUi.showToast("Goal deleted");
    window.location.href = "./goals.html";
  } catch (error) {
    AppUi.setMessage(messageBox, error.message);
    AppUi.showToast(error.message, "error");
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
