const logoutBtn = document.getElementById("logoutBtn");
const adminInitial = document.getElementById("adminInitial");
const adminName = document.getElementById("adminName");
const adminEmail = document.getElementById("adminEmail");
const adminUserSearch = document.getElementById("adminUserSearch");
const adminUserList = document.getElementById("adminUserList");
const questionTrackSelect = document.getElementById("questionTrackSelect");
const questionCareerTrack = document.getElementById("questionCareerTrack");
const questionForm = document.getElementById("questionForm");
const newQuestionBtn = document.getElementById("newQuestionBtn");
const resetQuestionBtn = document.getElementById("resetQuestionBtn");
const saveQuestionBtn = document.getElementById("saveQuestionBtn");
const adminQuestionList = document.getElementById("adminQuestionList");

let adminProfile = null;
let allUsers = [];
let technicalTracks = [];
let loadedQuestions = [];

if (!AppApi.getToken()) {
  window.location.href = "./login.html?portal=admin";
} else {
  initAdminPage();
}

logoutBtn?.addEventListener("click", () => {
  AppApi.clearToken();
  window.location.href = "./login.html?portal=admin";
});

adminUserSearch?.addEventListener("input", renderUserCards);
questionTrackSelect?.addEventListener("change", async () => {
  questionCareerTrack.value = questionTrackSelect.value;
  resetQuestionForm();
  await loadQuestionBank();
});
newQuestionBtn?.addEventListener("click", resetQuestionForm);
resetQuestionBtn?.addEventListener("click", resetQuestionForm);
questionForm?.addEventListener("submit", saveQuestion);

