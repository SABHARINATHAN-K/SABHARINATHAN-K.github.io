(function () {
  const TOKEN_KEY = "career_planning_token";

  function getToken() {
    return localStorage.getItem(TOKEN_KEY) || "";
  }

  function setToken(token) {
    localStorage.setItem(TOKEN_KEY, token || "");
  }

  function clearToken() {
    localStorage.removeItem(TOKEN_KEY);
  }

  function requireAuth() {
    if (!getToken()) {
      window.location.href = "./login.html";
      return false;
    }
    return true;
  }

  async function request(path, options) {
    const opts = options || {};
    const headers = Object.assign({}, opts.headers || {});

    if (opts.body !== undefined) {
      headers["Content-Type"] = "application/json";
    }

    if (opts.auth !== false) {
      const token = getToken();
      if (!token) {
        throw new Error("Login required.");
      }
      headers["X-Auth-Token"] = token;
    }

    const url = `${window.APP_CONFIG.API_BASE_URL}${path}`;
    const response = await fetch(url, {
      method: opts.method || "GET",
      headers,
      body: opts.body !== undefined ? JSON.stringify(opts.body) : undefined
    });

    const text = await response.text();
    let payload = {};

    try {
      payload = text ? JSON.parse(text) : {};
    } catch (error) {
      throw new Error("Invalid server response.");
    }

    if (!response.ok || payload.success === false) {
      throw new Error(payload.message || "Request failed.");
    }

    return payload;
  }

  async function listRoles() {
    const res = await request("/api/v1/lookups/roles", { auth: false });
    return res.data || [];
  }

  async function listCareerTracks() {
    const res = await request("/api/v1/lookups/career-tracks", { auth: false });
    return res.data || [];
  }

  async function listGoalCategories() {
    const res = await request("/api/v1/lookups/goal-categories", { auth: false });
    return res.data || [];
  }

  async function listGoalPriorities() {
    const res = await request("/api/v1/lookups/goal-priorities", { auth: false });
    return res.data || [];
  }

  async function listGoalTemplates() {
    const res = await request("/api/v1/lookups/goal-templates", { auth: false });
    return res.data || {};
  }

  async function getMe() {
    const res = await request("/api/v1/users/me");
    return res.data;
  }

  async function loginWithGoogle(idToken) {
    const res = await request("/api/v1/auth/google", {
      method: "POST",
      auth: false,
      body: { idToken }
    });
    return res.data;
  }

  async function updateMe(body) {
    const res = await request("/api/v1/users/me", {
      method: "PUT",
      body
    });
    return res.data;
  }

  async function listGoals() {
    const res = await request("/api/v1/goals");
    return res.data || [];
  }

  async function getGoal(goalId) {
    const res = await request(`/api/v1/goals/${goalId}`);
    return res.data;
  }

  async function createGoal(body) {
    const res = await request("/api/v1/goals", {
      method: "POST",
      body
    });
    return res.data;
  }

  async function updateGoal(goalId, body) {
    const res = await request(`/api/v1/goals/${goalId}`, {
      method: "PUT",
      body
    });
    return res.data;
  }

  async function deleteGoal(goalId) {
    const res = await request(`/api/v1/goals/${goalId}`, {
      method: "DELETE"
    });
    return res.data;
  }

  async function getGoalStats() {
    const res = await request("/api/v1/goals/stats");
    return res.data;
  }

  async function listGoalTasks(goalId) {
    const res = await request(`/api/v1/goals/${goalId}/tasks`);
    return res.data || [];
  }

  async function createGoalTask(goalId, body) {
    const res = await request(`/api/v1/goals/${goalId}/tasks`, {
      method: "POST",
      body
    });
    return res.data;
  }

  async function updateGoalTask(goalId, taskId, body) {
    const res = await request(`/api/v1/goals/${goalId}/tasks/${taskId}`, {
      method: "PUT",
      body
    });
    return res.data;
  }

  async function deleteGoalTask(goalId, taskId) {
    const res = await request(`/api/v1/goals/${goalId}/tasks/${taskId}`, {
      method: "DELETE"
    });
    return res.data;
  }

  async function listTechnicalAssessmentTracks() {
    const res = await request("/api/v1/career/technical-assessment/tracks", { auth: false });
    return res.data || [];
  }

  async function listTechnicalAssessmentQuestions(careerTrack) {
    const query = new URLSearchParams({ careerTrack });
    const res = await request(`/api/v1/career/technical-assessment/questions?${query.toString()}`, { auth: false });
    return res.data || [];
  }

  async function submitTechnicalAssessment(body) {
    const res = await request("/api/v1/career/technical-assessment/submit", {
      method: "POST",
      body
    });
    return res.data;
  }

  async function getTechnicalAssessmentProgress() {
    const res = await request("/api/v1/career/technical-assessment/progress");
    return res.data;
  }

  async function confirmCareerTrack(body) {
    const res = await request("/api/v1/career/confirm-track", {
      method: "POST",
      body
    });
    return res.data;
  }

  async function generateRoadmap() {
    const res = await request("/api/v1/career/generate-roadmap", {
      method: "POST"
    });
    return res.data;
  }

  async function listAdminRoles() {
    const res = await request("/api/v1/admin/roles");
    return res.data || [];
  }

  async function listAdminUsers() {
    const res = await request("/api/v1/admin/users");
    return res.data || [];
  }

  async function getAdminUserDetail(userId) {
    const res = await request(`/api/v1/admin/users/${userId}`);
    return res.data;
  }

  async function updateAdminUser(userId, body) {
    const res = await request(`/api/v1/admin/users/${userId}`, {
      method: "PUT",
      body
    });
    return res.data;
  }

  async function listAdminTechnicalAssessmentQuestions(careerTrack) {
    const query = new URLSearchParams({ careerTrack });
    const res = await request(`/api/v1/admin/technical-assessment/questions?${query.toString()}`);
    return res.data || [];
  }

  async function createAdminTechnicalAssessmentQuestion(body) {
    const res = await request("/api/v1/admin/technical-assessment/questions", {
      method: "POST",
      body
    });
    return res.data;
  }

  async function updateAdminTechnicalAssessmentQuestion(questionId, body) {
    const res = await request(`/api/v1/admin/technical-assessment/questions/${questionId}`, {
      method: "PUT",
      body
    });
    return res.data;
  }

  async function deleteAdminTechnicalAssessmentQuestion(questionId) {
    const res = await request(`/api/v1/admin/technical-assessment/questions/${questionId}`, {
      method: "DELETE"
    });
    return res.data;
  }

  window.AppApi = {
    request,
    getToken,
    setToken,
    clearToken,
    requireAuth,
    listRoles,
    listCareerTracks,
    listGoalCategories,
    listGoalPriorities,
    listGoalTemplates,
    getMe,
    loginWithGoogle,
    updateMe,
    listGoals,
    getGoal,
    createGoal,
    updateGoal,
    deleteGoal,
    getGoalStats,
    listGoalTasks,
    createGoalTask,
    updateGoalTask,
    deleteGoalTask,
    listTechnicalAssessmentTracks,
    listTechnicalAssessmentQuestions,
    submitTechnicalAssessment,
    getTechnicalAssessmentProgress,
    confirmCareerTrack,
    generateRoadmap,
    listAdminRoles,
    listAdminUsers,
    getAdminUserDetail,
    updateAdminUser,
    listAdminTechnicalAssessmentQuestions,
    createAdminTechnicalAssessmentQuestion,
    updateAdminTechnicalAssessmentQuestion,
    deleteAdminTechnicalAssessmentQuestion
  };
})();
