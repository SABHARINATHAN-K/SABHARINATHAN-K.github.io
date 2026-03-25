const logoutBtn = document.getElementById("logoutBtn");
const statusPill = document.getElementById("statusPill");
const chooserSection = document.getElementById("chooserSection");
const benchmarkSection = document.getElementById("benchmarkSection");
const resultSection = document.getElementById("resultSection");
const nextFlowSection = document.getElementById("nextFlowSection");

const manualTrackSelect = document.getElementById("manualTrackSelect");
const technicalRoleSelect = document.getElementById("technicalRoleSelect");
const confirmTrackSelect = document.getElementById("confirmTrackSelect");

const manualConfirmBtn = document.getElementById("manualConfirmBtn");
const manualGenerateBtn = document.getElementById("manualGenerateBtn");
const loadBenchmarkBtn = document.getElementById("loadBenchmarkBtn");
const backToChooserBtn = document.getElementById("backToChooserBtn");
const submitBenchmarkBtn = document.getElementById("submitBenchmarkBtn");
const confirmTrackBtn = document.getElementById("confirmTrackBtn");
const generatePlanBtn = document.getElementById("generatePlanBtn");

const benchmarkRoleLabel = document.getElementById("benchmarkRoleLabel");
const benchmarkHint = document.getElementById("benchmarkHint");
const questionCountText = document.getElementById("questionCountText");
const benchmarkQuestionList = document.getElementById("benchmarkQuestionList");

const benchmarkRoleText = document.getElementById("benchmarkRoleText");
const benchmarkLevelText = document.getElementById("benchmarkLevelText");
const benchmarkScoreText = document.getElementById("benchmarkScoreText");
const benchmarkImprovementText = document.getElementById("benchmarkImprovementText");
const benchmarkSummaryText = document.getElementById("benchmarkSummaryText");
const levelChip = document.getElementById("levelChip");
const skillAreaList = document.getElementById("skillAreaList");
const nextFlowSummary = document.getElementById("nextFlowSummary");

let technicalTracks = [];
let currentQuestions = [];
let answerMap = new Map();
let latestResult = null;
let confirmedTrack = "";

if (AppApi.requireAuth()) {
  initTechnicalReadiness();
}

logoutBtn?.addEventListener("click", () => {
  AppApi.clearToken();
  window.location.href = "./login.html?portal=user";
});

loadBenchmarkBtn?.addEventListener("click", loadReadinessCheck);
backToChooserBtn?.addEventListener("click", showChooser);
submitBenchmarkBtn?.addEventListener("click", submitReadinessCheck);
manualConfirmBtn?.addEventListener("click", handleManualTrackConfirm);
manualGenerateBtn?.addEventListener("click", handleManualGenerate);
confirmTrackBtn?.addEventListener("click", confirmCareerTrackFromResult);
generatePlanBtn?.addEventListener("click", generatePlanAfterResult);
manualTrackSelect?.addEventListener("change", syncConfirmedTrackState);
technicalRoleSelect?.addEventListener("change", syncConfirmedTrackState);
confirmTrackSelect?.addEventListener("change", syncConfirmedTrackState);

async function initTechnicalReadiness() {
  try {
    const [tracks, profile, progress] = await Promise.all([
      AppApi.listTechnicalAssessmentTracks(),
      AppApi.getMe(),
      AppApi.getTechnicalAssessmentProgress().catch(() => null)
    ]);

    if (window.AppSession && typeof window.AppSession.isAdminProfile === "function" && window.AppSession.isAdminProfile(profile)) {
      window.location.href = "./admin.html";
      return;
    }

    technicalTracks = Array.isArray(tracks) ? tracks : [];
    setTrackOptions(technicalTracks);

    confirmedTrack = profile.careerTrack || "";

    const preferredTrack = progress && technicalTracks.includes(progress.careerTrack)
      ? progress.careerTrack
      : profile.careerTrack;

    const defaultTrack = technicalTracks.includes(preferredTrack)
      ? preferredTrack
      : technicalTracks[0] || "";

    manualTrackSelect.value = defaultTrack;
    technicalRoleSelect.value = defaultTrack;
    confirmTrackSelect.value = defaultTrack;

    statusPill.textContent = progress && progress.reassessmentDue
      ? "Reassessment Due"
      : profile.onboardingCompleted
        ? "Ready to Recheck"
        : "Setup Required";
  } catch (error) {
    AppUi.showToast(error.message, "error");
  }
}

