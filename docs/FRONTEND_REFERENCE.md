# Frontend Complete Reference

This document contains the complete frontend source (HTML, CSS, JS) and concise explanations.

## Frontend Overview
- Type: Multi-page frontend (no framework build step)
- Entry page: `FrontEnd/index.html`
- Styling: `FrontEnd/assets/css/styles.css`
- Logic: `FrontEnd/assets/js/*.js`
- API integration: `X-Auth-Token` header via `FrontEnd/assets/js/api.js`

## User Flow Summary
1. `index.html` -> user chooses Register/Login.
2. `register.html` -> user creates account with role and career track.
3. `login.html` -> user logs in and token is stored in localStorage.
4. `dashboard.html` -> profile, goal stats, and templates are shown.
5. `goals.html` -> create/update/delete/filter/search goals.

## Complete Source Code

### `FrontEnd/index.html`

Landing page with primary CTAs for registration and login.

```html
<!doctype html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Career Planning</title>
  <link rel="stylesheet" href="./assets/css/styles.css" />
</head>
<body>
  <main class="landing">
    <section class="landing-card">
      <p class="eyebrow">Career Planning System</p>
      <h1>Plan your career with clear goals and progress tracking</h1>
      <p class="subtext">A simple full-stack project with role-based onboarding, predefined career tracks, and personal goal management.</p>
      <div class="cta-row">
        <a class="btn" href="./pages/register.html">Get Started</a>
        <a class="btn btn-ghost" href="./pages/login.html">Login</a>
      </div>
    </section>
  </main>
</body>
</html>

```

### `FrontEnd/pages/login.html`

Login form page for existing users.

```html
<!doctype html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Login - Career Planning</title>
  <link rel="stylesheet" href="../assets/css/styles.css" />
</head>
<body>
  <main class="auth-layout">
    <section class="auth-side">
      <p class="eyebrow">Welcome Back</p>
      <h1>Continue your career plan</h1>
      <p>Login to check your profile, track your goals, and update progress.</p>
      <a href="../index.html" class="text-link">Back to Home</a>
    </section>

    <section class="auth-card">
      <h2>Login</h2>
      <form id="loginForm" class="form">
        <label>Email
          <input id="email" type="email" placeholder="you@example.com" required />
        </label>
        <label>Password
          <input id="password" type="password" placeholder="Enter password" required />
        </label>
        <button id="loginBtn" type="submit" class="btn full">Login</button>
      </form>
      <p class="muted">New user? <a href="./register.html">Create account</a></p>
      <pre id="messageBox" class="message">Ready.</pre>
    </section>
  </main>

  <script src="../assets/js/config.js"></script>
  <script src="../assets/js/ui.js"></script>
  <script src="../assets/js/api.js"></script>
  <script src="../assets/js/login.js"></script>
</body>
</html>

```

### `FrontEnd/pages/register.html`

Registration page with role and career-track onboarding fields.

```html
<!doctype html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Register - Career Planning</title>
  <link rel="stylesheet" href="../assets/css/styles.css" />
</head>
<body>
  <main class="auth-layout">
    <section class="auth-side">
      <p class="eyebrow">Start Strong</p>
      <h1>Create your personalized career journey</h1>
      <p>Choose your role and career track. We will use it to personalize goal templates and dashboard insights.</p>
      <a href="../index.html" class="text-link">Back to Home</a>
    </section>

    <section class="auth-card">
      <h2>Create Account</h2>
      <form id="registerForm" class="form">
        <label>Full Name
          <input id="fullName" type="text" placeholder="Your name" required />
        </label>
        <label>Email
          <input id="email" type="email" placeholder="you@example.com" required />
        </label>
        <label>Password
          <input id="password" type="password" minlength="8" placeholder="Min 8 characters" required />
        </label>
        <label>Role
          <select id="role" required>
            <option value="">Loading roles...</option>
          </select>
        </label>
        <label>Career Track
          <select id="careerTrack" required>
            <option value="">Loading tracks...</option>
          </select>
        </label>
        <button id="registerBtn" type="submit" class="btn full">Register</button>
      </form>
      <p class="muted">Already registered? <a href="./login.html">Login</a></p>
      <pre id="messageBox" class="message">Ready.</pre>
    </section>
  </main>

  <script src="../assets/js/config.js"></script>
  <script src="../assets/js/ui.js"></script>
  <script src="../assets/js/api.js"></script>
  <script src="../assets/js/register.js"></script>
</body>
</html>

```

