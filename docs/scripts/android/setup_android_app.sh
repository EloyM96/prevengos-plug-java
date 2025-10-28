#!/usr/bin/env bash
#
# Script de apoyo para equipos operativos. Permite registrar el endpoint del hub PRL
# en la app Android y lanzar una compilación básica usando Gradle Wrapper.
#
set -euo pipefail

show_help() {
    cat <<'USAGE'
Uso: setup_android_app.sh --base-url https://servidor.local:8443/api [opciones]

Opciones disponibles:
  --base-url <url>        URL pública del hub PRL. Debe incluir protocolo (http/https).
  --build-type <tipo>     Tipo de build Gradle a ejecutar (assembleRelease o assembleDebug).
                          Por defecto no se lanza compilación automática.
  --run-build             Ejecuta el build indicado tras registrar la URL.
  --gradle-args "..."     Parámetros adicionales a pasar a Gradle (opcional).
  -h, --help              Muestra esta ayuda.
USAGE
}

BASE_URL=""
RUN_BUILD=false
BUILD_TASK=""
GRADLE_ARGS=""

while [[ $# -gt 0 ]]; do
    case "$1" in
        --base-url)
            BASE_URL="$2"
            shift 2
            ;;
        --build-type)
            BUILD_TASK="$2"
            shift 2
            ;;
        --run-build)
            RUN_BUILD=true
            shift
            ;;
        --gradle-args)
            GRADLE_ARGS="$2"
            shift 2
            ;;
        -h|--help)
            show_help
            exit 0
            ;;
        *)
            echo "Opción desconocida: $1" >&2
            show_help
            exit 1
            ;;
    esac
done

if [[ -z "$BASE_URL" ]]; then
    echo "Error: es obligatorio indicar --base-url" >&2
    show_help
    exit 1
fi

if [[ -z "$BUILD_TASK" && "$RUN_BUILD" == true ]]; then
    BUILD_TASK="assembleRelease"
fi

if [[ -n "$BUILD_TASK" && "$BUILD_TASK" != assembleRelease && "$BUILD_TASK" != assembleDebug ]]; then
    echo "Error: --build-type solo admite assembleRelease o assembleDebug" >&2
    exit 1
fi

# Normaliza la URL para garantizar la barra final
if [[ "$BASE_URL" != */ ]]; then
    BASE_URL="${BASE_URL}/"
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
GRADLE_PROPERTIES="${REPO_ROOT}/gradle.properties"

TMP_FILE="${GRADLE_PROPERTIES}.tmp"
if [[ -f "$GRADLE_PROPERTIES" ]]; then
    grep -v '^prevengosApiBaseUrl=' "$GRADLE_PROPERTIES" > "$TMP_FILE" || true
else
    : > "$TMP_FILE"
fi

printf 'prevengosApiBaseUrl=%s\n' "$BASE_URL" >> "$TMP_FILE"

mv "$TMP_FILE" "$GRADLE_PROPERTIES"

echo "✔ URL registrada en ${GRADLE_PROPERTIES}" 
cat <<INFO

Resumen de configuración:
  - Endpoint hub: ${BASE_URL}
  - Build Gradle: ${BUILD_TASK:-(no se ejecuta)}
INFO

if [[ "$RUN_BUILD" == true ]]; then
    pushd "$REPO_ROOT" > /dev/null
    echo "Ejecutando ./gradlew ${BUILD_TASK} ${GRADLE_ARGS}" 
    ./gradlew ${BUILD_TASK} ${GRADLE_ARGS}
    popd > /dev/null
else
    echo "Siguiente paso: ejecutar ./gradlew ${BUILD_TASK:-assembleRelease} cuando quieras generar el APK."
fi
