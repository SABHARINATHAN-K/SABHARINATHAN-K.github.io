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
    const [roles, tracks] = await Promise.all([
      AppApi.listRoles(),
      AppApi.listCareerTracks()
    ]);

    setOptions(roleSelect, roles, "PROFESSIONAL");
    setOptions(careerTrackSelect, tracks, "SOFTWARE_ENGINEERING");
    AppUi.setMessage(messageBox, "Fill the form and create your account.");
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
    role: roleSelect.value,
    careerTrack: careerTrackSelect.value
  };

  AppUi.setLoading(registerBtn, true, "Creating account...");

  try {
    const res = await AppApi.request("/api/v1/auth/register", {
      method: "POST",
      auth: false,
      body: payload
    });

    AppApi.setToken(res.data.token);
    AppUi.setMessage(messageBox, "Account created. Redirecting to dashboard...");
    AppUi.showToast("Account created successfully");

    setTimeout(() => {
      window.location.href = "./dashboard.html";
    }, 550);
  } catch (error) {
    AppUi.setMessage(messageBox, error.message);
    AppUi.showToast(error.message, "error");
  } finally {
    AppUi.setLoading(registerBtn, false);
  }
});
