(function () {
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
    showToast
  };
})();
