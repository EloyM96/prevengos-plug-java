# Inventario de drops CSV Prevengos

Este documento describe los intercambios CSV soportados por el hub PRL, las
rutas oficiales asignadas por Prevengos y los mecanismos de automatización
que deben configurarse en cada despliegue.

## Rutas oficiales

| Propósito                      | Ruta                                   | Origen | Destino |
|--------------------------------|----------------------------------------|--------|---------|
| Exportaciones hub → Prevengos  | `/var/prevengos/oficial/outgoing`      | Hub    | Prevengos |
| Importaciones Prevengos → hub  | `/var/prevengos/oficial/incoming`      | Prevengos | Hub |
| Históricos validados           | `/var/prevengos/oficial/archive`       | Hub    | Auditoría |
| Incidencias / reprocesos       | `/var/prevengos/oficial/error`         | Hub    | Soporte |

Cada drop debe seguir la estructura `YYYYMMDD/<proceso>/<origen>/`. Para RRHH
se emplea `proceso = rrhh`, `origen = hub` (exportaciones) y `origen = prevengos`
(importaciones). Los ficheros de datos se acompañan de su checksum `SHA-256`.

## Flujo de exportación (hub → Prevengos)

1. El job `RrhhCsvExportJob` consulta SQL Server y genera `pacientes.csv` y
   `cuestionarios.csv` junto a sus ficheros `.sha256`.
2. El destino efectivo queda bajo `RRHH_EXPORT_BASE`, por defecto
   `/var/prevengos/oficial/outgoing/YYYYMMDD/rrhh/hub/`.
3. Se mantienen 7 drops en la carpeta `archive` mediante la rotación manual que
   se describe en el checklist operativo.
4. El endpoint `POST /rrhh/export` permite relanzar el job para incidencias.

Variables de entorno relevantes:

- `RRHH_EXPORT_BASE`: ruta base de exportación.

## Flujo de importación (Prevengos → hub)

1. Prevengos deposita los ficheros en
   `/var/prevengos/oficial/incoming/YYYYMMDD/rrhh/prevengos/`.
2. El job `RrhhCsvImportJob` valida los checksum, inserta/actualiza datos en
   SQL Server y mueve el drop completo a `archive`.
3. Ante fallos, el drop se mueve a `error` manteniendo el mensaje de error en el
   nombre del directorio y registrándose en los logs del hub.
4. El endpoint `POST /rrhh/import` reprocesa manualmente la bandeja de entrada.

Variables de entorno relevantes:

- `RRHH_IMPORT_INBOX`: ruta de entrada.
- `RRHH_IMPORT_ARCHIVE`: destino para drops correctos.
- `RRHH_IMPORT_ERROR`: cuarentena para incidencias.

## Automatizaciones

El directorio `infra/automation` contiene el script `rrhh_csv_exchange.sh` que
orquesta ambos endpoints y registra la actividad en syslog. Puede programarse
vía `cron` o `systemd` con el siguiente ejemplo de cron diario:

```
0 3 * * * /opt/prevengos/rrhh_csv_exchange.sh >> /var/log/prevengos/rrhh_cron.log 2>&1
```

Antes de habilitar la tarea, revisar el checklist operativo publicado en
`docs/quality/rrhh-csv-checklist.md`.
