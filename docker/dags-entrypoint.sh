#!/bin/sh
set -eu

POSTGRES_DB="${POSTGRES_DB:-dags}"
POSTGRES_USER="${POSTGRES_USER:-dags}"
POSTGRES_PASSWORD="${POSTGRES_PASSWORD:-dags}"
POSTGRES_PORT="${POSTGRES_PORT:-5432}"
POSTGRES_LISTEN_ADDRESSES="${POSTGRES_LISTEN_ADDRESSES:-0.0.0.0}"
PGDATA="${PGDATA:-/var/lib/postgresql/data}"

mkdir -p "$PGDATA" /var/run/postgresql /var/log/supervisor
chown -R postgres:postgres "$PGDATA" /var/run/postgresql /var/log/supervisor
chmod 700 "$PGDATA"

if [ ! -s "$PGDATA/PG_VERSION" ]; then
  su postgres -s /bin/sh -c "initdb -D '$PGDATA' --auth-local=trust --auth-host=scram-sha-256"
fi

if ! grep -q "^port = ${POSTGRES_PORT}$" "$PGDATA/postgresql.conf" 2>/dev/null; then
  {
    printf "\nlisten_addresses = '%s'\n" "$POSTGRES_LISTEN_ADDRESSES"
    printf "port = %s\n" "$POSTGRES_PORT"
  } >> "$PGDATA/postgresql.conf"
fi

if ! grep -q '^host all all all scram-sha-256$' "$PGDATA/pg_hba.conf" 2>/dev/null; then
  printf '\nhost all all all scram-sha-256\n' >> "$PGDATA/pg_hba.conf"
fi

sql_escape_literal() {
  printf "%s" "$1" | sed "s/'/''/g"
}

sql_escape_identifier() {
  printf "%s" "$1" | sed 's/"/""/g'
}

escaped_db_literal="$(sql_escape_literal "$POSTGRES_DB")"
escaped_user_literal="$(sql_escape_literal "$POSTGRES_USER")"
escaped_password_literal="$(sql_escape_literal "$POSTGRES_PASSWORD")"
escaped_db_identifier="$(sql_escape_identifier "$POSTGRES_DB")"
escaped_user_identifier="$(sql_escape_identifier "$POSTGRES_USER")"

su postgres -s /bin/sh -c "pg_ctl -D '$PGDATA' -w start"

init_sql="$(mktemp)"
cat > "$init_sql" <<EOF
DO \$\$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = '${escaped_user_literal}') THEN
    CREATE ROLE "${escaped_user_identifier}" LOGIN PASSWORD '${escaped_password_literal}';
  ELSE
    ALTER ROLE "${escaped_user_identifier}" WITH LOGIN PASSWORD '${escaped_password_literal}';
  END IF;
END
\$\$;

SELECT 'CREATE DATABASE "${escaped_db_identifier}" OWNER "${escaped_user_identifier}"'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '${escaped_db_literal}')
\gexec

GRANT ALL PRIVILEGES ON DATABASE "${escaped_db_identifier}" TO "${escaped_user_identifier}";
EOF
chmod 644 "$init_sql"

su postgres -s /bin/sh -c "psql -d postgres -v ON_ERROR_STOP=1 -f '$init_sql'"
rm -f "$init_sql"

su postgres -s /bin/sh -c "psql -d \"${POSTGRES_DB}\" -v ON_ERROR_STOP=1 -c 'CREATE EXTENSION IF NOT EXISTS vector'"

su postgres -s /bin/sh -c "pg_ctl -D '$PGDATA' -m fast -w stop"

exec /usr/bin/supervisord -c /etc/supervisor/conf.d/dags.conf
