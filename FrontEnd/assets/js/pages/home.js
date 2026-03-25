const logoutBtn = document.getElementById("logoutBtn");
const homeWelcomeText = document.getElementById("homeWelcomeText");
const navUserInitial = document.getElementById("navUserInitial");
const navUserName = document.getElementById("navUserName");
const navUserEmail = document.getElementById("navUserEmail");
const priorityReminderCount = document.getElementById("priorityReminderCount");
const priorityReminderList = document.getElementById("priorityReminderList");
const homeNextActionPanel = document.getElementById("homeNextActionPanel");

if (AppApi.requireAuth()) {
  initHomePage();
}

logoutBtn?.addEventListener("click", () => {
  AppApi.clearToken();
  window.location.href = "./login.html";
});

async function initHomePage() {
  try {
    const [profile, stats, goals] = await Promise.all([
      AppApi.getMe(),
      AppApi.getGoalStats(),
      AppApi.listGoals()
    ]);

    if (profile.onboardingCompleted === false) {
      window.location.href = "./technical-readiness.html";
      return;
    }

    const firstName = (profile.fullName || "User").split(" ")[0];
    homeWelcomeText.textContent = `Welcome back, ${firstName}`;
    navUserInitial.textContent = (profile.fullName || "U").charAt(0).toUpperCase();
    navUserName.textContent = profile.fullName || "User";
    navUserEmail.textContent = profile.email || "-";

    setStat("homeStatTotal", Number(stats.total || 0));
    setStat("homeStatWeekly", `${Number(stats.weeklyExecutionScore || 0)}%`);
    setStat("homeStatDueSoon", Number(stats.dueSoonTaskCount || 0));
    setStat("homeStatOverdue", Number(stats.overdueTaskCount || 0));

    renderPriorityReminders(goals || []);
    renderNextBestAction(stats.nextBestAction || null);
    AppUi.applyReveal(document);
  } catch (error) {
    AppUi.showToast(error.message, "error");
  }
}

function setStat(id, value) {
  const element = document.getElementById(id);
  if (element) {
    element.textContent = String(value);
  }
}

function renderPriorityReminders(goals) {
  if (!priorityReminderList || !Array.isArray(goals)) {
    return;
  }

  const priorityWeight = {
    LOW: 1,
    MEDIUM: 2,
    HIGH: 3,
    URGENT: 4
  };

  const today = new Date();
  today.setHours(0, 0, 0, 0);

  const reminders = [];
  goals.forEach((goal) => {
    const goalPriority = String(goal.priority || "MEDIUM").toUpperCase();
    const tasks = Array.isArray(goal.tasks) ? goal.tasks : [];

    tasks.forEach((task) => {
      if (!task || task.completed || !task.dueDate) {
        return;
      }

      const due = new Date(`${task.dueDate}T00:00:00`);
      if (Number.isNaN(due.getTime())) {
        return;
      }

      const daysLeft = Math.floor((due.getTime() - today.getTime()) / 86400000);
      const timeUrgency = daysLeft < 0 ? 100 + Math.abs(daysLeft) : Math.max(0, 35 - daysLeft * 4);
      const urgency = timeUrgency + (priorityWeight[goalPriority] || 2) * 18;

      reminders.push({
        goalId: goal.id,
        goalTitle: goal.title || "Goal",
        taskTitle: task.title || "Task",
        dueDate: task.dueDate,
        daysLeft,
        priority: goalPriority,
        urgency
      });
    });
  });

  reminders.sort((a, b) => b.urgency - a.urgency || new Date(a.dueDate).getTime() - new Date(b.dueDate).getTime());
  const topReminders = reminders.slice(0, 6);

  if (priorityReminderCount) {
    priorityReminderCount.textContent = String(topReminders.length);
    priorityReminderCount.className = `chip ${topReminders.some((item) => item.daysLeft < 0) ? "priority-urgent" : "status-in-progress"}`;
  }

  if (!topReminders.length) {
    priorityReminderList.innerHTML = "<p class='text-muted'>No priority reminders right now. You are on track.</p>";
    return;
  }

  priorityReminderList.innerHTML = topReminders
    .map((item) => {
      const dueText = item.daysLeft < 0
        ? `${Math.abs(item.daysLeft)} day(s) overdue`
        : item.daysLeft === 0
          ? "Due today"
          : `Due in ${item.daysLeft} day(s)`;
      const dueClass = item.daysLeft < 0 ? "priority-urgent" : item.daysLeft <= 2 ? "status-in-progress" : "status-planned";

      return `
        <a class="goal-item" href="./goal-detail.html?id=${item.goalId}">
          <div class="goal-title-row">
            <h4>${AppUi.escapeHtml(item.taskTitle)}</h4>
            <span class="chip ${priorityClass(item.priority)}">${AppUi.escapeHtml(AppUi.humanize(item.priority))}</span>
          </div>
          <p class="text-muted">${AppUi.escapeHtml(item.goalTitle)}</p>
          <div class="goal-meta">
            <span>Due: ${AppUi.escapeHtml(AppUi.formatDate(item.dueDate))}</span>
            <span class="chip ${dueClass}">${AppUi.escapeHtml(dueText)}</span>
          </div>
        </a>`;
    })
    .join("");

  AppUi.applyReveal(priorityReminderList);
}

function renderNextBestAction(nextBestAction) {
  if (!homeNextActionPanel) {
    return;
  }

  if (!nextBestAction) {
    homeNextActionPanel.innerHTML = `
      <div class="empty-state" style="padding: 1rem;">
        <h4>You are caught up</h4>
        <p>No pending blueprint action right now.</p>
      </div>`;
    return;
  }

  homeNextActionPanel.innerHTML = `
    <a class="goal-item" href="./goal-detail.html?id=${nextBestAction.goalId}">
      <div class="goal-title-row">
        <h4>${AppUi.escapeHtml(nextBestAction.title || "Next Action")}</h4>
        <span class="chip status-in-progress">${AppUi.escapeHtml(nextBestAction.phase || "Blueprint")}</span>
      </div>
      <p class="text-muted">Complete this next to stay aligned with your plan.</p>
    </a>`;
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
