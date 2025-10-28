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
3. **Replay completo**: en incidentes mayores, iniciar pulls desde `syncToken=0` y paginar hasta alcanzar el token actual. Registrar la duración del proceso en los logs operativos.
4. **Limpieza de estado local**: si una caché local queda corrupta, limpiar el storage y reconstruirlo reanudando pulls desde `0`.

## Observabilidad

* Logs estructurados JSON (`logback-spring.xml`) incluyen campos `eventType`, `source`, `syncToken`, `batchSize` y `isNew`.
* El historial de sincronización se puede auditar directamente en SQL Server mediante las tablas `sync_events`, `pacientes`
  y `cuestionarios`. Consultas comunes:
  * `SELECT TOP 20 sync_token, event_type, source FROM dbo.sync_events ORDER BY sync_token DESC;`
  * `SELECT paciente_id, sync_token, updated_at FROM dbo.pacientes ORDER BY updated_at DESC;`
* Para observabilidad operativa integrar los logs en ELK/Splunk y configurar alertas sobre la ausencia de nuevos `sync_events`
  en un intervalo dado.

## Integración con pruebas

* Pruebas automatizadas en `SynchronizationFlowTest` cubren la secuencia offline → online → pull.
* El checklist manual (`docs/quality/manual-sync-checklist.md`) complementa las validaciones funcionales y de observabilidad.
