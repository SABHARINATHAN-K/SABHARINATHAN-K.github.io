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

## API Base URL
- `http://localhost:8080/api/v1`

## Endpoints
- `POST /auth/register`
- `POST /auth/login`
- `GET /users/me` (header: `X-Auth-Token`)
- `POST /goals` (header: `X-Auth-Token`)
- `GET /goals` (header: `X-Auth-Token`)
- `PUT /goals/{goalId}` (header: `X-Auth-Token`)
- `DELETE /goals/{goalId}` (header: `X-Auth-Token`)
- `GET /lookups/roles`
- `GET /lookups/career-tracks`
- `GET /lookups/goal-templates`

## Register Request Body
```json
{
  "fullName": "Sabhari",
  "email": "sabhari@example.com",
  "password": "password123",
  "role": "STUDENT",
  "careerTrack": "JAVA_BACKEND_DEVELOPER"
}
```
