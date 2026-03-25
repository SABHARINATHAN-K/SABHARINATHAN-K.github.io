const logoutBtn = document.getElementById("logoutBtn");
const notesInput = document.getElementById("notesInput");
const saveNotesBtn = document.getElementById("saveNotesBtn");
const deleteGoalBtn = document.getElementById("deleteGoalBtn");
const goalTargetDateInput = document.getElementById("goalTargetDateInput");
const saveTargetDateBtn = document.getElementById("saveTargetDateBtn");
const markGoalCompletedBtn = document.getElementById("markGoalCompletedBtn");
const markGoalPlannedBtn = document.getElementById("markGoalPlannedBtn");

const statusBadge = document.getElementById("statusBadge");
const priorityBadge = document.getElementById("priorityBadge");
const goalTitle = document.getElementById("goalTitle");
const goalDescription = document.getElementById("goalDescription");
const metaCategory = document.getElementById("metaCategory");
const metaTargetDate = document.getElementById("metaTargetDate");
const metaCreatedDate = document.getElementById("metaCreatedDate");
const metaProgress = document.getElementById("metaProgress");
const taskSummary = document.getElementById("taskSummary");
const taskList = document.getElementById("taskList");

const createTaskForm = document.getElementById("createTaskForm");
const taskTitleInput = document.getElementById("taskTitleInput");
const taskDueDateInput = document.getElementById("taskDueDateInput");
const taskWeightInput = document.getElementById("taskWeightInput");
const taskDetailsInput = document.getElementById("taskDetailsInput");
const createTaskBtn = document.getElementById("createTaskBtn");

const tagsPanel = document.getElementById("tagsPanel");
const tagsList = document.getElementById("tagsList");

let goalId = null;
let goal = null;

if (window.__careerGoalDetailPageLoaded) {
  // Prevent duplicate initialization if compatibility bridge loads this script.
  console.debug("goal-detail page script already initialized");
} else {
  window.__careerGoalDetailPageLoaded = true;
}

const hasRequiredMarkup = Boolean(
  logoutBtn &&
  notesInput &&
  saveNotesBtn &&
  deleteGoalBtn &&
  createTaskForm &&
  taskList &&
  taskSummary &&
  statusBadge &&
  priorityBadge
);

if (!hasRequiredMarkup) {
  console.warn("Goal detail page markup is incomplete. Skipping page bootstrap.");
} else if (AppApi.requireAuth()) {
  initGoalDetailPage();
}

if (logoutBtn) {
  logoutBtn.addEventListener("click", () => {
    AppApi.clearToken();
    window.location.href = "./login.html";
  });
}

if (saveNotesBtn) {
  saveNotesBtn.addEventListener("click", saveNotes);
}
if (deleteGoalBtn) {
  deleteGoalBtn.addEventListener("click", deleteGoal);
}
if (createTaskForm) {
  createTaskForm.addEventListener("submit", createTask);
}
if (saveTargetDateBtn) {
  saveTargetDateBtn.addEventListener("click", updateGoalDeadline);
}
if (markGoalCompletedBtn) {
  markGoalCompletedBtn.addEventListener("click", () => updateGoalStatus("COMPLETED"));
}
if (markGoalPlannedBtn) {
  markGoalPlannedBtn.addEventListener("click", () => updateGoalStatus("PLANNED"));
}

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
  } catch (error) {
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
  if (goalTargetDateInput) {
    goalTargetDateInput.value = AppUi.toInputDate(data.targetDate);
  }

  notesInput.value = data.notes || "";

  if (Array.isArray(data.tags) && data.tags.length) {
    tagsPanel.classList.remove("hidden");
    tagsList.innerHTML = data.tags
      .map((tag) => `<span class="chip">${AppUi.escapeHtml(tag)}</span>`)
      .join("");
  } else {
    tagsPanel.classList.add("hidden");
    tagsList.innerHTML = "";
  }

  renderTasks(data.tasks || []);
  AppUi.applyReveal(document);
}

