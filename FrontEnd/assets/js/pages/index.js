if (window.AppApi && AppApi.getToken()) {
  routeAuthenticatedUser();
}

async function routeAuthenticatedUser() {
  try {
    const profile = await AppApi.getMe();
    if (window.AppSession && typeof window.AppSession.redirectToLanding === "function") {
      window.AppSession.redirectToLanding(profile);
      return;
    }

    if (profile && String(profile.role || "").toUpperCase() === "ADMIN") {
      window.location.href = "./pages/admin.html";
      return;
    }

    window.location.href = profile && profile.onboardingCompleted === false
      ? "./pages/technical-readiness.html"
      : "./pages/home.html";
  } catch (error) {
    AppApi.clearToken();
  }
}
