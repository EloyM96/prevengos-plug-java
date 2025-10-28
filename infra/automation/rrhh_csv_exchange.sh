#!/usr/bin/env bash
set -euo pipefail

HUB_BASE_URL=${HUB_BASE_URL:-http://localhost:8080}
LOGGER_TAG=${LOGGER_TAG:-prevengos-rrhh}
CURL_TIMEOUT=${CURL_TIMEOUT:-30}

AUTH_HEADER=()
if [[ -n "${HUB_API_TOKEN:-}" ]]; then
  AUTH_HEADER=(-H "Authorization: Bearer ${HUB_API_TOKEN}")
fi

log() {
  local level=$1
  shift
  local message="$*"
  printf '%s %s\n' "$level" "$message"
  if command -v logger >/dev/null 2>&1; then
    logger -t "$LOGGER_TAG" "[$level] $message"
  fi
}

call_endpoint() {
  local endpoint=$1
  local label=$2
  local tmp
  tmp=$(mktemp)
  trap 'rm -f "$tmp"' RETURN

  log INFO "Invocando ${label} en ${HUB_BASE_URL}/${endpoint}"
  local status
  if ! status=$(curl -sS "${AUTH_HEADER[@]}" -X POST \
      --connect-timeout "$CURL_TIMEOUT" --max-time "$CURL_TIMEOUT" \
      -o "$tmp" -w '%{http_code}' "${HUB_BASE_URL}/${endpoint}"); then
    log ERROR "Fallo de red al llamar ${label}"
    return 1
  fi

  local body
  body=$(cat "$tmp")
  rm -f "$tmp"
  trap - RETURN

  if [[ $status -ge 200 && $status -lt 300 ]]; then
    log INFO "${label} completado (status=${status}): ${body}"
  else
    log ERROR "${label} falló (status=${status}): ${body}"
    return 1
  fi
}

call_endpoint "rrhh/export" "Exportación RRHH"
call_endpoint "rrhh/import" "Importación RRHH"