function setTrackOptions(tracks) {
  const options = tracks
    .map((track) => `<option value="${AppUi.escapeAttr(track)}">${AppUi.escapeHtml(AppUi.humanize(track))}</option>`)
    .join("");

  manualTrackSelect.innerHTML = options;
  technicalRoleSelect.innerHTML = options;
  confirmTrackSelect.innerHTML = options;
}

async function loadReadinessCheck() {
  const selectedTrack = technicalRoleSelect.value;
  if (!selectedTrack) {
    AppUi.showToast("Select a technical role first.", "error");
    return;
  }

  try {
    AppUi.setLoading(loadBenchmarkBtn, true, "Loading...");
    currentQuestions = await AppApi.listTechnicalAssessmentQuestions(selectedTrack);
    answerMap = new Map();
    latestResult = null;

    benchmarkRoleLabel.textContent = `${AppUi.humanize(selectedTrack)} Readiness Check`;
    benchmarkHint.textContent = "Answer every domain-specific question to measure your current technical readiness.";
    questionCountText.textContent = `${currentQuestions.length} Questions`;
    confirmTrackSelect.value = selectedTrack;
    statusPill.textContent = "In Progress";

    renderQuestions();
    chooserSection.classList.add("hidden");
    resultSection.classList.add("hidden");
    nextFlowSection.classList.add("hidden");
    benchmarkSection.classList.remove("hidden");
  } catch (error) {
    AppUi.showToast(error.message, "error");
  } finally {
    AppUi.setLoading(loadBenchmarkBtn, false);
  }
}

function renderQuestions() {
  if (!currentQuestions.length) {
    benchmarkQuestionList.innerHTML = `
      <div class="empty-state">
        <h4>No readiness questions configured</h4>
        <p>Try another role or ask the admin to configure questions for this track.</p>
      </div>`;
    return;
  }

  benchmarkQuestionList.innerHTML = currentQuestions
    .map((question) => {
      const optionsMarkup = (question.options || [])
        .map((option) => {
          const checked = answerMap.get(question.id) === option.id ? "checked" : "";
          return `
            <label class="option-item">
              <input type="radio" name="q-${question.id}" value="${option.id}" data-question-id="${question.id}" ${checked} />
              <span>${AppUi.escapeHtml(option.optionText || "Option")}</span>
            </label>`;
        })
        .join("");

      return `
        <article class="question-card">
          <div class="goal-title-row" style="margin-bottom:0.55rem;">
            <h4>${AppUi.escapeHtml(question.questionText || "Question")}</h4>
            <span class="chip">${AppUi.escapeHtml(AppUi.humanize(question.difficulty || "FOUNDATION"))}</span>
          </div>
          <p class="text-muted" style="margin-bottom:0.65rem;">Skill Area: ${AppUi.escapeHtml(question.skillArea || "Technical")}</p>
          <div class="option-list">${optionsMarkup}</div>
        </article>`;
    })
    .join("");

  benchmarkQuestionList.querySelectorAll("input[type='radio'][data-question-id]").forEach((input) => {
    input.addEventListener("change", () => {
      const questionId = Number(input.getAttribute("data-question-id"));
      const optionId = Number(input.value);
      if (!Number.isNaN(questionId) && !Number.isNaN(optionId)) {
        answerMap.set(questionId, optionId);
      }
    });
  });

  AppUi.applyReveal(benchmarkQuestionList);
}