### `FrontEnd/pages/dashboard.html`

Dashboard layout showing profile, stats, and recommended templates.

```html
<!doctype html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Dashboard - Career Planning</title>
  <link rel="stylesheet" href="../assets/css/styles.css" />
</head>
<body>
  <header class="topbar">
    <a class="brand" href="./dashboard.html">Career Planning</a>
    <nav>
      <a href="./goals.html">Goals</a>
      <button id="logoutBtn" class="link-btn" type="button">Logout</button>
    </nav>
  </header>

  <main class="container">
    <section class="hero-card">
      <div>
        <p class="eyebrow">Dashboard</p>
        <h1 id="welcomeText">Welcome</h1>
        <p class="muted">Track your growth by role, career track, and goal completion.</p>
      </div>
      <a class="btn" href="./goals.html">Manage Goals</a>
    </section>

    <section class="grid stats-grid">
      <article class="stat-card">
        <p>Total Goals</p>
        <h3 id="statTotal">0</h3>
      </article>
      <article class="stat-card">
        <p>Planned</p>
        <h3 id="statPlanned">0</h3>
      </article>
      <article class="stat-card">
        <p>In Progress</p>
        <h3 id="statInProgress">0</h3>
      </article>
      <article class="stat-card">
        <p>Completed</p>
        <h3 id="statCompleted">0</h3>
      </article>
    </section>

    <section class="grid dashboard-grid">
      <article class="card profile-card">
        <h2>My Profile</h2>
        <div class="profile-grid">
          <div class="profile-item">
            <span>Full Name</span>
            <p id="profileName">-</p>
          </div>
          <div class="profile-item">
            <span>Email</span>
            <p id="profileEmail">-</p>
          </div>
          <div class="profile-item">
            <span>Role</span>
            <p id="profileRole">-</p>
          </div>
          <div class="profile-item">
            <span>Career Track</span>
            <p id="profileCareerTrack">-</p>
          </div>
        </div>
        <p id="profileStatus" class="muted profile-status">Loading profile...</p>
      </article>

      <article class="card">
        <div class="row-between">
          <h2>Recommended Goal Templates</h2>
          <span class="pill" id="careerPill">Career</span>
        </div>
        <div id="templateList" class="template-list"></div>
      </article>
    </section>
  </main>

  <div id="toast" class="toast"></div>

  <script src="../assets/js/config.js"></script>
  <script src="../assets/js/ui.js"></script>
  <script src="../assets/js/api.js"></script>
  <script src="../assets/js/dashboard.js"></script>
</body>
</html>

```

### `FrontEnd/pages/goals.html`

Goals workspace for CRUD operations and goal filtering.

```html
<!doctype html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Goals - Career Planning</title>
  <link rel="stylesheet" href="../assets/css/styles.css" />
</head>
<body>
  <header class="topbar">
    <a class="brand" href="./dashboard.html">Career Planning</a>
    <nav>
      <a href="./dashboard.html">Dashboard</a>
      <button id="logoutBtn" class="link-btn" type="button">Logout</button>
    </nav>
  </header>

  <main class="container">
    <section class="hero-card">
      <div>
        <p class="eyebrow">Goals Workspace</p>
        <h1>Create, update, and track your goals</h1>
      </div>
      <button id="refreshBtn" type="button" class="btn btn-secondary">Refresh</button>
    </section>

    <section class="grid goals-grid">
      <article class="card">
        <h2>Create Goal</h2>
        <form id="createForm" class="form">
          <label>Title
            <input id="createTitle" type="text" required />
          </label>
          <label>Description
            <input id="createDescription" type="text" />
          </label>
          <label>Target Date
            <input id="createTargetDate" type="date" />
          </label>
          <button id="createBtn" type="submit" class="btn full">Create Goal</button>
        </form>

        <h3>Quick Templates</h3>
        <label>Career Track
          <select id="templateCareerTrack"></select>
        </label>
        <div id="templateButtons" class="template-list"></div>
      </article>

      <article class="card">
        <h2>Update Goal</h2>
        <form id="updateForm" class="form">
          <label>Goal ID
            <input id="updateId" type="number" required />
          </label>
          <label>Title
            <input id="updateTitle" type="text" required />
          </label>
          <label>Description
            <input id="updateDescription" type="text" />
          </label>
          <label>Status
            <select id="updateStatus">
              <option value="PLANNED">PLANNED</option>
              <option value="IN_PROGRESS">IN_PROGRESS</option>
              <option value="COMPLETED">COMPLETED</option>
            </select>
          </label>
          <label>Target Date
            <input id="updateTargetDate" type="date" />
          </label>
          <button id="updateBtn" type="submit" class="btn full">Update Goal</button>
        </form>
      </article>
    </section>

    <section class="card">
      <div class="row-between wrap">
        <h2>My Goals</h2>
        <div class="inline-filters">
          <input id="searchInput" type="text" placeholder="Search title/description" />
          <select id="statusFilter">
            <option value="ALL">All Status</option>
            <option value="PLANNED">PLANNED</option>
            <option value="IN_PROGRESS">IN_PROGRESS</option>
            <option value="COMPLETED">COMPLETED</option>
          </select>
        </div>
      </div>

      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Title</th>
            <th>Description</th>
            <th>Status</th>
            <th>Target Date</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody id="goalsBody"></tbody>
      </table>

      <pre id="messageBox" class="message">Ready.</pre>
    </section>
  </main>

  <div id="toast" class="toast"></div>

  <script src="../assets/js/config.js"></script>
  <script src="../assets/js/ui.js"></script>
  <script src="../assets/js/api.js"></script>
  <script src="../assets/js/goals.js"></script>
</body>
</html>

```

