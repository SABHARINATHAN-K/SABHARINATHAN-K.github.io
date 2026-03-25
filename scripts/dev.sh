#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUN_DIR="$ROOT_DIR/.run"
DEV_ENV_FILE="$ROOT_DIR/.dev.env"

BACKEND_DIR="$ROOT_DIR/BackEnd"
FRONTEND_DIR="$ROOT_DIR/FrontEnd"

BACKEND_PID_FILE="$RUN_DIR/backend.pid"
FRONTEND_PID_FILE="$RUN_DIR/frontend.pid"
BACKEND_LOG="$RUN_DIR/backend.log"
FRONTEND_LOG="$RUN_DIR/frontend.log"

BACKEND_PORT=8081
FRONTEND_PORT=5500
BACKEND_READY_TIMEOUT=180
FRONTEND_READY_TIMEOUT=20
DB_URL="jdbc:mysql://localhost:3306/career_planning_system?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
DB_USERNAME="sabhari"
DB_PASSWORD="ss"

load_env() {
  if [[ -f "$DEV_ENV_FILE" ]]; then
    set -a
    # shellcheck disable=SC1090
    source "$DEV_ENV_FILE"
    set +a
  fi

  BACKEND_URL="http://localhost:${BACKEND_PORT}"
  FRONTEND_URL="http://localhost:${FRONTEND_PORT}"
}

ensure_run_dir() {
  mkdir -p "$RUN_DIR"
}

require_command() {
  local command_name="$1"
  if ! command -v "$command_name" >/dev/null 2>&1; then
    echo "Missing required command: $command_name"
    return 1
  fi
}

is_pid_running() {
  local pid="$1"
  [[ -n "$pid" ]] && kill -0 "$pid" 2>/dev/null
}

read_pid() {
  local pid_file="$1"
  if [[ -f "$pid_file" ]]; then
    cat "$pid_file"
  fi
}

port_listener_pid() {
  local port="$1"
  if command -v lsof >/dev/null 2>&1; then
    lsof -t -iTCP:"$port" -sTCP:LISTEN 2>/dev/null | head -n 1 || true
  else
    echo ""
  fi
}

wait_for_url() {
  local url="$1"
  local timeout_seconds="$2"
  local elapsed=0

  while (( elapsed < timeout_seconds )); do
    if curl -fsS "$url" >/dev/null 2>&1; then
      return 0
    fi
    sleep 1
    elapsed=$((elapsed + 1))
  done

  return 1
}

start_backend() {
  require_command mvn
  require_command curl

  local existing_pid
  existing_pid="$(read_pid "$BACKEND_PID_FILE")"

  if is_pid_running "$existing_pid"; then
    echo "Backend already running on $BACKEND_URL (PID $existing_pid)."
    return 0
  fi

  local listener_pid
  listener_pid="$(port_listener_pid "$BACKEND_PORT")"
  if [[ -n "$listener_pid" ]]; then
    echo "Port $BACKEND_PORT is already in use by PID $listener_pid. Stop it or change BACKEND_PORT in .dev.env."
    return 1
  fi

  : > "$BACKEND_LOG"

  (
    cd "$BACKEND_DIR"
    export DB_URL DB_USERNAME DB_PASSWORD
    export PORT="$BACKEND_PORT"
    nohup mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dserver.port=$BACKEND_PORT" >> "$BACKEND_LOG" 2>&1 &
    echo $! > "$BACKEND_PID_FILE"
  )

  echo "Starting backend on $BACKEND_URL ..."
  if wait_for_url "$BACKEND_URL/api/v1/lookups/roles" "$BACKEND_READY_TIMEOUT"; then
    echo "Backend is ready: $BACKEND_URL"
  else
    echo "Backend did not become ready in ${BACKEND_READY_TIMEOUT}s. Check logs: $BACKEND_LOG"
    return 1
  fi
}

