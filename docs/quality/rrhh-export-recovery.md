# Recuperación de exportaciones RRHH

Este procedimiento complementa la [checklist de sincronización](manual-sync-checklist.md) y
describe cómo repetir una exportación oficial y validar la entrega de los CSV cuando
Prevengos solicite un reenvío.

## 1. Preparación

1. Confirmar que la sincronización completa (captura → hub) funciona siguiendo los pasos de
   `manual-sync-checklist.md`. No continuar si alguna verificación falla.
2. Identificar el `trace_id` del último drop correcto ejecutando:
   ```sql
   SELECT TOP 5 trace_id, remote_path, created_at, status
   FROM rrhh_exports
   ORDER BY created_at DESC;
   ```
3. Recopilar evidencias del fallo en Prevengos (ticket, correo o log externo) para adjuntarlo
   al informe de recuperación.

## 2. Reejecutar la exportación

1. Lanzar manualmente el job desde el hub (recibirás `202 Accepted` con los metadatos del drop):
   ```bash
   curl -X POST http://localhost:8080/rrhh/export
   ```
   Anotar el `traceId` y la ruta `remotePath` devueltos (puede ser `null` si la entrega remota está deshabilitada).
2. Confirmar que se han generado los CSV en la carpeta de staging y en el archivo local:
   ```bash
   ls "$RRHH_EXPORT_BASE/$(date +%Y%m%d)/rrhh/hub"
   ls "$RRHH_EXPORT_ARCHIVE/$(date +%Y%m%d)/rrhh/hub/$TRACE_ID"
   ```
3. Validar la auditoría en base de datos:
   ```sql
   SELECT * FROM rrhh_exports WHERE trace_id = '$TRACE_ID';
   SELECT * FROM file_drop_log WHERE trace_id = '$TRACE_ID';
   ```

## 3. Comprobaciones finales

1. Verificar con el equipo de infra si el fichero ha llegado al drop remoto (SFTP/SMB). Si no,
   reenviar manualmente con `scp` o `smbclient` utilizando la carpeta archivada.
2. Documentar el incidente y adjuntar las consultas anteriores al ticket de Prevengos.
3. Cerrar la incidencia únicamente cuando Prevengos confirme que ha recibido y procesado los
   ficheros.

## 4. Script de validación rápida

Guardar como `scripts/check_rrhh_export.sql` en tu entorno local para reutilizar:

```sql
-- Valida el último drop generado por el hub
SELECT TOP 1 trace_id, remote_path, pacientes_count, cuestionarios_count, status
FROM rrhh_exports
ORDER BY created_at DESC;

SELECT log_id, file_name, status, message
FROM file_drop_log
WHERE trace_id = (SELECT TOP 1 trace_id FROM rrhh_exports ORDER BY created_at DESC);
```

Este script permite al equipo de soporte verificar en segundos el estado de la última
exportación sin necesidad de acceder al servidor de ficheros.
