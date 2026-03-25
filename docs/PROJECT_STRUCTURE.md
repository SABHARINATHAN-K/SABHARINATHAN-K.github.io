# Project Structure Guide

This project keeps backend, frontend, and developer tooling separate so daily work stays predictable.

## Top Level

```text
CAREER PLANNING SYSTEM/
├── BackEnd/
├── FrontEnd/
├── scripts/
├── docs/
├── dev.sh
├── .dev.env.example
└── Makefile
```

## Backend (`BackEnd/`)

- `src/main/java/com/careerplanning/backend/common` shared API response and exception handling.
- `src/main/java/com/careerplanning/backend/config` app-level config (CORS, etc.).
- `src/main/java/com/careerplanning/backend/modules/*` feature modules (auth, users, goals, career, lookups).
- `src/main/resources/db/migration` Flyway migrations.
- `src/test/java/.../modules/*` module-oriented tests.

## Frontend (`FrontEnd/`)

- `index.html` public entry page.
- `pages/*.html` authenticated and onboarding pages.
- `assets/css/styles.css` shared design system and page styles.
- `assets/js/core/` shared runtime pieces:
  - `config.js`
  - `ui.js`
  - `api.js`
  - `auth-guard.js`
- `assets/js/pages/` page-specific logic:
  - `index.js`, `login.js`, `register.js`, `dashboard.js`, `goals.js`, `goal-detail.js`, `profile.js`, `analytics.js`, `career-discovery.js`.

## Scripts (`scripts/`)

- `scripts/dev.sh` unified local process manager (`up/down/status/logs/restart`).
- `scripts/backend.sh` backend-only launcher.
- `scripts/frontend.sh` frontend-only launcher.
- Root `dev.sh` stays as a compatibility wrapper that calls `scripts/dev.sh`.

## Local Run Files

- `.dev.env.example` template for local ports and DB credentials.
- `.dev.env` ignored local override file.
- `.run/` generated PID/log files for local services.

