(function () {
  const storedApiBaseUrl = localStorage.getItem("career_planning_api_base_url");
  const storedGoogleClientId = localStorage.getItem("career_planning_google_client_id");
  const hostname = window.location.hostname || "localhost";
  const metaApiBaseUrl = document
    .querySelector("meta[name='career-planning-api-base-url']")
    ?.getAttribute("content")
    ?.trim();
  const metaGoogleClientId = document
    .querySelector("meta[name='career-planning-google-client-id']")
    ?.getAttribute("content")
    ?.trim();
  const runtimeConfig = window.__APP_CONFIG__ || {};
  const isLocalHost = ["localhost", "127.0.0.1"].includes(hostname);
  const defaultApiBaseUrl = isLocalHost
    ? `http://${hostname}:8081`
    : "https://career-planning-backend-izw4.onrender.com";
  const defaultGoogleClientId = "";

  window.APP_CONFIG = {
    API_BASE_URL: storedApiBaseUrl || runtimeConfig.API_BASE_URL || metaApiBaseUrl || defaultApiBaseUrl,
    GOOGLE_CLIENT_ID: storedGoogleClientId || runtimeConfig.GOOGLE_CLIENT_ID || metaGoogleClientId || defaultGoogleClientId
  };
})();
