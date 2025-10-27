# Bus de mensajería PRL Hub (RabbitMQ / Redis Streams)

Este documento lista los topics/eventos que circularán por el bus interno del Hub PRL. Todos los
mensajes siguen la envoltura `EventEnvelope` definida en `contracts/json/v1/event-envelope.schema.json`
(ejemplo pendiente de publicar) e incluyen metadatos de trazabilidad (`event_id`, `source`,
`occurred_at`, `schema_version`).

## Topología

- **RabbitMQ** actúa como *event backbone* para integraciones críticas (Prevengos, móviles, ETLs).
  - Exchange principal `hub.prl.events` (tipo `topic`).
  - Cada evento se publica en `routing_key` = `<dominio>.<evento>`.
- **Redis Streams** (`prl-hub:outbox`) mantiene una ventana corta (<48h) para *replay* y testing.
- **Outbox Pattern**: la API y los conectores persisten eventos en una tabla `event_outbox` antes de
  publicarlos para asegurar idempotencia.

## Eventos publicados

| Evento (`routing_key`)             | Dominio       | Productor principal        | Consumidores esperados                               | Payload (schema)                   | Notas |
|-----------------------------------|---------------|-----------------------------|------------------------------------------------------|------------------------------------|-------|
| `pacientes.registrado`            | Pacientes     | API `/pacientes`            | Prevengos RRHH, motor analítico, sincronización móvil | `contracts/json/v1/paciente`       | Dispara actualización maestro trabajador. |
| `pacientes.actualizado`           | Pacientes     | API `/pacientes`            | Prevengos RRHH, historizador                        | `contracts/json/v1/paciente`       | Incluye `change_version` y `delta`. |
| `cuestionarios.completado`        | Cuestionarios | API `/cuestionarios`        | Motor Python, Prevengos médico, data lake            | `contracts/json/v1/cuestionario`   | Adjuntos se entregan vía S3/MinIO referenciado. |
| `reconocimientos.planificado`     | Reconocimientos | API `/reconocimientos`    | Agenda médica, Prevengos citas, notificador Omnichannel | `contracts/json/v1/cita`        | Incluye `externo_ref` para correlación. |
| `reconocimientos.estado_cambiado` | Reconocimientos | Sincronización móvil/Prevengos | Portal empresas, Prevengos, data lake             | `contracts/json/v1/cita`           | `metadata.status_source` indica origen (móvil/Prevengos). |
| `aptitud.cambiada`                | Aptitud       | Motor analítico / médico    | RRHH, portal empresa, generador certificados         | `contracts/json/v1/cita`           | Cambios de aptitud deben disparar generación de certificado. |
| `rrhh.drop_generado`              | Integraciones | Job `/prevengos/jobs/rrhh`  | Servidor de ficheros, Prevengos legacy               | `contracts/json/v1/event-envelope` | Notifica disponibilidad del fichero Access/CSV en la drop-zone. |
| `sync.conflicto_detectado`        | Sincronización | Servicio Sync               | Equipo soporte, data quality                         | `contracts/json/v1/event-envelope` | Incluye detalles del conflicto para resolución manual. |

## Suscripciones clave

- **Prevengos Core**: colas dedicadas `prevengos.rrhh`, `prevengos.medico`. Filtro por `pacientes.*`, `reconocimientos.*`, `aptitud.cambiada`.
- **Aplicaciones móviles**: cola `mobile.sync` suscrita a `pacientes.*` y `reconocimientos.*`.
- **Motor analítico/LLM (Python Engine)**: cola `analytics` suscrita a `cuestionarios.completado`, `aptitud.cambiada` y `sync.conflicto_detectado`.
- **ETL Data Lake**: cola `datalake.raw` con `#` para ingestión completa.

## Consideraciones de operación

1. **Versionado de eventos**: la propiedad `schema_version` debe alinearse con `contracts/json`.
2. **Garantías**: RabbitMQ configurado en modo `publisher confirms` + colas durables.
3. **DLQ**: cada cola crítica tiene cola de rechazo `<queue>.dlq` con TTL 7 días.
4. **Monitoreo**: métricas expuestas en Prometheus (`hub_prl_events_published_total`, `hub_prl_events_retry_total`).
5. **Seguridad**: credenciales específicas por consumidor, roles de solo lectura para Redis Streams.

## Pendientes

- Publicar ejemplos concretos de `EventEnvelope` firmados.
- Definir convención de *replay* para sincronizaciones masivas (>48h) usando almacenamiento S3.
- Automatizar provisión de colas vía Terraform/Ansible.
