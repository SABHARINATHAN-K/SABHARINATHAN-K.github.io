const registerForm = document.getElementById("registerForm");
const registerBtn = document.getElementById("registerBtn");
const roleSelect = document.getElementById("role");
const roleHint = document.getElementById("roleHint");
const messageBox = document.getElementById("messageBox");
const fullNameInput = document.getElementById("fullName");
const emailInput = document.getElementById("email");
const passwordInput = document.getElementById("password");
const confirmPasswordInput = document.getElementById("confirmPassword");
const confirmPasswordHelp = document.getElementById("confirmPasswordHelp");
const passwordToggle = document.getElementById("passwordToggle");
const confirmPasswordToggle = document.getElementById("confirmPasswordToggle");
const passwordChecklistItems = Array.from(document.querySelectorAll("[data-password-rule]"));

const PASSWORD_RULES = {
  length: {
    label: "at least 8 characters",
    test(value) {
      return String(value || "").length >= 8;
    }
  },
  lower: {
    label: "one lowercase letter",
    test(value) {
      return /[a-z]/.test(value || "");
    }
  },
  upper: {
    label: "one uppercase letter",
    test(value) {
      return /[A-Z]/.test(value || "");
    }
  },
  digit: {
    label: "one number",
    test(value) {
      return /[0-9]/.test(value || "");
    }
  },
  special: {
    label: "one special character",
    test(value) {
      return /[^A-Za-z0-9]/.test(value || "");
    }
  }
};

const ROLE_HINTS = {
  STUDENT: "Recommended for students and early learners building their first roadmap.",
  FRESH_GRADUATE: "Great for graduates who want a structured transition into their first technical role.",
  WORKING_PROFESSIONAL: "Best if you are already employed and using the learner workspace to upskill with a focused plan.",
  CAREER_SWITCHER: "Use this if you are moving into a new technical track and want a guided transition plan.",
  PROFESSIONAL: "A balanced option for experienced learners who want readiness checks, goals, and reassessment history.",
  MANAGER: "Useful if you want a learner workspace focused on technical context while leading teams.",
  EXECUTIVE: "Choose this if you want an executive-level learner view with technical planning support."
};

if (AppApi.getToken()) {
  redirectExistingSession();
}

bindPasswordToggle(passwordToggle, passwordInput);
bindPasswordToggle(confirmPasswordToggle, confirmPasswordInput);
passwordInput?.addEventListener("input", updatePasswordFeedback);
confirmPasswordInput?.addEventListener("input", updateConfirmPasswordFeedback);
roleSelect?.addEventListener("change", updateRoleHint);

init();

async function init() {
  updatePasswordFeedback();
  updateRoleHint();

  try {
    const roles = await AppApi.listRoles();
    setOptions(roleSelect, roles, "STUDENT");
    updateRoleHint();
    AppUi.setMessage(messageBox, "Create your learner account, then continue into the technical readiness setup.");
  } catch (error) {
    AppUi.setMessage(messageBox, `Failed to load options: ${error.message}`);
    AppUi.showToast("Could not load form options", "error");
  }
}

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

function setOptions(selectElement, values, preferred) {
  if (!selectElement) {
    return;
  }

  const orderedValues = orderRoles(values || []);
  selectElement.innerHTML = orderedValues
    .map((value) => `<option value="${AppUi.escapeAttr(value)}">${AppUi.escapeHtml(AppUi.humanize(value))}</option>`)
    .join("");

  if (orderedValues.includes(preferred)) {
    selectElement.value = preferred;
  } else if (orderedValues.length) {
    selectElement.value = orderedValues[0];
  }
}

