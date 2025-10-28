#!/usr/bin/env bash
set -euo pipefail

# Variables de entorno esperadas
: "${HUB_OUTBOX:?Debe definir HUB_OUTBOX con la carpeta de salida del hub}" 
: "${PREVENGOS_DROP:?Debe definir PREVENGOS_DROP con la carpeta o URL remota}" 
: "${TRACE_ID:=manual}" 

STAMP=$(date +%Y%m%d)
WORKDIR="${HUB_OUTBOX}/${STAMP}/rrhh/hub"
LOGFILE="${HUB_OUTBOX}/../logs/export_rrhh.log"

if [[ ! -d "${WORKDIR}" ]]; then
  echo "[$(date --iso-8601=seconds)] [${TRACE_ID}] No existe ${WORKDIR}" | tee -a "${LOGFILE}"
  exit 1
fi

echo "[$(date --iso-8601=seconds)] [${TRACE_ID}] Generando checksums" | tee -a "${LOGFILE}"
find "${WORKDIR}" -maxdepth 1 -type f -name '*.csv' -print0 | while IFS= read -r -d '' file; do
  sha256sum "${file}" > "${file}.sha256"
done

PACKAGE="${WORKDIR}/rrhh_${STAMP}_${TRACE_ID}.tar.gz"

echo "[$(date --iso-8601=seconds)] [${TRACE_ID}] Empaquetando CSV en ${PACKAGE}" | tee -a "${LOGFILE}"
tar -czf "${PACKAGE}" -C "${WORKDIR}" .

echo "[$(date --iso-8601=seconds)] [${TRACE_ID}] Transfiriendo a ${PREVENGOS_DROP}" | tee -a "${LOGFILE}"
if [[ "${PREVENGOS_DROP}" == sftp:* ]]; then
  DEST=${PREVENGOS_DROP#sftp:}
  scp "${PACKAGE}" "${DEST}"
else
  rsync -av "${PACKAGE}" "${PREVENGOS_DROP}/"
fi

echo "[$(date --iso-8601=seconds)] [${TRACE_ID}] Transferencia completada" | tee -a "${LOGFILE}"
