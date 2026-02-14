#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

# Optional local override: copy .env.example to .env and edit values.
if [[ -f .env ]]; then
  set -a
  # shellcheck disable=SC1091
  source .env
  set +a
fi

export DB_URL="${DB_URL:-jdbc:mysql://localhost:3306/career_planning_system?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC}"
export DB_USERNAME="${DB_USERNAME:-sabhari}"
export DB_PASSWORD="${DB_PASSWORD:-ss}"
export PORT="${PORT:-8081}"

mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dserver.port=$PORT"
