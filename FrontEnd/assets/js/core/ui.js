(function () {
  let revealObserver = null;

  function ensureLegacyGoalDetailCompat() {
    const body = document.body;
    if (!body) {
      return;
    }

    let legacyCompatRoot = document.getElementById("legacyGoalDetailCompatRoot");
    if (!legacyCompatRoot) {
      legacyCompatRoot = document.createElement("div");
      legacyCompatRoot.id = "legacyGoalDetailCompatRoot";
      legacyCompatRoot.style.display = "none";
      legacyCompatRoot.setAttribute("aria-hidden", "true");
      body.appendChild(legacyCompatRoot);
    }

    const ensureElement = (id, tagName, attributes) => {
      if (document.getElementById(id)) {
        return;
      }

      const element = document.createElement(tagName);
      element.id = id;
      Object.entries(attributes || {}).forEach(([key, value]) => {
        if (key === "textContent") {
          element.textContent = String(value);
        } else {
          element.setAttribute(key, String(value));
        }
      });
      legacyCompatRoot.appendChild(element);
    };

    ensureElement("messageBox", "div");
    ensureElement("progressRange", "input", { type: "range", min: "0", max: "100", value: "0" });
    ensureElement("progressValue", "strong", { textContent: "0%" });
    ensureElement("saveProgressBtn", "button", { type: "button", textContent: "Save Progress" });
  }

  function ensureRevealObserver() {
    if (revealObserver || typeof IntersectionObserver === "undefined") {
      return;
    }

    revealObserver = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (!entry.isIntersecting) {
            return;
          }
          entry.target.classList.add("reveal-in");
          revealObserver.unobserve(entry.target);
        });
      },
      { threshold: 0.08, rootMargin: "0px 0px -30px 0px" }
    );
  }

  function applyReveal(root) {
    const container = root || document;
    const selectors = [
      ".hero-card",
      ".section-card",
      ".panel",
      ".stat-card",
      ".workflow-card",
      ".goal-card",
      ".goal-item",
      ".home-shortcut-card",
      ".choice-card",
      ".result-card",
      ".result-highlight",
      ".template-item",
      ".timeline-item",
      ".warning-item",
      ".task-item"
    ];

    const elements = container.querySelectorAll(selectors.join(","));
    if (!elements.length) {
      return;
    }

    ensureRevealObserver();

    elements.forEach((element) => {
      if (element.dataset.revealBound === "true") {
        return;
      }
      element.dataset.revealBound = "true";
      element.classList.add("reveal-ready");
      if (revealObserver) {
        revealObserver.observe(element);
      } else {
        element.classList.add("reveal-in");
      }
    });
  }

  document.addEventListener("DOMContentLoaded", () => {
    ensureLegacyGoalDetailCompat();
    applyReveal(document);
  });

  ensureLegacyGoalDetailCompat();

  function setMessage(element, text) {
    if (element) {
      element.textContent = text;
    }
  }

  function setLoading(button, isLoading, loadingText) {
    if (!button) {
      return;
    }

    if (isLoading) {
      button.dataset.originalText = button.textContent;
      button.textContent = loadingText || "Loading...";
      button.disabled = true;
      button.classList.add("is-loading");
    } else {
      button.textContent = button.dataset.originalText || button.textContent;
      button.disabled = false;
      button.classList.remove("is-loading");
    }
  }

  function humanize(value) {
    return String(value || "")
      .toLowerCase()
      .split("_")
      .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
      .join(" ");
  }

  function escapeHtml(value) {
    return String(value || "")
      .replaceAll("&", "&amp;")
      .replaceAll("<", "&lt;")
      .replaceAll(">", "&gt;")
      .replaceAll('"', "&quot;")
      .replaceAll("'", "&#039;");
  }

  function escapeAttr(value) {
    return escapeHtml(value).replaceAll("`", "");
  }

  function formatDate(value) {
    if (!value) {
      return "Not set";
    }
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
      return "Not set";
    }
    return date.toLocaleDateString();
  }

  function toInputDate(value) {
    if (!value) {
      return "";
    }
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
      return "";
    }
    return date.toISOString().slice(0, 10);
  }

  function parseTagText(value) {
    if (!value) {
      return [];
    }
    return value
      .split(",")
      .map((part) => part.trim())
      .filter((part) => part.length > 0);
  }

  function showToast(message, type) {
    const toast = document.getElementById("toast");
    if (!toast) {
      return;
    }

    toast.textContent = message;
    toast.classList.remove("success", "error");
    toast.classList.add(type === "error" ? "error" : "success", "show");

    clearTimeout(toast._timer);
    toast._timer = setTimeout(() => {
      toast.classList.remove("show");
    }, 2200);
  }

  window.AppUi = {
    setMessage,
    setLoading,
    humanize,
    escapeHtml,
    escapeAttr,
    formatDate,
    toInputDate,
    parseTagText,
    showToast,
    applyReveal
  };
})();
