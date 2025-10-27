#!/usr/bin/env bash
set -euo pipefail

required=("POSTGRES_SUPERUSER" "APP_DB_NAME" "APP_DB_USER" "APP_DB_PASSWORD")
for var in "${required[@]}"; do
  if [[ -z "${!var:-}" ]]; then
    echo "[init] Variable ${var} no definida" >&2
    exit 1
  fi
done

psql -v ON_ERROR_STOP=1 --username "${POSTGRES_SUPERUSER}" --dbname postgres <<EOSQL
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = '${APP_DB_USER}') THEN
        EXECUTE format('CREATE ROLE %I LOGIN PASSWORD %L', '${APP_DB_USER}', '${APP_DB_PASSWORD}');
    ELSE
        EXECUTE format('ALTER ROLE %I WITH PASSWORD %L', '${APP_DB_USER}', '${APP_DB_PASSWORD}');
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = '${APP_DB_NAME}') THEN
        EXECUTE format('CREATE DATABASE %I OWNER %I', '${APP_DB_NAME}', '${APP_DB_USER}');
    END IF;
END
$$;

GRANT ALL PRIVILEGES ON DATABASE ${APP_DB_NAME} TO ${APP_DB_USER};
ALTER ROLE ${APP_DB_USER} SET search_path TO public;
EOSQL
