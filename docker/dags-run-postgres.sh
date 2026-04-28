#!/bin/sh
set -eu

exec /usr/lib/postgresql/17/bin/postgres \
  -D "${PGDATA:-/var/lib/postgresql/data}" \
  -c "listen_addresses=${POSTGRES_LISTEN_ADDRESSES:-0.0.0.0}" \
  -c "port=${POSTGRES_PORT:-5432}"
