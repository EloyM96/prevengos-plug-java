# Checklist manual de sincronización Hub ⇄ Prevengos

Este procedimiento cubre la secuencia end-to-end para validar la sincronización desde captura offline hasta la obtención de eventos por pulls subsecuentes.

## Preparación

1. Levantar el entorno local con Docker Compose siguiendo `infra/local-hub/README.md`.
2. Verificar que la API del Hub responde:
   ```bash
   curl http://localhost:8080/actuator/health
   ```
3. Exportar variables útiles (adaptar si cambió el puerto):
   ```bash
   export HUB_URL=http://localhost:8080
   export SOURCE_SYSTEM=offline-tablet
   ```

## Captura offline y sincronización

1. Simular la captura de pacientes en el dispositivo desconectado generando el payload JSON (`pacientes.json`).
2. Sincronizar cuando el dispositivo recupera conectividad:
   ```bash
   http POST "$HUB_URL/sincronizacion/pacientes" \ 
     "X-Source-System:$SOURCE_SYSTEM" \ 
     < pacientes.json
   ```
3. Repetir el proceso para los cuestionarios:
   ```bash
   http POST "$HUB_URL/sincronizacion/cuestionarios" \ 
     "X-Source-System:$SOURCE_SYSTEM" \ 
     < cuestionarios.json
   ```

## Verificación en base de datos

1. Conectarse al contenedor de PostgreSQL:
   ```bash
   docker compose --env-file infra/local-hub/.env -f infra/local-hub/docker-compose.yml exec postgres psql \
     -U "$POSTGRES_USER" -d "$POSTGRES_DB"
   ```
2. Ejecutar las consultas de control:
   ```sql
   SELECT COUNT(*) FROM pacientes;
   SELECT COUNT(*) FROM cuestionarios;
   SELECT sync_token, event_type, source FROM sync_events ORDER BY sync_token;
   ```

## Pulls subsecuentes

1. Solicitar eventos pendientes desde el cliente:
   ```bash
   http "$HUB_URL/sincronizacion/pull?limit=100"
   ```
2. Guardar el `nextToken` retornado y repetir el pull usando dicho token para confirmar que no se repiten eventos:
   ```bash
   http "$HUB_URL/sincronizacion/pull?syncToken=<NEXT_TOKEN>&limit=100"
   ```
3. Registrar métricas y logs:
   * Logs estructurados visibles con `docker compose logs hub-backend`.
   * Métricas en `http://localhost:8080/actuator/prometheus` (`hub_sync_*`).

## Limpieza y repetición

* Para reiniciar pruebas:
  ```bash
  docker compose --env-file infra/local-hub/.env -f infra/local-hub/docker-compose.yml down -v
  docker compose --env-file infra/local-hub/.env -f infra/local-hub/docker-compose.yml up --build
  ```
* Repetir la secuencia con diferentes fuentes (`X-Source-System`) para validar etiquetado en métricas y logs.
