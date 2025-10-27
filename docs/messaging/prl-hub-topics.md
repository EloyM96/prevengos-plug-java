# Flujo de eventos locales PRL Hub

La arquitectura actual no incorpora un bus de mensajería dedicado (RabbitMQ/Redis). En su lugar, el hub Java registra eventos relevantes en tablas de auditoría de SQL Server y genera CSV para que otros sistemas los consuman. Este documento describe qué eventos se persisten y cómo pueden ser consultados por herramientas externas sin introducir un broker adicional.

## Registro de eventos en SQL Server

| Evento                               | Tabla SQL                | Productor principal            | Consumidores externos              | Notas |
|--------------------------------------|--------------------------|--------------------------------|------------------------------------|-------|
| `pacientes.registrado`               | `event_log_pacientes`    | API `/pacientes` y apps        | RRHH (CSV), sincronización móvil   | Incluye payload JSON y `trace_id`. |
| `pacientes.actualizado`              | `event_log_pacientes`    | API `/pacientes`               | RRHH (CSV), historización externa  | Campo `change_version` incremental. |
| `cuestionarios.completado`           | `event_log_cuestionarios`| API `/cuestionarios`           | Prevengos médico (CSV), prl-notifier | Adjuntos referenciados por ruta local. |
| `reconocimientos.planificado`        | `event_log_reconocimientos` | API `/reconocimientos`      | Agenda médica, Prevengos citas     | Guarda fecha y recurso asignado. |
| `reconocimientos.estado_cambiado`    | `event_log_reconocimientos` | Sincronización Prevengos    | Portal empresas, reporting         | `metadata.status_source` indica origen. |
| `aptitud.cambiada`                   | `event_log_reconocimientos` | Validación médica          | RRHH, generación de certificados   | Dispara exportación CSV para Prevengos. |
| `rrhh.drop_generado`                 | `event_log_integraciones` | Job CSV RRHH                 | Equipo RRHH                         | Guarda ruta y checksum del fichero. |
| `sync.conflicto_detectado`           | `event_log_sync`          | Servicio de sincronización    | Equipo soporte                      | Se detalla el conflicto para resolución manual. |

## Consulta por otros proyectos

1. **Vistas SQL**: publicar vistas `vw_event_log_*` con filtros por fecha y dominio. Los sistemas externos (p.ej. `prl-notifier`) pueden ejecutar lecturas periódicas o CDC.
2. **CSV incrementales**: jobs nocturnos generan CSV con los eventos nuevos y los depositan en `/var/prevengos/events/YYYYMMDD/`. Esta es la alternativa ligera a RabbitMQ.
3. **API REST**: el hub expone endpoints `/eventos/<dominio>` con paginación y filtros (`desde`, `hasta`, `trace_id`). Diseñados para consultas manuales o integración sencilla.

## Consideraciones operativas

1. **Versionado**: los payloads almacenados siguen los esquemas de `contracts/json`. Cada fila registra `schema_version`.
2. **Retención**: conservar mínimo 180 días de eventos en SQL Server; mover históricos a almacenamiento frío si se requiere.
3. **Auditoría**: activar triggers que capturen usuario y cliente de origen. Los eventos sirven como evidencia ante Prevengos y auditorías internas.
4. **Alertas**: crear vistas o procedimientos almacenados que detecten retrasos en exportaciones CSV y envíen alertas manuales (correo corporativo/ticketing).
5. **Escalabilidad**: si en el futuro se necesitara un broker, los consumidores deberían poder migrar leyendo desde las tablas outbox existentes.

## Pendientes

- Publicar ejemplos concretos de filas en `event_log_*` y sus CSV derivados.
- Documentar scripts de extracción (`scripts/export-events.sh`) para soportar auditorías.
- Evaluar si alguna integración futura justifica la introducción de un broker dedicado.
