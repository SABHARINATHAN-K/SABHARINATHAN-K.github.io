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

  async function listGoalTemplates() {
    const res = await request("/api/v1/lookups/goal-templates", { auth: false });
    return res.data || {};
  }

  window.AppApi = {
    request,
    getToken,
    setToken,
    clearToken,
    requireAuth,
    listRoles,
    listCareerTracks,
    listGoalTemplates
  };
})();
