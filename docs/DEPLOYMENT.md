# Deployment Guide

This project is deployed as two separate services:

- Frontend: Vercel
- Backend: Render
- Database: TiDB Cloud

## 1. Check Locally Before Deploying

From the repository root:

```bash
cd BackEnd
mvn clean package
```

Frontend note:

- The frontend is a static HTML/CSS/JavaScript app in `FrontEnd/`.
- There is no frontend build step.

## 2. Deploy the Backend to Render

Use a Render **Web Service** with the **Docker** runtime.

Recommended Render settings for this repository:

- Root Directory: `BackEnd`
- Dockerfile Path: `Dockerfile`
- Branch: your deployment branch
- Auto-Deploy: enabled

Why this setup:

- `BackEnd/Dockerfile` now builds and runs the Spring Boot app directly from the backend folder.
- The application already reads the HTTP port from `PORT`, which Render provides automatically.

### Required Render Environment Variables

Set these in the Render dashboard:

```text
DB_URL=jdbc:mysql://<tidb-host>:4000/<database>?sslMode=VERIFY_IDENTITY&enabledTLSProtocols=TLSv1.2,TLSv1.3&serverTimezone=UTC
DB_USERNAME=<tidb-username>
DB_PASSWORD=<tidb-password>
```

Optional but recommended:

```text
CORS_ALLOWED_ORIGINS=https://<your-vercel-project>.vercel.app
GOOGLE_AUTH_CLIENT_ID=<your-google-web-client-id>
```

Notes:

- Do not hardcode `PORT`; Render injects it automatically.
- Use the exact TiDB JDBC host, username, password, and database from the TiDB console if they differ from the example above.
- If you use a custom frontend domain later, append it to `CORS_ALLOWED_ORIGINS` as a comma-separated value.

### Backend Smoke Test

After Render finishes deploying, open:

```text
https://<your-render-service>.onrender.com/api/v1/lookups/roles
```

If the backend is healthy, this endpoint should return JSON.

## 3. Deploy the Frontend to Vercel

Use the `FrontEnd/` folder as the Vercel project root.

Recommended Vercel settings:

- Root Directory: `FrontEnd`
- Framework Preset: `Other`
- Build Command: leave empty

Why:

- This frontend is a static site and does not require a bundler or build output directory.

## 4. Confirm the Frontend API Base URL

The frontend reads its API base URL from:

1. `localStorage` override, if present
2. runtime or meta overrides, if present
3. local default during localhost development
4. production fallback in `FrontEnd/assets/js/core/config.js`

If your Render backend URL changes, update the production fallback in:

- `FrontEnd/assets/js/core/config.js`

Then redeploy Vercel.

## 5. Post-Deploy Test Checklist

After both services are live, test in this order:

1. Open the Vercel frontend.
2. Open browser DevTools and confirm `GET /api/v1/lookups/roles` succeeds.
3. Register a new account.
4. Log in with that account.
5. Open the technical readiness page.
6. Create a goal and verify it is saved.
7. Refresh the page and confirm data persists.

## 6. If Deployment Still Fails

Check these first:

- Render build logs: Dockerfile path or startup failure
- Render runtime logs: database connection or Flyway migration failure
- Browser console: JavaScript or CORS errors
- Browser network tab: wrong API base URL or mixed-content error

Most common causes for this repo:

- Vercel project root was not set to `FrontEnd`
- Render service was not set to Docker
- Render service root directory pointed to `BackEnd`, but Dockerfile path was wrong
- `DB_URL` was copied from local MySQL instead of TiDB
- `CORS_ALLOWED_ORIGINS` did not include the Vercel domain
