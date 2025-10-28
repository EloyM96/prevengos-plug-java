# Flujos de sincronización y manejo de conflictos

Este documento describe la arquitectura de sincronización entre el Hub PRL y los sistemas Prevengos, detallando el uso de tokens, conflictos y estrategias de recuperación.

## Flujos principales

1. **Captura offline**: dispositivos móviles u orígenes desconectados almacenan pacientes y cuestionarios en caché local junto con metadatos (`createdAt`, `updatedAt`).
2. **Sincronización online**: al recuperar conectividad se envía un lote combinado a `/sincronizacion/push` incluyendo `source` y `correlationId` en el cuerpo.
3. **Registro de eventos**: cada `upsert` persiste la entidad y registra un `SyncEvent` con `event_type` (`*-upserted`) y `source` resuelto automáticamente.
4. **Pull incremental**: clientes consumen `/sincronizacion/pull` pasando `syncToken` para avanzar con tokens monotónicos.

## Tokens de sincronización

* `sync_token` es la clave auto incremental en `sync_events` y sirve como cursor global y ordenado.
* El Hub retorna `nextToken` en cada pull; los clientes deben persistirlo y reenviarlo en la siguiente llamada.
* En caso de pérdida del token, reiniciar el proceso desde `syncToken=0` y reconstruir el estado local con los lotes recibidos.

## Manejo de conflictos

* El último `updated_at` recibido gana; el servicio conserva `last_modified` y `sync_token` para trazabilidad.
* Se recomienda que los clientes detecten divergencias comparando `updated_at` local vs. remoto.
* Eventos duplicados se evitan porque el `sync_token` es único y ascendente. Si un cliente reenvía un registro con cambios, se generará un nuevo evento que sobrescribe el anterior.

## Procedimientos de recuperación

1. **Reintentos automáticos**: ante errores transitorios (5xx o timeouts) reintentar con backoff exponencial, manteniendo el payload original.
2. **Reprocesar desde token**: identificar el último `syncToken` confirmado y repetir el pull con `limit` reducido para evitar sobresaturación.
3. **Replay completo**: en incidentes mayores, iniciar pulls desde `syncToken=0` y paginar hasta alcanzar el token actual. Registrar métricas de duración (`hub_sync_pull_duration_seconds`).
4. **Limpieza de estado local**: si una caché local queda corrupta, limpiar el storage y reconstruirlo reanudando pulls desde `0`.

## Observabilidad

* Logs estructurados JSON (`logback-spring.xml`) incluyen campos `eventType`, `source`, `syncToken`, `batchSize` y `isNew`.
* Métricas expuestas en `/actuator/prometheus`:
  * `hub_sync_events_registered_total` (counter) por tipo de evento y origen.
  * `hub_sync_pull_requests_total` (counter) con etiqueta `has_token` para diferenciar llamadas iniciales de las incrementales.
  * `hub_sync_pull_duration_seconds` (timer) y `hub_sync_pull_batch_size` (summary).
  * `hub_sync_pacientes_processed_total` y `hub_sync_cuestionarios_processed_total` para seguimiento por fuente.
  * `hub_sync_pacientes_batch_size` y `hub_sync_cuestionarios_batch_size` describen tamaños de lote por origen.
* Utilizar dashboards (Prometheus/Grafana) para graficar latencias de pull y tamaños de lote.

## Integración con pruebas

* Pruebas automatizadas en `SynchronizationFlowTest` cubren la secuencia offline → online → pull.
* El checklist manual (`docs/quality/manual-sync-checklist.md`) complementa las validaciones funcionales y de observabilidad.