### `FrontEnd/assets/css/styles.css`

Global styling, layout system, theme colors, components, and responsiveness.

```css
:root {
  --bg: #eef3fb;
  --bg-soft: #f8fbff;
  --panel: #ffffff;
  --text: #1f2630;
  --muted: #5f6a7a;
  --line: #d7e2ef;
  --primary: #0f62d6;
  --primary-dark: #0b4ea8;
  --secondary: #347ab7;
  --danger: #c23737;
  --chip: #eef6ff;
}

* {
  box-sizing: border-box;
}

body {
  margin: 0;
  font-family: "Segoe UI", Tahoma, sans-serif;
  color: var(--text);
  background: radial-gradient(circle at 10% 0%, #dcecff 0%, var(--bg) 45%),
              linear-gradient(150deg, var(--bg) 0%, #f7faff 100%);
}

a {
  color: var(--primary-dark);
}

.landing {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 1rem;
}

.landing-card {
  width: min(760px, 100%);
  background: var(--panel);
  border: 1px solid var(--line);
  border-radius: 20px;
  padding: 2rem;
  box-shadow: 0 14px 30px rgba(13, 55, 110, 0.11);
  animation: rise 0.45s ease-out;
}

.eyebrow {
  text-transform: uppercase;
  letter-spacing: 0.08em;
  font-size: 0.78rem;
  color: var(--secondary);
  font-weight: 700;
  margin: 0 0 0.5rem;
}

.subtext {
  color: var(--muted);
}

.auth-layout {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.1fr 1fr;
}

.auth-side {
  padding: 2.2rem;
  display: grid;
  align-content: center;
  gap: 0.9rem;
  background: linear-gradient(140deg, #0c5dcc 0%, #3087d7 100%);
  color: white;
}

.auth-side .eyebrow {
  color: #d6e9ff;
}

.auth-side p {
  margin: 0;
  line-height: 1.5;
}

.text-link {
  color: white;
  text-decoration: underline;
}

.auth-card {
  display: grid;
  align-content: center;
  gap: 0.8rem;
  padding: 2rem;
  background: var(--panel);
}

.topbar {
  position: sticky;
  top: 0;
  z-index: 10;
  background: rgba(255, 255, 255, 0.96);
  border-bottom: 1px solid var(--line);
  backdrop-filter: blur(6px);
  padding: 0.9rem 1.1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.brand {
  font-weight: 700;
  text-decoration: none;
  color: var(--primary-dark);
}

.topbar nav {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.container {
  width: min(1160px, calc(100% - 2rem));
  margin: 1.2rem auto 2rem;
}

.grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 1rem;
}

.dashboard-grid {
  align-items: start;
}

.goals-grid {
  align-items: start;
}

.stats-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.hero-card {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  align-items: center;
  border: 1px solid var(--line);
  border-radius: 16px;
  background: linear-gradient(135deg, #ffffff 0%, #f2f8ff 100%);
  padding: 1.1rem 1.2rem;
  box-shadow: 0 10px 24px rgba(18, 56, 106, 0.08);
  margin-bottom: 1rem;
}

.card,
.stat-card {
  background: var(--panel);
  border: 1px solid var(--line);
  border-radius: 14px;
  box-shadow: 0 10px 24px rgba(21, 57, 106, 0.08);
  padding: 1rem;
  animation: rise 0.32s ease-out;
}

.stat-card p {
  margin: 0;
  color: var(--muted);
  font-size: 0.88rem;
}

.stat-card h3 {
  margin: 0.35rem 0 0;
  font-size: 1.5rem;
}

.form {
  display: grid;
  gap: 0.7rem;
}

label {
  display: grid;
  gap: 0.3rem;
  font-size: 0.94rem;
}

input,
select,
button {
  font: inherit;
  border-radius: 10px;
}

input,
select {
  border: 1px solid var(--line);
  padding: 0.62rem 0.75rem;
  background: #fff;
}

.btn {
  border: 1px solid transparent;
  background: var(--primary);
  color: #fff;
  padding: 0.62rem 0.86rem;
  cursor: pointer;
  transition: background 0.2s ease, transform 0.08s ease;
}

.btn:hover {
  background: var(--primary-dark);
}

.btn:active {
  transform: translateY(1px);
}

.btn-secondary {
  background: var(--secondary);
}

.btn-ghost {
  background: transparent;
  color: var(--primary-dark);
  border-color: var(--line);
}

.full {
  width: 100%;
}

.link-btn {
  border: none;
  background: transparent;
  color: var(--primary-dark);
  text-decoration: underline;
  cursor: pointer;
}

.row-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.8rem;
}

.wrap {
  flex-wrap: wrap;
}

.inline-filters {
  display: flex;
  gap: 0.6rem;
  flex-wrap: wrap;
}

table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 0.65rem;
}

th,
td {
  border: 1px solid var(--line);
  padding: 0.5rem;
  text-align: left;
  vertical-align: top;
  font-size: 0.9rem;
}

.action-group {
  display: flex;
  gap: 0.4rem;
  flex-wrap: wrap;
}

.action-btn {
  border: 1px solid transparent;
  padding: 0.36rem 0.56rem;
  border-radius: 8px;
  font-size: 0.8rem;
  cursor: pointer;
}

.action-btn.edit {
  background: #2e7cc8;
  color: #fff;
}

.action-btn.delete {
  background: var(--danger);
  color: #fff;
}

.message {
  margin-top: 0.75rem;
  border: 1px solid var(--line);
  border-radius: 10px;
  background: var(--bg-soft);
  padding: 0.7rem;
  min-height: 52px;
  white-space: pre-wrap;
  word-break: break-word;
}

.template-list {
  display: grid;
  gap: 0.55rem;
  margin-top: 0.6rem;
}

.template-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.6rem;
  border: 1px solid var(--line);
  background: #fff;
  border-radius: 10px;
  padding: 0.55rem 0.6rem;
}

.template-item p {
  margin: 0;
  font-size: 0.9rem;
}

.template-add {
  border: none;
  background: var(--chip);
  color: var(--primary-dark);
  padding: 0.38rem 0.55rem;
  border-radius: 8px;
  cursor: pointer;
}

.pill {
  background: var(--chip);
  color: var(--primary-dark);
  border: 1px solid var(--line);
  padding: 0.2rem 0.5rem;
  border-radius: 999px;
  font-size: 0.78rem;
}

.muted {
  color: var(--muted);
}

.cta-row {
  display: flex;
  gap: 0.7rem;
  flex-wrap: wrap;
  margin-top: 1rem;
}

.toast {
  position: fixed;
  right: 1rem;
  bottom: 1rem;
  background: #1c2f46;
  color: #fff;
  padding: 0.6rem 0.8rem;
  border-radius: 10px;
  box-shadow: 0 10px 18px rgba(13, 24, 37, 0.26);
  opacity: 0;
  pointer-events: none;
  transform: translateY(10px);
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.toast.show {
  opacity: 1;
  transform: translateY(0);
}

@keyframes rise {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 980px) {
  .auth-layout {
    grid-template-columns: 1fr;
  }

  .auth-side {
    min-height: 220px;
  }

  .grid,
  .stats-grid {
    grid-template-columns: 1fr;
  }

  table {
    display: block;
    overflow-x: auto;
  }
}

.profile-card {
  display: grid;
  gap: 0.8rem;
}

.profile-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.7rem;
}

.profile-item {
  border: 1px solid var(--line);
  background: var(--bg-soft);
  border-radius: 10px;
  padding: 0.65rem;
}

.profile-item span {
  display: block;
  font-size: 0.78rem;
  text-transform: uppercase;
  letter-spacing: 0.07em;
  color: var(--muted);
  margin-bottom: 0.25rem;
}

.profile-item p {
  margin: 0;
  font-weight: 600;
}

.profile-status {
  margin: 0;
}

@media (max-width: 700px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }
}

```

