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
    } else {
      button.textContent = button.dataset.originalText || button.textContent;
      button.disabled = false;
    }
  }

  function humanize(value) {
    return String(value || "")
      .toLowerCase()
      .split("_")
      .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
      .join(" ");
  }

  function showToast(message) {
    const toast = document.getElementById("toast");
    if (!toast) {
      return;
    }

    toast.textContent = message;
    toast.classList.add("show");

    clearTimeout(toast._timer);
    toast._timer = setTimeout(() => {
      toast.classList.remove("show");
    }, 1800);
  }

  window.AppUi = {
    setMessage,
    setLoading,
    humanize,
    showToast
  };
})();