function showChooser() {
  benchmarkSection.classList.add("hidden");
  chooserSection.classList.remove("hidden");
  statusPill.textContent = "Ready";
}

async function submitReadinessCheck() {
  if (!currentQuestions.length) {
    AppUi.showToast("Load a readiness check first.", "error");
    return;
  }

  if (answerMap.size !== currentQuestions.length) {
    AppUi.showToast("Answer every question before submitting.", "error");
    return;
  }

  try {
    AppUi.setLoading(submitBenchmarkBtn, true, "Scoring...");
    latestResult = await AppApi.submitTechnicalAssessment({
      careerTrack: technicalRoleSelect.value,
      answers: currentQuestions.map((question) => ({
        questionId: question.id,
        optionId: answerMap.get(question.id)
      }))
    });

    renderResult(latestResult);
    benchmarkSection.classList.add("hidden");
    resultSection.classList.remove("hidden");
    nextFlowSection.classList.add("hidden");
    statusPill.textContent = "Completed";
  } catch (error) {
    AppUi.showToast(error.message, "error");
  } finally {
    AppUi.setLoading(submitBenchmarkBtn, false);
  }
}

function renderResult(result) {
  benchmarkRoleText.textContent = AppUi.humanize(result.careerTrack || "-");
  benchmarkLevelText.textContent = AppUi.humanize(result.proficiencyLevel || "BEGINNER");
  benchmarkScoreText.textContent = `${Number(result.percentageScore || 0)}% (${Number(result.totalScore || 0)}/${Number(result.maxScore || 0)})`;
  benchmarkImprovementText.textContent = formatImprovement(result.improvementPercentagePoints);
  benchmarkSummaryText.textContent = result.performanceSummary || "No summary available.";
  levelChip.textContent = AppUi.humanize(result.proficiencyLevel || "BEGINNER");
  confirmTrackSelect.value = result.careerTrack || technicalRoleSelect.value;
  renderSkillAreas(result.skillAreas || []);
  syncConfirmedTrackState();
}

function renderSkillAreas(skillAreas) {
  if (!skillAreas.length) {
    skillAreaList.innerHTML = "<p class='text-muted'>No skill breakdown available.</p>";
    return;
  }

  skillAreaList.innerHTML = skillAreas
    .map((skill) => {
      const percentage = Number(skill.percentageScore || 0);
      return `
        <div class="bar-row">
          <div class="bar-head">
            <span>${AppUi.escapeHtml(skill.skillArea || "Skill")}</span>
            <strong>${percentage}%</strong>
          </div>
          <div class="bar-track"><div class="bar-fill" style="width:${Math.max(6, percentage)}%"></div></div>
        </div>`;
    })
    .join("");
  AppUi.applyReveal(skillAreaList);
}

function formatImprovement(improvement) {
  if (improvement === null || improvement === undefined) {
    return "This is your baseline readiness result for this role.";
  }
  if (improvement > 0) {
    return `Improved by ${improvement} percentage points from your previous attempt.`;
  }
  if (improvement < 0) {
    return `Dropped by ${Math.abs(improvement)} percentage points compared with your previous attempt.`;
  }
  return "Your score is unchanged from your previous readiness check.";
}

async function handleManualTrackConfirm() {
  if (!manualTrackSelect.value) {
    AppUi.showToast("Select a technical role first.", "error");
    return;
  }

  try {
    AppUi.setLoading(manualConfirmBtn, true, "Confirming...");
    const response = await AppApi.confirmCareerTrack({ careerTrack: manualTrackSelect.value });
    confirmedTrack = response.careerTrack || manualTrackSelect.value;
    manualTrackSelect.value = confirmedTrack;
    technicalRoleSelect.value = confirmedTrack;
    confirmTrackSelect.value = confirmedTrack;
    statusPill.textContent = "Role Confirmed";
    AppUi.showToast(`Role confirmed: ${AppUi.humanize(response.careerTrack)}`, "success");
  } catch (error) {
    AppUi.showToast(error.message, "error");
  } finally {
    AppUi.setLoading(manualConfirmBtn, false);
  }
}

