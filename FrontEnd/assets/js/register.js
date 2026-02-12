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
