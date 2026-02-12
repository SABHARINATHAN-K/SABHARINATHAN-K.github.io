# Career Planning System

Career Planning System is a full-stack web application to help learners define, track, and complete career goals based on role and career-track preferences.

## Tech Stack
- Frontend: HTML, CSS, Vanilla JavaScript (multi-page UI)
- Backend: Java 21, Spring Boot 3, Spring Data JPA, Flyway
- Database: MySQL 8
- Tooling: Maven, Bash scripts, Git/GitHub workflow

## Repository Structure
```text
CAREER PLANNING/
├── BackEnd/          # Spring Boot REST API
├── FrontEnd/         # Static client (HTML/CSS/JS)
├── docs/             # Architecture and frontend reference docs
└── README.md
```

## Prerequisites
- Java 21+
- Maven 3.9+
- MySQL 8+
- Python 3 (for local static frontend server)

## Installation & Run Steps
1. Clone the repository and move into project root:
```bash
cd "/home/sabhari/VS/CAREER PLANNING/CAREER PLANNING"
```

2. Configure backend environment:
```bash
cd BackEnd
cp .env.example .env
```
Edit `.env` values if required (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `PORT`).

3. Start backend:
```bash
./run.sh
```
Backend URL: `http://localhost:8080/api/v1`

4. Start frontend in another terminal:
```bash
cd "/home/sabhari/VS/CAREER PLANNING/CAREER PLANNING/FrontEnd"
./run.sh
```
Frontend URL: `http://localhost:5500`

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
