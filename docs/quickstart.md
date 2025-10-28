# Quickstart Prevengos Plug

Guía unificada para levantar el entorno local, generar datos de prueba y validar el intercambio CSV con Prevengos.

## 1. Preparación del entorno

1. Instala los prerrequisitos:
   * JDK 21
   * Docker Engine 24+ y Docker Compose V2
   * Node.js 20+ (para las pruebas end-to-end)
   * [HTTPie](https://httpie.io/) 3.x (o, si lo prefieres, `curl`) para ejecutar las llamadas REST de sincronización
2. Clona el repositorio y entra al directorio raíz:
   ```bash
   git clone git@github.com:prevengos/prevengos-plug-java.git
   cd prevengos-plug-java
   ```
3. Copia las variables de ejemplo del entorno local y ajusta credenciales si es necesario:
   ```bash
   cp infra/local-hub/.env.example infra/local-hub/.env
   ```

## 2. Levantar el hub con SQL Server

```bash
docker compose --env-file infra/local-hub/.env -f infra/local-hub/docker-compose.yml up --build
```

El comando levanta SQL Server (`prevengos-local-sqlserver`) y el backend Spring Boot (`hub-backend`). Comprueba la salud de la API con:

```bash
curl http://localhost:8080/actuator/health
```

## 3. Verificar salud y observabilidad del hub

1. Comprueba el estado general del backend y que expose los endpoints de observabilidad:
   ```bash
   curl http://localhost:8080/actuator/health
   curl "http://localhost:8080/actuator/metrics/hub.sync.events.registered?tag=event_type:paciente-upserted"
   ```
   La segunda llamada devuelve el contador Micrometer utilizado por los tests automatizados para validar la ingesta de eventos.

## 4. Ejecutar una sincronización manual

1. Genera payloads `pacientes.json` y `cuestionarios.json` utilizando las plantillas de `contracts/json` o exportando desde la app Android.
2. Envía ambos lotes con `POST /sincronizacion/push`:
   ```bash
    curl -X POST http://localhost:8080/sincronizacion/push \
      -H 'Content-Type: application/json' \
      -d @payloads/sync-request.json
   ```
   El repositorio incluye un ejemplo en [`payloads/sync-request.json`](../payloads/sync-request.json)
   que puedes adaptar con los datos generados a partir de `contracts/json`. El backend
   devolverá el `last_sync_token` procesado y los identificadores consolidados.
3. Recupera cambios pendientes con `GET /sincronizacion/pull` especificando el token recibido:
   ```bash
   curl "http://localhost:8080/sincronizacion/pull?syncToken=${LAST_TOKEN}&limit=50"
   ```
4. Sigue el procedimiento detallado en [`docs/quality/manual-sync-checklist.md`](docs/quality/manual-sync-checklist.md) para validar los registros en SQL Server mediante `sqlcmd` y realizar pulls subsecuentes.

## 5. Ejecutar pruebas automatizadas

1. **Backend hub:**
   ```bash
   ./gradlew :modules:hub-backend:test
   ```
   La suite ejecuta pruebas de integración con SQL Server real (Testcontainers) que cubren push, pull y generación de CSV.
2. **Aplicación Android:**
   > ⏳ Las pruebas de la app Android están en fase de diseño. Sigue el roadmap del proyecto para conocer el ticket de implementación y su estado actual.
3. **Aplicación de escritorio:**
   > ⏳ Las pruebas automatizadas de la app de escritorio se incorporarán cuando exista un prototipo funcional.
4. **Pruebas end-to-end del hub:**
   ```bash
   cd tests/e2e
   npm install
   npx playwright install --with-deps
   npm test
   ```
   Por defecto se levanta un mock server local que replica `/sincronizacion` y `/actuator`; si tienes un hub operativo, establece `E2E_BASE_URL=http://localhost:8080` antes de lanzar `npm test`.

## 6. Validar el intercambio CSV

1. Los formatos oficiales y ejemplos están documentados en [`contracts/csv/rrhh`](contracts/csv/rrhh/README.md) y en la guía funcional [`docs/integrations/csv-formatos.md`](docs/integrations/csv-formatos.md).
2. Lanza una exportación ad-hoc con:
   ```bash
   curl -X POST http://localhost:8080/rrhh/export -H 'Content-Type: application/json' -d '{"trigger_type":"manual"}'
   ```
   Los CSV se generan en `RRHH_EXPORT_BASE` y se archivan en `RRHH_EXPORT_ARCHIVE`.
3. Para un recorrido completo sobre automatizaciones y carpetas de drop, revisa [`docs/operations/csv-automation.md`](docs/operations/csv-automation.md).

## 7. Preparar entregables de cliente

* Para la app Android, consulta el estado del diseño en [`docs/operations/android-build-and-distribution.md`](docs/operations/android-build-and-distribution.md); la guía se completará cuando existan builds oficiales.
* Para la app de escritorio JavaFX, revisa el alcance preliminar en [`docs/operations/desktop-app-distribution.md`](docs/operations/desktop-app-distribution.md) y el README del módulo [`desktop-app/README.md`](desktop-app/README.md); ambos documentos se completarán con instrucciones definitivas cuando el cliente esté implementado.
* Completa los jobs descritos en [`docs/operations/csv-automation.md`](docs/operations/csv-automation.md) para enviar/recibir CSV sin intervención manual y revisa los runbooks de soporte en `docs/operations` junto con los procedimientos de calidad en `docs/quality`.
