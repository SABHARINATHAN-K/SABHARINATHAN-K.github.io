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
