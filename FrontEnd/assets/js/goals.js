(function () {
  const REFRESH_PARAM = "__refresh_goals";
  const LEGACY_FLAG = "__legacy_goals_bridge_loaded";

  if (window[LEGACY_FLAG]) {
    return;
  }
  window[LEGACY_FLAG] = true;

  function loadScript(src) {
    return new Promise((resolve, reject) => {
      const existing = Array.from(document.scripts).find((script) => script.src === src);
      if (existing) {
        if (existing.dataset.loaded === "true") {
          resolve();
          return;
        }
        existing.addEventListener("load", () => resolve(), { once: true });
        existing.addEventListener("error", () => reject(new Error(`Failed to load ${src}`)), { once: true });
        return;
      }

      const script = document.createElement("script");
      script.src = src;
      script.async = false;
      script.addEventListener("load", () => {
        script.dataset.loaded = "true";
        resolve();
      }, { once: true });
      script.addEventListener("error", () => reject(new Error(`Failed to load ${src}`)), { once: true });
      document.head.appendChild(script);
    });
  }

  function resolve(relativePath) {
    const current = document.currentScript;
    const base = current ? current.src : window.location.href;
    return new URL(relativePath, base).toString();
  }

  async function bootstrap() {
    const hasModernMarkup = Boolean(document.getElementById("goalsGrid")) && Boolean(document.getElementById("newGoalBtn"));
    if (!hasModernMarkup) {
      const url = new URL(window.location.href);
      if (!url.searchParams.has(REFRESH_PARAM)) {
        url.searchParams.set(REFRESH_PARAM, String(Date.now()));
        window.location.replace(url.toString());
        return;
      }
    }

    if (!window.APP_CONFIG) {
      await loadScript(resolve("./core/config.js"));
    }
    if (!window.AppUi) {
      await loadScript(resolve("./core/ui.js"));
    }
    if (!window.AppApi) {
      await loadScript(resolve("./core/api.js"));
    }
    await loadScript(resolve("./core/auth-guard.js"));

    if (!window.__careerGoalsPageLoaded) {
      await loadScript(resolve("./pages/goals.js"));
    }
  }

  bootstrap().catch((error) => {
    console.error("Failed to bootstrap goals compatibility script", error);
  });
})();