start_frontend() {
  require_command python3
  require_command curl

  local existing_pid
  existing_pid="$(read_pid "$FRONTEND_PID_FILE")"

  if is_pid_running "$existing_pid"; then
    echo "Frontend already running on $FRONTEND_URL (PID $existing_pid)."
    return 0
  fi

  local listener_pid
  listener_pid="$(port_listener_pid "$FRONTEND_PORT")"
  if [[ -n "$listener_pid" ]]; then
    echo "Port $FRONTEND_PORT is already in use by PID $listener_pid. Stop it or change FRONTEND_PORT in .dev.env."
    return 1
  fi

  : > "$FRONTEND_LOG"

  (
    cd "$FRONTEND_DIR"
    nohup python3 -m http.server "$FRONTEND_PORT" >> "$FRONTEND_LOG" 2>&1 &
    echo $! > "$FRONTEND_PID_FILE"
  )

  echo "Starting frontend on $FRONTEND_URL ..."
  if wait_for_url "$FRONTEND_URL" "$FRONTEND_READY_TIMEOUT"; then
    echo "Frontend is ready: $FRONTEND_URL"
  else
    echo "Frontend did not become ready in ${FRONTEND_READY_TIMEOUT}s. Check logs: $FRONTEND_LOG"
    return 1
  fi
}

stop_service() {
  local name="$1"
  local pid_file="$2"

  local pid
  pid="$(read_pid "$pid_file")"

  if ! is_pid_running "$pid"; then
    rm -f "$pid_file"
    echo "$name is not running."
    return 0
  fi

  echo "Stopping $name (PID $pid) ..."
  kill "$pid" 2>/dev/null || true

  local waited=0
  while is_pid_running "$pid" && (( waited < 10 )); do
    sleep 1
    waited=$((waited + 1))
  done

  if is_pid_running "$pid"; then
    echo "$name did not stop gracefully. Sending SIGKILL ..."
    kill -9 "$pid" 2>/dev/null || true
  fi

  rm -f "$pid_file"
  echo "$name stopped."
}

show_status() {
  local backend_pid frontend_pid
  backend_pid="$(read_pid "$BACKEND_PID_FILE")"
  frontend_pid="$(read_pid "$FRONTEND_PID_FILE")"

  if is_pid_running "$backend_pid"; then
    echo "Backend : RUNNING (PID $backend_pid) -> $BACKEND_URL"
  else
    echo "Backend : STOPPED"
  fi

  if is_pid_running "$frontend_pid"; then
    echo "Frontend: RUNNING (PID $frontend_pid) -> $FRONTEND_URL"
  else
    echo "Frontend: STOPPED"
  fi

  echo "Logs:"
  echo "  Backend : $BACKEND_LOG"
  echo "  Frontend: $FRONTEND_LOG"
}

show_logs() {
  ensure_run_dir
  touch "$BACKEND_LOG" "$FRONTEND_LOG"
  tail -n 100 -f "$BACKEND_LOG" "$FRONTEND_LOG"
}

show_help() {
  cat <<USAGE
Usage: ./dev.sh <command>

Commands:
  up        Start backend and frontend
  down      Stop backend and frontend
  restart   Restart backend and frontend
  status    Show process status and URLs
  logs      Follow backend and frontend logs

Optional config file:
  .dev.env (copy from .dev.env.example)
  Supports BACKEND_READY_TIMEOUT and FRONTEND_READY_TIMEOUT in seconds.
USAGE
}

main() {
  load_env
  ensure_run_dir

  local command="${1:-help}"

  case "$command" in
    up)
      start_backend
      start_frontend
      show_status
      ;;
    down)
      stop_service "Frontend" "$FRONTEND_PID_FILE"
      stop_service "Backend" "$BACKEND_PID_FILE"
      ;;
    restart)
      stop_service "Frontend" "$FRONTEND_PID_FILE"
      stop_service "Backend" "$BACKEND_PID_FILE"
      start_backend
      start_frontend
      show_status
      ;;
    status)
      show_status
      ;;
    logs)
      show_logs
      ;;
    help|--help|-h)
      show_help
      ;;
    *)
      echo "Unknown command: $command"
      show_help
      return 1
      ;;
  esac
}

main "$@"
