# BackEnd

Java Spring Boot REST API for Career Planning.

## Stack
- Java 21
- Spring Boot
- Spring Data JPA
- MySQL
- Flyway

## Project Structure
- `src/main/java/com/careerplanning/backend/modules/auth` - register/login
- `src/main/java/com/careerplanning/backend/modules/users` - profile
- `src/main/java/com/careerplanning/backend/modules/goals` - goal CRUD
- `src/main/java/com/careerplanning/backend/config/WebConfig.java` - CORS for frontend
- `src/main/resources/db/migration` - DB migration SQL

## Run
```bash
cd "/home/sabhari/VS/CAREER PLANNING/CAREER PLANNING/BackEnd"
./run.sh
```

## Environment
`run.sh` supports `.env`-based configuration:
- Copy `.env.example` to `.env`
- Set `PORT`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- If `.env` is missing, safe defaults are applied

## Troubleshooting
If backend startup fails with:
- `Access denied for user '...@localhost' (using password: YES)` (MySQL error `1045`)

Then your configured DB credentials are incorrect for your local MySQL.

Fix:
1. Ensure MySQL is running.
2. Create/update `BackEnd/.env` with valid credentials.
3. Re-run `./run.sh`.

Example:
```bash
cd BackEnd
cp .env.example .env
```

Edit `.env`:
```env
PORT=8080
DB_URL=jdbc:mysql://localhost:3306/career_planning_system?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
DB_USERNAME=<your_mysql_username>
DB_PASSWORD=<your_mysql_password>
```

## API Base URL
- `http://localhost:8080/api/v1`

## Endpoints
- `POST /auth/register`
- `POST /auth/login`
- `GET /users/me` (header: `X-Auth-Token`)
- `PUT /users/me` (header: `X-Auth-Token`)
- `POST /goals` (header: `X-Auth-Token`)
- `GET /goals` (header: `X-Auth-Token`)
- `GET /goals/{goalId}` (header: `X-Auth-Token`)
- `GET /goals/stats` (header: `X-Auth-Token`)
- `PUT /goals/{goalId}` (header: `X-Auth-Token`)
- `DELETE /goals/{goalId}` (header: `X-Auth-Token`)
- `GET /lookups/roles`
- `GET /lookups/career-tracks`
- `GET /lookups/goal-categories`
- `GET /lookups/goal-priorities`
- `GET /lookups/goal-templates`

## Register Request Body
```json
{
  "fullName": "Sabhari",
  "email": "sabhari@example.com",
  "password": "password123",
  "role": "PROFESSIONAL",
  "careerTrack": "SOFTWARE_ENGINEERING"
}
```
