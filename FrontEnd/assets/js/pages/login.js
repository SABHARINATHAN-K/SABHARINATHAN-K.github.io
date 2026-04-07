const loginForm = document.getElementById("loginForm");
const loginBtn = document.getElementById("loginBtn");
const messageBox = document.getElementById("messageBox");
const googleSignInContainer = document.getElementById("googleSignInContainer");
const googleSignInHint = document.getElementById("googleSignInHint");
const emailInput = document.getElementById("email");
const passwordInput = document.getElementById("password");
const passwordToggle = document.getElementById("passwordToggle");
const portalHint = document.getElementById("portalHint");

const portalBadge = document.getElementById("portalBadge");
const loginHeading = document.getElementById("loginHeading");
const loginSubheading = document.getElementById("loginSubheading");
const infoEyebrow = document.getElementById("infoEyebrow");
const infoTitle = document.getElementById("infoTitle");
const infoText = document.getElementById("infoText");
const loginFooterLink = document.getElementById("loginFooterLink");
const portalUserLink = document.getElementById("portalUserLink");
const portalAdminLink = document.getElementById("portalAdminLink");

const portalMode = getPortalMode();
applyPortalCopy();
bindPasswordToggle(passwordToggle, passwordInput);

if (AppApi.getToken()) {
  redirectAfterAuth();
}

loginForm.addEventListener("submit", async (event) => {
  event.preventDefault();

  const email = normalizeEmailValue(emailInput && emailInput.value);
  const password = passwordInput ? passwordInput.value : "";

  AppUi.setLoading(loginBtn, true, "Signing in...");
  AppUi.setMessage(messageBox, portalMode === "admin" ? "Verifying admin access..." : "Authenticating your learner workspace...");

  try {
    const res = await AppApi.request("/api/v1/auth/login", {
      method: "POST",
      auth: false,
      body: { email, password }
    });

    AppApi.setToken(res.data.token);
    AppUi.setMessage(messageBox, "Login successful. Redirecting...");
    AppUi.showToast("Login successful");

    setTimeout(redirectAfterAuth, 350);
  } catch (error) {
    const friendlyMessage = formatLoginError(error);
    AppUi.setMessage(messageBox, friendlyMessage);
    AppUi.showToast(friendlyMessage, "error");
  } finally {
    AppUi.setLoading(loginBtn, false);
  }
});

function bindPasswordToggle(button, input) {
  if (!button || !input) {
    return;
  }

  button.addEventListener("click", () => {
    const nextType = input.type === "password" ? "text" : "password";
    input.type = nextType;
    button.textContent = nextType === "password" ? "Show" : "Hide";
    button.setAttribute("aria-label", nextType === "password" ? "Show password" : "Hide password");
  });
}

function normalizeEmailValue(value) {
  return String(value || "").trim().toLowerCase();
}

function formatLoginError(error) {
  if (!error || !error.message) {
    return "Could not complete login.";
  }

  if (error.message === "Invalid credentials") {
    return "Invalid credentials. Check your email spelling and password case, then try again.";
  }

  return error.message;
}

async function redirectAfterAuth() {
  try {
    const profile = await AppApi.getMe();
    const isAdmin = window.AppSession && typeof window.AppSession.isAdminProfile === "function"
      ? window.AppSession.isAdminProfile(profile)
      : String(profile && profile.role ? profile.role : "").toUpperCase() === "ADMIN";

    if (portalMode === "admin" && !isAdmin) {
      AppApi.clearToken();
      AppUi.setMessage(messageBox, "This account does not have admin access. Use the learner portal instead.");
      AppUi.showToast("Admin access required", "error");
      return;
    }

    if (window.AppSession && typeof window.AppSession.redirectToLanding === "function") {
      window.AppSession.redirectToLanding(profile);
      return;
    }

    if (isAdmin) {
      window.location.href = "./admin.html";
      return;
    }

    window.location.href = profile && profile.onboardingCompleted === false
      ? "./technical-readiness.html"
      : "./home.html";
  } catch (error) {
    AppApi.clearToken();
    AppUi.setMessage(messageBox, "Could not load your account after login.");
    AppUi.showToast("Could not complete login", "error");
  }
}

function getPortalMode() {
  const params = new URLSearchParams(window.location.search);
  return params.get("portal") === "admin" ? "admin" : "user";
}

function applyPortalCopy() {
  const isAdminPortal = portalMode === "admin";

  portalUserLink?.classList.toggle("active", !isAdminPortal);
  portalAdminLink?.classList.toggle("active", isAdminPortal);

  if (isAdminPortal) {
    portalBadge.textContent = "Admin Portal";
    loginHeading.textContent = "Admin sign in";
    loginSubheading.textContent = "Use your admin account to manage learners, plans, and question quality.";
    infoEyebrow.textContent = "Admin Control Room";
    infoTitle.textContent = "Access platform administration";
    infoText.textContent = "Admins can open learner plans, review readiness progress, and edit the question bank by role.";
    portalHint.textContent = "Use the admin tab only for provisioned admin accounts. Learner accounts should sign in from the learner tab.";
    loginFooterLink.innerHTML = "Need the learner workspace instead? <a href='./login.html?portal=user'>Switch to learner sign in</a>";
    AppUi.setMessage(messageBox, "Use your admin email and password to enter the control room.");
    return;
  }

  portalBadge.textContent = "Learner Portal";
  loginHeading.textContent = "Welcome back";
  loginSubheading.textContent = "Sign in to continue your technical career planning workflow.";
  infoEyebrow.textContent = "Technical Readiness Workflow";
  infoTitle.textContent = "Log in to continue your plan";
  infoText.textContent = "Use the learner portal for readiness checks, goals, and analytics. Use the admin portal for platform oversight.";
  portalHint.textContent = "Learner accounts are created from the register page. Email matching is case-insensitive, so you can sign in with any letter casing.";
  loginFooterLink.innerHTML = "Need a learner account? <a href='./register.html'>Create account</a>";
  AppUi.setMessage(messageBox, "Use the email and password you registered with to continue.");
}

void googleSignInContainer;
void googleSignInHint;
