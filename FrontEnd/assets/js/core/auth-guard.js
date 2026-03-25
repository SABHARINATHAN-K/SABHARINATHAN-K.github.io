(function () {
  function getToken() {
    return window.AppApi && typeof window.AppApi.getToken === "function"
      ? window.AppApi.getToken()
      : "";
  }

  function currentPath() {
    return String(window.location.pathname || "").toLowerCase();
  }

  function currentFileName() {
    const parts = currentPath().split("/").filter(Boolean);
    return parts.length ? parts[parts.length - 1] : "index.html";
  }

  function buildPageHref(pageName) {
    return currentPath().includes("/pages/") ? `./${pageName}` : `./pages/${pageName}`;
  }

  function isAdminProfile(profile) {
    return String(profile && profile.role ? profile.role : "").toUpperCase() === "ADMIN";
  }

  function getLandingPage(profile) {
    if (isAdminProfile(profile)) {
      return "admin.html";
    }
    return profile && profile.onboardingCompleted === false
      ? "technical-readiness.html"
      : "home.html";
  }

  function redirectToLanding(profile) {
    window.location.href = buildPageHref(getLandingPage(profile));
  }

  function isPublicPage(fileName) {
    return ["index.html", "login.html", "register.html"].includes(fileName);
  }

  function isLegacyReadinessAlias(fileName) {
    return fileName === "career-discovery.html" || fileName === "onboarding.html";
  }

  function isReadinessPage(fileName) {
    return fileName === "technical-readiness.html" || isLegacyReadinessAlias(fileName);
  }

  function isAdminPage(fileName) {
    return fileName === "admin.html" || fileName === "admin-user.html";
  }

  window.AppSession = {
    buildPageHref,
    isAdminProfile,
    getLandingPage,
    redirectToLanding
  };

  const token = getToken();
  if (!token || !window.AppApi || typeof window.AppApi.getMe !== "function") {
    return;
  }

  const fileName = currentFileName();
  if (isPublicPage(fileName)) {
    return;
  }

  window.AppApi.getMe()
    .then((profile) => {
      if (isAdminProfile(profile)) {
        if (!isAdminPage(fileName)) {
          redirectToLanding(profile);
        }
        return;
      }

      if (isAdminPage(fileName)) {
        redirectToLanding(profile);
        return;
      }

      if (profile && profile.onboardingCompleted === false && !isReadinessPage(fileName)) {
        window.location.href = buildPageHref("technical-readiness.html");
        return;
      }

      if (profile && profile.onboardingCompleted === true && isLegacyReadinessAlias(fileName)) {
        window.location.href = buildPageHref("technical-readiness.html");
      }
    })
    .catch(() => {
      // Ignore guard failures and let page-level handlers manage auth errors.
    });
})();