function renderTasks(tasks) {
  const completedCount = tasks.filter((task) => task.completed).length;
  taskSummary.textContent = `${completedCount} / ${tasks.length} milestones completed`;
  taskSummary.className = `chip ${completedCount === tasks.length && tasks.length > 0 ? "status-completed" : "status-in-progress"}`;

  if (!tasks.length) {
    taskList.innerHTML = `
      <div class="empty-state" style="padding: 1rem;">
        <h4>No tasks available</h4>
        <p>Add milestone tasks to track objective progress.</p>
      </div>`;
    return;
  }

  taskList.innerHTML = tasks
    .map((task) => {
      const dueDate = task.dueDate ? AppUi.formatDate(task.dueDate) : "No due date";
      const dueInputValue = AppUi.toInputDate(task.dueDate);
      const isCompleted = Boolean(task.completed);
      const completedClass = isCompleted ? "task-item-completed" : "";
      const completionLabel = isCompleted ? "Completed" : "Complete";
      const completionNextValue = isCompleted ? "false" : "true";
      const completionButtonClass = `btn btn-small task-complete-btn ${isCompleted ? "task-complete-done" : "btn-success"}`;

      return `
        <article class="task-item ${completedClass}">
          <div class="task-main">
            <h4>${AppUi.escapeHtml(task.title || "Task")}</h4>
            <p class="text-muted">${AppUi.escapeHtml(task.details || "No details")}</p>
            <div class="task-meta-row">
              <span class="chip">Due: ${AppUi.escapeHtml(dueDate)}</span>
              <span class="chip">Contribution: ${Number(task.weight || 0)} pts</span>
            </div>
            <div class="task-schedule-controls">
              <input class="task-date-input" type="date" data-task-date-id="${task.id}" value="${AppUi.escapeAttr(dueInputValue)}" />
              <button class="btn btn-ghost btn-small" data-task-save-date-id="${task.id}" type="button">Update Date</button>
            </div>
          </div>
          <div class="task-item-actions">
            <button
              class="${completionButtonClass}"
              data-task-complete-id="${task.id}"
              data-task-complete-value="${completionNextValue}"
              type="button"
            >${completionLabel}</button>
            <button class="btn btn-danger btn-small" data-task-delete-id="${task.id}" type="button">Remove</button>
          </div>
        </article>`;
    })
    .join("");

  taskList.querySelectorAll("button[data-task-complete-id]").forEach((button) => {
    button.addEventListener("click", async () => {
      const taskId = button.getAttribute("data-task-complete-id");
      if (!taskId) {
        return;
      }

      const nextValue = button.getAttribute("data-task-complete-value") === "true";
      await toggleTask(taskId, nextValue);
    });
  });

  taskList.querySelectorAll("button[data-task-delete-id]").forEach((button) => {
    button.addEventListener("click", async () => {
      const taskId = button.getAttribute("data-task-delete-id");
      if (!taskId) {
        return;
      }

      if (!window.confirm("Delete this task? Goal must retain at least 5 milestone tasks.")) {
        return;
      }

      await deleteTask(taskId);
    });
  });

  taskList.querySelectorAll("button[data-task-save-date-id]").forEach((button) => {
    button.addEventListener("click", async () => {
      const taskId = button.getAttribute("data-task-save-date-id");
      if (!taskId) {
        return;
      }

      const input = taskList.querySelector(`input[data-task-date-id="${taskId}"]`);
      if (!input) {
        return;
      }

      await updateTaskDueDate(taskId, input.value || null);
    });
  });

  AppUi.applyReveal(taskList);
}

async function toggleTask(taskId, completed) {
  try {
    goal = await AppApi.updateGoalTask(goalId, taskId, { completed });
    renderGoal(goal);
    AppUi.showToast(completed ? "Task marked completed" : "Task moved back to pending");
  } catch (error) {
    AppUi.showToast(error.message, "error");
    await loadGoal();
  }
}

async function updateTaskDueDate(taskId, dueDate) {
  try {
    goal = await AppApi.updateGoalTask(goalId, taskId, { dueDate });
    renderGoal(goal);
    AppUi.showToast("Task deadline updated");
  } catch (error) {
    AppUi.showToast(error.message, "error");
  }
}

async function createTask(event) {
  event.preventDefault();

  const payload = {
    title: taskTitleInput.value.trim(),
    details: taskDetailsInput.value.trim() || null,
    dueDate: taskDueDateInput.value || null,
    weight: Number(taskWeightInput.value || 15)
  };

  AppUi.setLoading(createTaskBtn, true, "Adding...");

  try {
    goal = await AppApi.createGoalTask(goalId, payload);
    renderGoal(goal);
    createTaskForm.reset();
    taskWeightInput.value = "15";
    AppUi.showToast("Task added");
  } catch (error) {
    AppUi.showToast(error.message, "error");
  } finally {
    AppUi.setLoading(createTaskBtn, false);
  }
}

async function deleteTask(taskId) {
  try {
    goal = await AppApi.deleteGoalTask(goalId, taskId);
    renderGoal(goal);
    AppUi.showToast("Task deleted");
  } catch (error) {
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
    AppUi.showToast(error.message, "error");
  } finally {
    AppUi.setLoading(saveNotesBtn, false);
  }
}

async function updateGoalDeadline() {
  if (!saveTargetDateBtn || !goalTargetDateInput) {
    return;
  }

  AppUi.setLoading(saveTargetDateBtn, true, "Updating...");
  try {
    goal = await AppApi.updateGoal(goalId, {
      targetDate: goalTargetDateInput.value || null
    });
    renderGoal(goal);
    AppUi.showToast("Goal deadline updated");
  } catch (error) {
    AppUi.showToast(error.message, "error");
  } finally {
    AppUi.setLoading(saveTargetDateBtn, false);
  }
}

async function updateGoalStatus(status) {
  try {
    goal = await AppApi.updateGoal(goalId, { status });
    renderGoal(goal);
    AppUi.showToast(status === "COMPLETED" ? "Goal marked completed" : "Goal reset to planned");
  } catch (error) {
    AppUi.showToast(error.message, "error");
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