async function initAdminPage() {
  try {
    const [profile, users, tracks] = await Promise.all([
      AppApi.getMe(),
      AppApi.listAdminUsers(),
      AppApi.listTechnicalAssessmentTracks()
    ]);

    if (!(window.AppSession && typeof window.AppSession.isAdminProfile === "function" && window.AppSession.isAdminProfile(profile))) {
      window.location.href = "./home.html";
      return;
    }

    adminProfile = profile;
    allUsers = Array.isArray(users) ? users : [];
    technicalTracks = Array.isArray(tracks) ? tracks : [];

    adminInitial.textContent = (profile.fullName || "A").charAt(0).toUpperCase();
    adminName.textContent = profile.fullName || "Admin";
    adminEmail.textContent = profile.email || "-";

    setSelectOptions(questionTrackSelect, technicalTracks);
    setSelectOptions(questionCareerTrack, technicalTracks);
    questionTrackSelect.value = technicalTracks[0] || "";
    questionCareerTrack.value = technicalTracks[0] || "";

    renderAdminStats();
    renderUserCards();
    resetQuestionForm();
    await loadQuestionBank();
    AppUi.applyReveal(document);
  } catch (error) {
    AppUi.showToast(error.message, "error");
    adminUserList.innerHTML = `<p class='text-muted'>${AppUi.escapeHtml(error.message)}</p>`;
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

function renderAdminStats() {
  const totalUsers = allUsers.length;
  const adminCount = allUsers.filter((user) => String(user.role || "").toUpperCase() === "ADMIN").length;
  const learnerCount = totalUsers - adminCount;
  const totalGoals = allUsers.reduce((sum, user) => sum + Number(user.goalCount || 0), 0);
  const totalTasks = allUsers.reduce((sum, user) => sum + Number(user.taskCount || 0), 0);
  const completedTasks = allUsers.reduce((sum, user) => sum + Number(user.completedTaskCount || 0), 0);

  setText("adminTotalUsers", totalUsers);
  setText("adminLearnerCount", learnerCount);
  setText("adminAdminCount", adminCount);
  setText("adminGoalCount", totalGoals);
  setText("adminTaskCount", totalTasks);
  setText("adminCompletedTaskCount", completedTasks);
}

function renderUserCards() {
  if (!adminUserList) {
    return;
  }

  const query = String(adminUserSearch && adminUserSearch.value ? adminUserSearch.value : "").trim().toLowerCase();
  const filteredUsers = allUsers.filter((user) => {
    if (!query) {
      return true;
    }
    return [user.fullName, user.email, user.role, user.careerTrack]
      .filter(Boolean)
      .some((value) => String(value).toLowerCase().includes(query));
  });

  if (!filteredUsers.length) {
    adminUserList.innerHTML = "<p class='text-muted'>No users match the current filter.</p>";
    return;
  }

  adminUserList.innerHTML = filteredUsers
    .map((user) => {
      const completionRate = Number(user.taskCount || 0) > 0
        ? Math.round((Number(user.completedTaskCount || 0) * 100) / Number(user.taskCount || 1))
        : 0;
      return `
        <article class="goal-item admin-user-card">
          <div class="goal-title-row">
            <div>
              <h4>${AppUi.escapeHtml(user.fullName || "User")}</h4>
              <p class="text-muted">${AppUi.escapeHtml(user.email || "-")}</p>
            </div>
            <span class="chip ${String(user.role || "").toUpperCase() === "ADMIN" ? "status-completed" : "status-in-progress"}">${AppUi.escapeHtml(AppUi.humanize(user.role || "USER"))}</span>
          </div>
          <div class="admin-user-meta-grid">
            <span>Track: <strong>${AppUi.escapeHtml(AppUi.humanize(user.careerTrack || "-"))}</strong></span>
            <span>Goals: <strong>${Number(user.goalCount || 0)}</strong></span>
            <span>Tasks: <strong>${Number(user.taskCount || 0)}</strong></span>
            <span>Task Completion: <strong>${completionRate}%</strong></span>
          </div>
          <div class="toolbar-actions" style="margin-top:0.9rem;">
            <a class="btn btn-primary btn-small" href="./admin-user.html?id=${user.id}">Open Student</a>
            <span class="chip ${user.onboardingCompleted ? "status-completed" : "status-planned"}">${user.onboardingCompleted ? "Setup complete" : "Setup pending"}</span>
          </div>
        </article>`;
    })
    .join("");

  AppUi.applyReveal(adminUserList);
}

async function loadQuestionBank() {
  const careerTrack = questionTrackSelect.value;
  if (!careerTrack) {
    adminQuestionList.innerHTML = "<p class='text-muted'>Select a technical role first.</p>";
    return;
  }

  adminQuestionList.innerHTML = "<p class='text-muted'>Loading questions...</p>";

  try {
    loadedQuestions = await AppApi.listAdminTechnicalAssessmentQuestions(careerTrack);
    renderQuestionList();
  } catch (error) {
    adminQuestionList.innerHTML = `<p class='text-muted'>${AppUi.escapeHtml(error.message)}</p>`;
  }
}

function renderQuestionList() {
  if (!loadedQuestions.length) {
    adminQuestionList.innerHTML = "<p class='text-muted'>No questions configured for this role yet.</p>";
    return;
  }

  adminQuestionList.innerHTML = loadedQuestions
    .map((question) => {
      const optionsMarkup = (question.options || [])
        .map((option) => `<li>${AppUi.escapeHtml(option.optionText || "Option")}${option.correct ? " <strong>(Correct)</strong>" : ""}</li>`)
        .join("");

      return `
        <article class="section-card admin-question-card" data-question-id="${question.id}">
          <div class="section-header">
            <div>
              <h4>#${Number(question.displayOrder || 0)} · ${AppUi.escapeHtml(question.skillArea || "Skill")}</h4>
              <p class="text-muted">${AppUi.escapeHtml(AppUi.humanize(question.careerTrack || ""))} · ${AppUi.escapeHtml(AppUi.humanize(question.difficulty || "FOUNDATION"))}</p>
            </div>
            <div class="toolbar-actions">
              <span class="chip ${question.active ? "status-completed" : "status-planned"}">${question.active ? "Active" : "Hidden"}</span>
              <button class="btn btn-ghost btn-small" type="button" data-action="edit-question" data-question-id="${question.id}">Edit</button>
              <button class="btn btn-muted btn-small" type="button" data-action="delete-question" data-question-id="${question.id}">Delete</button>
            </div>
          </div>
          <p>${AppUi.escapeHtml(question.questionText || "Question")}</p>
          ${question.explanation ? `<p class="text-muted" style="margin-top:0.55rem;">${AppUi.escapeHtml(question.explanation)}</p>` : ""}
          <ol class="admin-option-list">${optionsMarkup}</ol>
        </article>`;
    })
    .join("");

  adminQuestionList.querySelectorAll("[data-action='edit-question']").forEach((button) => {
    button.addEventListener("click", () => {
      const questionId = Number(button.getAttribute("data-question-id"));
      const question = loadedQuestions.find((item) => item.id === questionId);
      if (question) {
        populateQuestionForm(question);
        window.location.hash = "questionBankSection";
      }
    });
  });

  adminQuestionList.querySelectorAll("[data-action='delete-question']").forEach((button) => {
    button.addEventListener("click", async () => {
      const questionId = Number(button.getAttribute("data-question-id"));
      if (Number.isNaN(questionId)) {
        return;
      }
      await deleteQuestion(questionId);
    });
  });

  AppUi.applyReveal(adminQuestionList);
}

function populateQuestionForm(question) {
  document.getElementById("questionId").value = String(question.id || "");
  questionCareerTrack.value = question.careerTrack || questionTrackSelect.value;
  questionDifficulty.value = question.difficulty || "FOUNDATION";
  document.getElementById("questionDisplayOrder").value = String(question.displayOrder || "");
  document.getElementById("questionSkillArea").value = question.skillArea || "";
  document.getElementById("questionText").value = question.questionText || "";
  document.getElementById("questionExplanation").value = question.explanation || "";
  document.getElementById("questionActive").checked = question.active !== false;

  const options = Array.isArray(question.options) ? question.options : [];
  for (let index = 0; index < 4; index += 1) {
    const option = options[index] || {};
    document.getElementById(`option${index + 1}`).value = option.optionText || "";
    document.getElementById(`correct${index + 1}`).checked = Boolean(option.correct);
  }

  saveQuestionBtn.textContent = "Update Question";
}

function resetQuestionForm() {
  document.getElementById("questionId").value = "";
  questionCareerTrack.value = questionTrackSelect.value || technicalTracks[0] || "";
  questionDifficulty.value = "FOUNDATION";
  document.getElementById("questionDisplayOrder").value = "";
  document.getElementById("questionSkillArea").value = "";
  document.getElementById("questionText").value = "";
  document.getElementById("questionExplanation").value = "";
  document.getElementById("questionActive").checked = true;

  for (let index = 0; index < 4; index += 1) {
    document.getElementById(`option${index + 1}`).value = "";
    document.getElementById(`correct${index + 1}`).checked = index === 0;
  }

  saveQuestionBtn.textContent = "Save Question";
}

async function saveQuestion(event) {
  event.preventDefault();

  const questionId = document.getElementById("questionId").value.trim();
  const correctOptionIndex = Number((document.querySelector("input[name='correctOption']:checked") || {}).value || 0);
  const options = [1, 2, 3, 4].map((index) => ({
    optionText: document.getElementById(`option${index}`).value.trim(),
    correct: correctOptionIndex === index - 1
  }));

  const payload = {
    careerTrack: questionCareerTrack.value,
    difficulty: document.getElementById("questionDifficulty").value,
    displayOrder: Number(document.getElementById("questionDisplayOrder").value),
    skillArea: document.getElementById("questionSkillArea").value.trim(),
    questionText: document.getElementById("questionText").value.trim(),
    explanation: document.getElementById("questionExplanation").value.trim() || null,
    active: document.getElementById("questionActive").checked,
    options
  };

  AppUi.setLoading(saveQuestionBtn, true, questionId ? "Updating..." : "Saving...");

  try {
    if (questionId) {
      await AppApi.updateAdminTechnicalAssessmentQuestion(Number(questionId), payload);
      AppUi.showToast("Question updated");
    } else {
      await AppApi.createAdminTechnicalAssessmentQuestion(payload);
      AppUi.showToast("Question created");
    }

    questionTrackSelect.value = payload.careerTrack;
    resetQuestionForm();
    await loadQuestionBank();
  } catch (error) {
    AppUi.showToast(error.message, "error");
  } finally {
    AppUi.setLoading(saveQuestionBtn, false);
  }
}

async function deleteQuestion(questionId) {
  const confirmed = window.confirm("Delete this question from the readiness check?");
  if (!confirmed) {
    return;
  }

  try {
    await AppApi.deleteAdminTechnicalAssessmentQuestion(questionId);
    AppUi.showToast("Question deleted");
    if (String(questionId) === document.getElementById("questionId").value) {
      resetQuestionForm();
    }
    await loadQuestionBank();
  } catch (error) {
    AppUi.showToast(error.message, "error");
  }
}

function setText(id, value) {
  const element = document.getElementById(id);
  if (element) {
    element.textContent = String(value);
  }
}
