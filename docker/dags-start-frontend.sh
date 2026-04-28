#!/bin/sh
set -eu

BACKEND_PORT="${BACKEND_PORT:-8080}"

export AI_TOOLS_API_BASE_URL="${AI_TOOLS_API_BASE_URL:-http://127.0.0.1:${BACKEND_PORT}/api/v1}"
export AI_TOOLS_API_USERNAME="${AI_TOOLS_API_USERNAME:-${API_BASIC_AUTH_USERNAME:-ai}}"
export AI_TOOLS_API_PASSWORD="${AI_TOOLS_API_PASSWORD:-${API_BASIC_AUTH_PASSWORD:-completelylocal}}"
export HOST="${HOST:-${FRONTEND_HOST:-0.0.0.0}}"
export PORT="${PORT:-${FRONTEND_PORT:-3000}}"
export NITRO_HOST="${NITRO_HOST:-$HOST}"
export NITRO_PORT="${NITRO_PORT:-$PORT}"

exec node /opt/dags/frontend/.output/server/index.mjs
