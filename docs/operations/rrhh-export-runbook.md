# Runbook operativo: drops RRHH Prevengos

## Objetivo

Centralizar las acciones de nivel 1/2 cuando falle la entrega diaria de CSV RRHH
(`pacientes.csv`/`cuestionarios.csv`) hacia Prevengos. Este runbook cubre detección,
comunicación y escalado interno.

## 1. Detección

1. **Alertas automáticas**: el monitoreo de `rrhh_exports.status = 'FAILED'` genera un ticket
   en JIRA (cola `PRL-HUB`). Revisar la descripción para obtener el `trace_id`.
2. **Correos manuales**: si Prevengos reporta la incidencia por correo, abrir ticket manual y
   adjuntar el mensaje original. Etiquetar con `RRHH-DROP`.
3. **Dashboards**: el panel Grafana `hub-rrhh` muestra el último `remote_path` entregado. Si la
   métrica `hub_rrhh_export_success` es `0` durante >30 minutos, disparar alerta manual.

## 2. Contención

1. Validar si existen datos pendientes consultando `file_drop_log` para el `trace_id` afectado.
2. Revisar los logs de la aplicación (`docker compose logs hub-backend`) filtrando por el
   identificador del job.
3. En caso de fallo de red SFTP/SMB, coordinar con Infraestructura para habilitar la ruta
   alternativa (VPN + drop temporal).

## 3. Recuperación

1. Seguir `docs/quality/rrhh-export-recovery.md` para reejecutar el job y validar la auditoría.
2. Si el envío automático vuelve a fallar, transferir manualmente los CSV archivados usando:
   ```bash
   scp -i ~/.ssh/prevengos $ARCHIVE/pacientes.csv user@sftp.prevengos:/oficial/rrhh/
   ```
   o
   ```bash
   smbclient //smb.prevengos/rrhh -U DOMAIN\\user -c "put pacientes.csv"
   ```
3. Registrar la acción manual en el ticket indicando fecha/hora y operador.

## 4. Comunicación interna

1. **Nivel 1**: informar al equipo de PRL vía correo `soporte-prl@empresa.com` con el estado
   actual (éxito/pendiente) y adjuntar el `trace_id`.
2. **Nivel 2**: si se requieren cambios en credenciales o infraestructura, abrir subtarea para
   el equipo de Sistemas con prioridad `Alta`.
3. **Cierre**: actualizar el ticket con capturas de `rrhh_exports` y confirmación de Prevengos.

## 5. Prevención

* Revisar semanalmente los registros en `file_drop_log` para detectar tendencias.
* Rotar credenciales SFTP/SMB según el calendario de seguridad corporativo.
* Mantener un histórico de 7 días en la carpeta de archivo para facilitar reenvíos.