async function handleManualGenerate() {
  if (!manualTrackSelect.value) {
    AppUi.showToast("Select a technical role first.", "error");
    return;
  }

  if (!isCurrentTrackConfirmed(manualTrackSelect.value)) {
    await handleManualTrackConfirm();
    if (!isCurrentTrackConfirmed(manualTrackSelect.value)) {
      return;
    }
  }

  try {
    AppUi.setLoading(manualGenerateBtn, true, "Generating...");
    const response = await AppApi.generateRoadmap();
    showNextFlow(`Plan generated for ${AppUi.humanize(response.careerTrack)}. ${response.goalsCreated} roadmap goal(s) were created.`);
  } catch (error) {
    AppUi.showToast(error.message, "error");
  } finally {
    AppUi.setLoading(manualGenerateBtn, false);
  }
}

async function confirmCareerTrackFromResult() {
  if (!confirmTrackSelect.value) {
    AppUi.showToast("Select a final technical role first.", "error");
    return;
  }

  try {
    AppUi.setLoading(confirmTrackBtn, true, "Saving...");
    const response = await AppApi.confirmCareerTrack({ careerTrack: confirmTrackSelect.value });
    confirmedTrack = response.careerTrack || confirmTrackSelect.value;
    manualTrackSelect.value = confirmedTrack;
    technicalRoleSelect.value = confirmedTrack;
    confirmTrackSelect.value = confirmedTrack;
    statusPill.textContent = "Role Confirmed";
    AppUi.showToast(`Role confirmed: ${AppUi.humanize(response.careerTrack)}`, "success");
  } catch (error) {
    AppUi.showToast(error.message, "error");
  } finally {
    AppUi.setLoading(confirmTrackBtn, false);
  }
}

async function generatePlanAfterResult() {
  if (!confirmTrackSelect.value) {
    AppUi.showToast("Select a final technical role first.", "error");
    return;
  }

  if (!isCurrentTrackConfirmed(confirmTrackSelect.value)) {
    await confirmCareerTrackFromResult();
    if (!isCurrentTrackConfirmed(confirmTrackSelect.value)) {
      return;
    }
  }

  try {
    AppUi.setLoading(generatePlanBtn, true, "Generating...");
    const response = await AppApi.generateRoadmap();
    showNextFlow(`Readiness result saved for ${AppUi.humanize(confirmTrackSelect.value)}. ${response.goalsCreated} roadmap goal(s) were generated.`);
  } catch (error) {
    AppUi.showToast(error.message, "error");
  } finally {
    AppUi.setLoading(generatePlanBtn, false);
  }
}

function showNextFlow(message) {
  nextFlowSummary.textContent = message;
  nextFlowSection.classList.remove("hidden");
  AppUi.applyReveal(nextFlowSection);
}

function syncConfirmedTrackState() {
  const selectedTrack = !resultSection.classList.contains("hidden")
    ? confirmTrackSelect.value
    : !benchmarkSection.classList.contains("hidden")
      ? technicalRoleSelect.value
      : manualTrackSelect.value;

  if (isCurrentTrackConfirmed(selectedTrack)) {
    statusPill.textContent = "Role Confirmed";
    return;
  }

  if (!benchmarkSection.classList.contains("hidden")) {
    statusPill.textContent = "In Progress";
    return;
  }

  if (!resultSection.classList.contains("hidden")) {
    statusPill.textContent = "Completed";
    return;
  }

  statusPill.textContent = "Ready";
}

function isCurrentTrackConfirmed(track) {
  return Boolean(track) && confirmedTrack === track;
}