function orderRoles(values) {
  const preferredOrder = [
    "STUDENT",
    "FRESH_GRADUATE",
    "WORKING_PROFESSIONAL",
    "CAREER_SWITCHER",
    "PROFESSIONAL",
    "MANAGER",
    "EXECUTIVE"
  ];

  return [...values].sort((left, right) => {
    const leftIndex = preferredOrder.indexOf(left);
    const rightIndex = preferredOrder.indexOf(right);
    const normalizedLeftIndex = leftIndex === -1 ? Number.MAX_SAFE_INTEGER : leftIndex;
    const normalizedRightIndex = rightIndex === -1 ? Number.MAX_SAFE_INTEGER : rightIndex;

    if (normalizedLeftIndex !== normalizedRightIndex) {
      return normalizedLeftIndex - normalizedRightIndex;
    }

    return String(left).localeCompare(String(right));
  });
}

function getPasswordStatus(password) {
  return Object.fromEntries(
    Object.entries(PASSWORD_RULES).map(([ruleName, rule]) => [ruleName, rule.test(password)])
  );
}

function updatePasswordFeedback() {
  const passwordValue = passwordInput ? passwordInput.value : "";
  const status = getPasswordStatus(passwordValue);

  passwordChecklistItems.forEach((item) => {
    const ruleName = item.getAttribute("data-password-rule");
    item.classList.toggle("is-valid", Boolean(status[ruleName]));
  });

  updateConfirmPasswordFeedback();
}

function updateConfirmPasswordFeedback() {
  if (!confirmPasswordHelp || !confirmPasswordInput || !passwordInput) {
    return;
  }

  confirmPasswordHelp.classList.remove("is-error", "is-success");

  if (!confirmPasswordInput.value) {
    confirmPasswordHelp.textContent = "Re-enter the same password to continue.";
    return;
  }

  if (confirmPasswordInput.value === passwordInput.value) {
    confirmPasswordHelp.textContent = "Passwords match.";
    confirmPasswordHelp.classList.add("is-success");
    return;
  }

  confirmPasswordHelp.textContent = "Passwords do not match yet.";
  confirmPasswordHelp.classList.add("is-error");
}

function updateRoleHint() {
  if (!roleHint) {
    return;
  }

  const selectedRole = String(roleSelect && roleSelect.value ? roleSelect.value : "").toUpperCase();
  const roleMessage = ROLE_HINTS[selectedRole] || "All listed roles use the learner workspace.";
  roleHint.textContent = `${roleMessage} Admin accounts are provisioned separately.`;
}

function normalizeFullName(value) {
  return String(value || "").trim().replace(/\s+/g, " ");
}

function normalizeEmail(value) {
  return String(value || "").trim().toLowerCase();
}

function validateRegistrationPayload(payload) {
  if (!payload.fullName) {
    return "Enter your full name to create the account.";
  }

  if (!payload.email) {
    return "Enter the email address you want to use for login.";
  }

  if (!payload.role) {
    return "Choose the learner role that best matches your current stage.";
  }

  const passwordStatus = getPasswordStatus(payload.password);
  const missingRules = Object.entries(PASSWORD_RULES)
    .filter(([ruleName]) => !passwordStatus[ruleName])
    .map(([, rule]) => rule.label);

  if (missingRules.length) {
    return `Password still needs ${missingRules.join(", ")}.`;
  }

  if (payload.password !== String(confirmPasswordInput && confirmPasswordInput.value ? confirmPasswordInput.value : "")) {
    return "Confirm password must exactly match the password above.";
  }

  return "";
}

registerForm.addEventListener("submit", async (event) => {
  event.preventDefault();

  const payload = {
    fullName: normalizeFullName(fullNameInput && fullNameInput.value),
    email: normalizeEmail(emailInput && emailInput.value),
    password: passwordInput ? passwordInput.value : "",
    role: roleSelect ? roleSelect.value : ""
  };

  const validationMessage = validateRegistrationPayload(payload);
  if (validationMessage) {
    AppUi.setMessage(messageBox, validationMessage);
    AppUi.showToast(validationMessage, "error");
    updatePasswordFeedback();
    return;
  }

  AppUi.setLoading(registerBtn, true, "Creating account...");

  try {
    const res = await AppApi.request("/api/v1/auth/register", {
      method: "POST",
      auth: false,
      body: payload
    });

    AppApi.setToken(res.data.token);
    AppUi.setMessage(messageBox, "Account created. Redirecting to your technical readiness setup...");
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