### `FrontEnd/assets/js/config.js`

Application config for backend base URL.

```javascript
window.APP_CONFIG = {
  API_BASE_URL: "http://localhost:8080"
};

```

### `FrontEnd/assets/js/ui.js`

Shared UI helpers for messages, loading states, humanized labels, and toast.

```javascript
(function () {
  function setMessage(element, text) {
    if (element) {
      element.textContent = text;
    }
  }

  function setLoading(button, isLoading, loadingText) {
    if (!button) {
      return;
    }
    if (isLoading) {
      button.dataset.originalText = button.textContent;
      button.textContent = loadingText || "Loading...";
      button.disabled = true;
    } else {
      button.textContent = button.dataset.originalText || button.textContent;
      button.disabled = false;
    }
  }

  function humanize(value) {
    return String(value || "")
      .toLowerCase()
      .split("_")
      .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
      .join(" ");
  }

  function showToast(message) {
    const toast = document.getElementById("toast");
    if (!toast) {
      return;
    }

    toast.textContent = message;
    toast.classList.add("show");

    clearTimeout(toast._timer);
    toast._timer = setTimeout(() => {
      toast.classList.remove("show");
    }, 1800);
  }

  window.AppUi = {
    setMessage,
    setLoading,
    humanize,
    showToast
  };
})();

```

