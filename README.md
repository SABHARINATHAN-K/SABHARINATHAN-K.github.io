# Career Planning System

Career Planning System is a full-stack web application to help learners define, track, and complete career goals based on role and career-track preferences.

## Tech Stack
- Frontend: HTML, CSS, Vanilla JavaScript (multi-page UI)
- Backend: Java 21, Spring Boot 3.4.2, Spring Data JPA, Flyway
- Database: MySQL 8
- Tooling: Maven, Bash scripts, Git/GitHub workflow

## Repository Structure
```text
CAREER PLANNING/
├── BackEnd/          # Spring Boot REST API (source, tests, migrations)
├── FrontEnd/         # Static client (pages, CSS, JS)
├── scripts/          # Unified project scripts
├── docs/             # Architecture and references
├── dev.sh            # Backward-compatible launcher -> scripts/dev.sh
├── Makefile          # Shortcut commands (make up/down/status/logs)
└── README.md
```

## Prerequisites
- Java 21+
- Maven 3.9+
- MySQL 8+
- Python 3 (for local static frontend server)

## Quick Start
1. Move into project root:
```bash
cd "/home/sabhari/VS/CAREER PLANNING/CAREER PLANNING SYSTEM"
```

2. Configure ports and DB credentials once:
```bash
cp .dev.env.example .dev.env
```

3. Start both backend and frontend together:
```bash
./dev.sh up
```

4. Open the app:
- Frontend: `http://localhost:5500`
- Backend API: `http://localhost:8081/api/v1`

You only need to open the frontend URL in your browser for normal use.

## Daily Commands
```bash
./dev.sh status   # check both services + URLs
./dev.sh logs     # follow backend + frontend logs
./dev.sh down     # stop both services
./dev.sh restart  # restart both services
```

## Google Sign-In Setup
Google sign-in is available at `POST /api/v1/auth/google` and the login page.

1. Create a Google OAuth Client ID (Web application) in Google Cloud Console.
2. Add backend config in `.dev.env`:
```bash
GOOGLE_AUTH_CLIENT_ID=your_google_web_client_id.apps.googleusercontent.com
```
3. Set frontend client ID once in browser console (for local static frontend):
```js
localStorage.setItem("career_planning_google_client_id", "your_google_web_client_id.apps.googleusercontent.com");
```
4. Restart services and hard refresh login page:
```bash
./dev.sh restart
```
Open `http://localhost:5500/pages/login.html` and use `Sign in with Google`.

Optional `make` shortcuts:
```bash
make up
make status
make down
```

## Legacy Per-Service Run (Optional)
- Backend only: `./scripts/backend.sh`
- Frontend only: `./scripts/frontend.sh`

## Core API Endpoints
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `GET /api/v1/users/me`
- `POST /api/v1/goals`
- `GET /api/v1/goals`
- `PUT /api/v1/goals/{goalId}`
- `DELETE /api/v1/goals/{goalId}`
- `GET /api/v1/lookups/roles`
- `GET /api/v1/lookups/career-tracks`
- `GET /api/v1/lookups/goal-templates`

## Branching Strategy
- `main`: stable, review/demo-ready branch.
- `develop`: integration branch for tested feature branches.
- `feature/<name>`: individual tasks (UI, auth, goals, docs, etc.).
- `hotfix/<name>`: urgent fixes directly from `main`.

Suggested workflow:
1. Create `feature/*` from `develop`.
2. Open PR to `develop` after local testing.
3. Merge `develop` to `main` only for release/review milestones.

## Documentation
- `docs/ARCHITECTURE.md`
- `docs/FRONTEND_REFERENCE.md`
- `docs/PROJECT_STRUCTURE.md`
