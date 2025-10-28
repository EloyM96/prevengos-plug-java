#!/bin/bash
set -euo pipefail

if [[ -z "${MSSQL_SA_PASSWORD:-}" ]]; then
  echo "MSSQL_SA_PASSWORD must be set" >&2
  exit 1
fi

wait_for_sqlserver() {
  local retries=60
  until /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P "$MSSQL_SA_PASSWORD" -Q "SELECT 1" >/dev/null 2>&1; do
    sleep 1
    retries=$((retries - 1))
    if [[ $retries -le 0 ]]; then
      echo "SQL Server did not become ready in time" >&2
      exit 1
    fi
  done
}

create_database() {
  if [[ -z "${MSSQL_DB:-}" ]]; then
    return
  fi

  /opt/mssql-tools/bin/sqlcmd \
    -S localhost \
    -U sa \
    -P "$MSSQL_SA_PASSWORD" \
    -d master \
    -Q "IF DB_ID('${MSSQL_DB}') IS NULL BEGIN CREATE DATABASE [${MSSQL_DB}]; END"
}

provision_app_user() {
  if [[ -z "${MSSQL_APP_USER:-}" || -z "${MSSQL_APP_PASSWORD:-}" || -z "${MSSQL_DB:-}" ]]; then
    return
  fi

  /opt/mssql-tools/bin/sqlcmd \
    -S localhost \
    -U sa \
    -P "$MSSQL_SA_PASSWORD" \
    -d master \
    -Q "IF NOT EXISTS (SELECT 1 FROM sys.sql_logins WHERE name = '${MSSQL_APP_USER}') BEGIN CREATE LOGIN [${MSSQL_APP_USER}] WITH PASSWORD=N'${MSSQL_APP_PASSWORD}'; END"

  /opt/mssql-tools/bin/sqlcmd \
    -S localhost \
    -U sa \
    -P "$MSSQL_SA_PASSWORD" \
    -d "${MSSQL_DB}" \
    -Q "IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = '${MSSQL_APP_USER}') BEGIN CREATE USER [${MSSQL_APP_USER}] FOR LOGIN [${MSSQL_APP_USER}]; END; ALTER ROLE db_owner ADD MEMBER [${MSSQL_APP_USER}];"
}

wait_for_sqlserver
create_database
provision_app_user