### `FrontEnd/assets/js/api.js`

HTTP client wrapper, token storage, and lookup APIs.

```javascript
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

```

### `FrontEnd/assets/js/login.js`

Login flow: form submit, authentication request, token handling, redirect.

```javascript
const loginForm = document.getElementById("loginForm");
const loginBtn = document.getElementById("loginBtn");
const messageBox = document.getElementById("messageBox");

if (AppApi.getToken()) {
  window.location.href = "./dashboard.html";
}

loginForm.addEventListener("submit", async (event) => {
  event.preventDefault();

  const email = document.getElementById("email").value.trim();
  const password = document.getElementById("password").value;

  AppUi.setLoading(loginBtn, true, "Signing in...");
  AppUi.setMessage(messageBox, "Authenticating...");

  try {
    const res = await AppApi.request("/api/v1/auth/login", {
      method: "POST",
      auth: false,
      body: { email, password }
    });

    AppApi.setToken(res.data.token);
    AppUi.setMessage(messageBox, "Login successful. Redirecting...");
    setTimeout(() => {
      window.location.href = "./dashboard.html";
    }, 450);
  } catch (error) {
    AppUi.setMessage(messageBox, error.message);
  } finally {
    AppUi.setLoading(loginBtn, false);
  }
});

```

### `FrontEnd/assets/js/register.js`

Registration flow with dynamic role/track options from backend lookups.

