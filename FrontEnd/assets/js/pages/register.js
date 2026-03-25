const registerForm = document.getElementById("registerForm");
const registerBtn = document.getElementById("registerBtn");
const roleSelect = document.getElementById("role");
const messageBox = document.getElementById("messageBox");

if (AppApi.getToken()) {
  redirectExistingSession();
}

init();

async function init() {
  try {
    const roles = await AppApi.listRoles();

    setOptions(roleSelect, roles, "PROFESSIONAL");
    AppUi.setMessage(messageBox, "Fill the form to create your learner account.");
  } catch (error) {
    AppUi.setMessage(messageBox, `Failed to load options: ${error.message}`);
    AppUi.showToast("Could not load form options", "error");
  }
}

function setOptions(selectElement, values, preferred) {
  selectElement.innerHTML = values
    .map((value) => `<option value="${AppUi.escapeAttr(value)}">${AppUi.escapeHtml(AppUi.humanize(value))}</option>`)
    .join("");

  if (values.includes(preferred)) {
    selectElement.value = preferred;
  }
}

registerForm.addEventListener("submit", async (event) => {
  event.preventDefault();

  const payload = {
    fullName: document.getElementById("fullName").value.trim(),
    email: document.getElementById("email").value.trim(),
    password: document.getElementById("password").value,
    role: roleSelect.value
  };

  AppUi.setLoading(registerBtn, true, "Creating account...");

  try {
    const res = await AppApi.request("/api/v1/auth/register", {
      method: "POST",
      auth: false,
      body: payload
    });

    AppApi.setToken(res.data.token);
    AppUi.setMessage(messageBox, "Account created. Redirecting to the technical readiness setup...");
    AppUi.showToast("Account created successfully");

    setTimeout(redirectAfterRegister, 400);
  } catch (error) {
    AppUi.setMessage(messageBox, error.message);
    AppUi.showToast(error.message, "error");
  } finally {
    AppUi.setLoading(registerBtn, false);
  }
});

function redirectAfterRegister() {
  window.location.href = "./technical-readiness.html";
}

async function redirectExistingSession() {
  try {
    const profile = await AppApi.getMe();
    if (window.AppSession && typeof window.AppSession.redirectToLanding === "function") {
      window.AppSession.redirectToLanding(profile);
      return;
    }
    window.location.href = profile && profile.onboardingCompleted === false
      ? "./technical-readiness.html"
      : "./home.html";
  } catch (error) {
    AppApi.clearToken();
  }
}
