# Checklist operativo RRHH CSV

Este checklist guía al personal de soporte para verificar el intercambio CSV con
Prevengos antes y después de la ventana nocturna.

## Antes de la ventana (D-1)

1. Confirmar conectividad con el recurso compartido oficial:
   ```bash
   ls -l /var/prevengos/oficial
   ```
2. Validar permisos de escritura/lectura en las carpetas `outgoing/`,
   `incoming/`, `archive/` y `error/`.
3. Revisar que la variable `RRHH_EXPORT_BASE` y compañeras están definidas en el
   `Deployment`/`systemd` correspondiente.
4. Lanzar una llamada manual a `POST /rrhh/export` y confirmar respuesta `202`:
   ```bash
   curl -s -o /dev/null -w "%{http_code}" -X POST https://hub.prevengos.local/rrhh/export
   ```
   Si se genera un drop durante la prueba, moverlo manualmente a
   `archive/validacion/` para no interferir con la ventana nocturna.

## Durante la ventana (03:00 CET)

1. Verificar en los logs (`journalctl -t prevengos-rrhh` o `/var/log/prevengos/rrhh_cron.log`)
   que el script se ejecutó sin errores.
2. Confirmar que existe una carpeta con la fecha actual en
   `/var/prevengos/oficial/outgoing/YYYYMMDD/rrhh/hub/` con los CSV y checksums.
3. Validar que la bandeja de entrada `/var/prevengos/oficial/incoming` queda
   vacía o solo contiene drops pendientes anteriores.

## Después de la ventana (D+0)

1. Revisar la carpeta `archive/` y borrar manualmente los drops con antigüedad
   superior a 30 días, conservando al menos los últimos 7.
2. Investigar cualquier drop movido a `error/`: abrir ticket con Prevengos e
   incluir el log de aplicación.
3. Comunicar el `trace_id` registrado en la tabla `file_drop_log` (consulta
   `SELECT TOP 5 * FROM file_drop_log ORDER BY created_at DESC;`) en el informe
   diario.
4. Actualizar el estado del checklist en la intranet corporativa.

## Reproceso manual

En caso de incidencias, ejecutar:

```bash
curl -X POST https://hub.prevengos.local/rrhh/export
curl -X POST https://hub.prevengos.local/rrhh/import
```

Documentar el resultado en el parte de soporte, adjuntando la ruta del drop
procesado o movido a `error/`.
