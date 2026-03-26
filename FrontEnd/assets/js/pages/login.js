const loginForm = document.getElementById("loginForm");
const loginBtn = document.getElementById("loginBtn");
const messageBox = document.getElementById("messageBox");
const googleSignInContainer = document.getElementById("googleSignInContainer");
const googleSignInHint = document.getElementById("googleSignInHint");

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

if (AppApi.getToken()) {
  redirectAfterAuth();
}

loginForm.addEventListener("submit", async (event) => {
  event.preventDefault();

  const email = document.getElementById("email").value.trim();
  const password = document.getElementById("password").value;

  AppUi.setLoading(loginBtn, true, "Signing in...");
  AppUi.setMessage(messageBox, portalMode === "admin" ? "Verifying admin access..." : "Authenticating...");

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
    AppUi.setMessage(messageBox, error.message);
    AppUi.showToast(error.message, "error");
  } finally {
    AppUi.setLoading(loginBtn, false);
  }
});

async function redirectAfterAuth() {
  try {
    const profile = await AppApi.getMe();
    const isAdmin = window.AppSession && typeof window.AppSession.isAdminProfile === "function"
      ? window.AppSession.isAdminProfile(profile)
      : String(profile && profile.role ? profile.role : "").toUpperCase() === "ADMIN";

    if (portalMode === "admin" && !isAdmin) {
      AppApi.clearToken();
      AppUi.setMessage(messageBox, "This account does not have admin access.");
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
    infoText.textContent = "Admins can open student plans, review readiness progress, and edit the question bank by role.";
    loginFooterLink.innerHTML = "Need the learner workspace instead? <a href='./login.html?portal=user'>Switch to learner sign in</a>";
    return;
  }

  portalBadge.textContent = "Learner Portal";
  loginHeading.textContent = "Welcome back";
  loginSubheading.textContent = "Sign in to continue your technical career planning workflow.";
  infoEyebrow.textContent = "Technical Readiness Workflow";
  infoTitle.textContent = "Log in to continue your plan";
  infoText.textContent = "Use the learner portal for readiness checks, goals, and analytics. Use the admin portal for platform oversight.";
  loginFooterLink.innerHTML = "Need a learner account? <a href='./register.html'>Create account</a>";
