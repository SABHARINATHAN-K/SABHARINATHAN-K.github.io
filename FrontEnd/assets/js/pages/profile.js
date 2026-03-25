const logoutBtn = document.getElementById("logoutBtn");
const editBtn = document.getElementById("editBtn");
const cancelBtn = document.getElementById("cancelBtn");
const saveBtn = document.getElementById("saveBtn");
const messageBox = document.getElementById("messageBox");

const fullNameInput = document.getElementById("fullName");
const emailInput = document.getElementById("email");
const bioInput = document.getElementById("bio");
const locationInput = document.getElementById("location");
const roleSelect = document.getElementById("role");
const careerTrackSelect = document.getElementById("careerTrack");

const profileInitial = document.getElementById("profileInitial");
const profileDisplayName = document.getElementById("profileDisplayName");
const profileDisplayEmail = document.getElementById("profileDisplayEmail");
const joinedDateText = document.getElementById("joinedDateText");
const quickRole = document.getElementById("quickRole");
const quickTrack = document.getElementById("quickTrack");

let profile = null;
let editing = false;

if (AppApi.requireAuth()) {
  initProfilePage();
}

logoutBtn.addEventListener("click", () => {
  AppApi.clearToken();
  window.location.href = "./login.html";
});

editBtn.addEventListener("click", () => {
  setEditing(true);
});

cancelBtn.addEventListener("click", () => {
  renderProfile(profile);
  setEditing(false);
});

saveBtn.addEventListener("click", saveProfile);

async function initProfilePage() {
  try {
    const [roles, tracks, me] = await Promise.all([
      AppApi.listRoles(),
      AppApi.listCareerTracks(),
      AppApi.getMe()
    ]);

    setOptions(roleSelect, roles);
    setOptions(careerTrackSelect, tracks);

    profile = me;
    renderProfile(me);
    setEditing(false);
    AppUi.setMessage(messageBox, "Profile loaded.");
  } catch (error) {
    AppUi.setMessage(messageBox, error.message);
    AppUi.showToast(error.message, "error");
  }
}

function setOptions(selectElement, values) {
  selectElement.innerHTML = values
    .map((value) => `<option value="${AppUi.escapeAttr(value)}">${AppUi.escapeHtml(AppUi.humanize(value))}</option>`)
    .join("");
}

function renderProfile(data) {
  if (!data) {
    return;
  }

  fullNameInput.value = data.fullName || "";
  emailInput.value = data.email || "";
  bioInput.value = data.bio || "";
  locationInput.value = data.location || "";
  roleSelect.value = data.role || "";
  careerTrackSelect.value = data.careerTrack || "";

  const initial = (data.fullName || "U").charAt(0).toUpperCase();
  profileInitial.textContent = initial;
  profileDisplayName.textContent = data.fullName || "User";
  profileDisplayEmail.textContent = data.email || "-";
  quickRole.textContent = AppUi.humanize(data.role || "-");
  quickTrack.textContent = AppUi.humanize(data.careerTrack || "-");

  if (data.joinedDate) {
    const joined = new Date(data.joinedDate);
    joinedDateText.textContent = `Joined ${joined.toLocaleDateString(undefined, {
      year: "numeric",
      month: "long",
      day: "numeric"
    })}`;
  } else {
    joinedDateText.textContent = "Joined recently";
  }
}

function setEditing(isEditing) {
  editing = isEditing;

  [fullNameInput, bioInput, locationInput, roleSelect, careerTrackSelect].forEach((field) => {
    field.disabled = !isEditing;
  });

  editBtn.classList.toggle("hidden", isEditing);
  cancelBtn.classList.toggle("hidden", !isEditing);
  saveBtn.classList.toggle("hidden", !isEditing);
}

async function saveProfile() {
  if (!editing) {
    return;
  }

  const payload = {
    fullName: fullNameInput.value.trim(),
    bio: bioInput.value.trim() || null,
    location: locationInput.value.trim() || null,
    role: roleSelect.value,
    careerTrack: careerTrackSelect.value
  };

  AppUi.setLoading(saveBtn, true, "Saving...");

  try {
    const updated = await AppApi.updateMe(payload);
    profile = updated;
    renderProfile(updated);
    setEditing(false);
    AppUi.setMessage(messageBox, "Profile updated successfully.");
    AppUi.showToast("Profile updated");
  } catch (error) {
    AppUi.setMessage(messageBox, error.message);
    AppUi.showToast(error.message, "error");
  } finally {
    AppUi.setLoading(saveBtn, false);
  }
}