```javascript
const registerForm = document.getElementById("registerForm");
const registerBtn = document.getElementById("registerBtn");
const roleSelect = document.getElementById("role");
const careerTrackSelect = document.getElementById("careerTrack");
const messageBox = document.getElementById("messageBox");

if (AppApi.getToken()) {
  window.location.href = "./dashboard.html";
}

init();

async function init() {
  try {
    const [roles, careerTracks] = await Promise.all([
      AppApi.listRoles(),
      AppApi.listCareerTracks()
    ]);

    setOptions(roleSelect, roles);
    setOptions(careerTrackSelect, careerTracks);
    AppUi.setMessage(messageBox, "Choose your role and career track.");
  } catch (error) {
    AppUi.setMessage(messageBox, `Failed to load options: ${error.message}`);
  }
}

function setOptions(selectElement, values) {
  selectElement.innerHTML = values
    .map((value) => `<option value="${value}">${AppUi.humanize(value)}</option>`)
    .join("");
}

registerForm.addEventListener("submit", async (event) => {
  event.preventDefault();

  const fullName = document.getElementById("fullName").value.trim();
  const email = document.getElementById("email").value.trim();
  const password = document.getElementById("password").value;
  const role = roleSelect.value;
  const careerTrack = careerTrackSelect.value;

  AppUi.setLoading(registerBtn, true, "Creating account...");

  try {
    const res = await AppApi.request("/api/v1/auth/register", {
      method: "POST",
      auth: false,
      body: { fullName, email, password, role, careerTrack }
    });

    AppApi.setToken(res.data.token);
    AppUi.setMessage(messageBox, "Registration successful. Redirecting...");
    setTimeout(() => {
      window.location.href = "./dashboard.html";
    }, 550);
  } catch (error) {
    AppUi.setMessage(messageBox, error.message);
  } finally {
    AppUi.setLoading(registerBtn, false);
  }
});

```

### `FrontEnd/assets/js/dashboard.js`

Dashboard data loading, stat calculation, template actions, and logout.

```javascript
const logoutBtn = document.getElementById("logoutBtn");
const welcomeText = document.getElementById("welcomeText");
const careerPill = document.getElementById("careerPill");
const templateList = document.getElementById("templateList");
const profileName = document.getElementById("profileName");
const profileEmail = document.getElementById("profileEmail");
const profileRole = document.getElementById("profileRole");
const profileCareerTrack = document.getElementById("profileCareerTrack");
const profileStatus = document.getElementById("profileStatus");

if (AppApi.requireAuth()) {
  initDashboard();
}

logoutBtn.addEventListener("click", () => {
  AppApi.clearToken();
  window.location.href = "./login.html";
});

async function initDashboard() {
  try {
    const [profileRes, goalsRes, templateMap] = await Promise.all([
      AppApi.request("/api/v1/users/me"),
      AppApi.request("/api/v1/goals"),
      AppApi.listGoalTemplates()
    ]);

    const profile = profileRes.data;
    const goals = goalsRes.data || [];

    welcomeText.textContent = `Welcome, ${profile.fullName}`;
    profileName.textContent = profile.fullName;
    profileEmail.textContent = profile.email;
    profileRole.textContent = AppUi.humanize(profile.role);
    profileCareerTrack.textContent = AppUi.humanize(profile.careerTrack);
    profileStatus.textContent = "Profile loaded successfully.";
    careerPill.textContent = AppUi.humanize(profile.careerTrack);

    renderStats(goals);
    renderTemplateActions(profile.careerTrack, templateMap);
  } catch (error) {
    profileStatus.textContent = `Could not load profile: ${error.message}`;
  }
}

function renderStats(goals) {
  const total = goals.length;
  const planned = goals.filter((goal) => goal.status === "PLANNED").length;
  const inProgress = goals.filter((goal) => goal.status === "IN_PROGRESS").length;
  const completed = goals.filter((goal) => goal.status === "COMPLETED").length;

  document.getElementById("statTotal").textContent = total;
  document.getElementById("statPlanned").textContent = planned;
  document.getElementById("statInProgress").textContent = inProgress;
  document.getElementById("statCompleted").textContent = completed;
}

function renderTemplateActions(careerTrack, templateMap) {
  const templates = templateMap[careerTrack] || [];

  if (!templates.length) {
    templateList.innerHTML = "<p class='muted'>No templates available.</p>";
    return;
  }

  templateList.innerHTML = templates
    .map((template) => {
      return `<div class="template-item">
        <p>${escapeHtml(template)}</p>
        <button class="template-add" data-template="${escapeAttr(template)}" type="button">Use</button>
      </div>`;
    })
    .join("");

  templateList.querySelectorAll("button[data-template]").forEach((button) => {
    button.addEventListener("click", () => {
      const template = button.getAttribute("data-template") || "";
      const url = new URL("./goals.html", window.location.href);
      url.searchParams.set("title", template);
      url.searchParams.set("description", `Planned from template: ${template}`);
      window.location.href = url.toString();
    });
  });
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

```

### `FrontEnd/assets/js/goals.js`

Goal CRUD, template apply, search/filter, table rendering, edit/delete actions.

```javascript
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

```
