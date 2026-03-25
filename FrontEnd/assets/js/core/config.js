(function () {
  const storedApiBaseUrl = localStorage.getItem("career_planning_api_base_url");
  const storedGoogleClientId = localStorage.getItem("career_planning_google_client_id");
  const hostname = window.location.hostname || "localhost";
  const defaultApiBaseUrl = `http://${hostname}:8081`;
  const defaultGoogleClientId = "";

  window.APP_CONFIG = {
    API_BASE_URL: storedApiBaseUrl || defaultApiBaseUrl,
    GOOGLE_CLIENT_ID: storedGoogleClientId || defaultGoogleClientId
  };
})();
