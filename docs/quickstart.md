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

1. Comprueba el estado general del backend:
   ```bash
   curl http://localhost:8080/actuator/health
   ```
2. Verifica que la base almacena eventos y tokens monotónicos conectándote a SQL Server (la contraseña proviene de `infra/local-hub/.env`):
   ```bash
   docker compose --env-file infra/local-hub/.env -f infra/local-hub/docker-compose.yml exec sqlserver \
     /opt/mssql-tools/bin/sqlcmd -S localhost,1433 -U ${MSSQL_APP_USER} -P ${MSSQL_APP_PASSWORD} -d ${MSSQL_DB} \
     -Q "SELECT TOP 10 sync_token, event_type, source FROM dbo.sync_events ORDER BY sync_token DESC;"
   ```
   Deberías observar tokens crecientes conforme se registran pushes.

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
   > Si trabajas sin wrapper (`./gradlew`), ejecuta el mismo comando con `gradle` en su lugar.
2. **Aplicación Android (repositorio y parsing remoto):**
   ```bash
   ./gradlew :android-app:test
   ```
   Los tests mockean Retrofit para validar el marcado de lotes, los pulls incrementales y el borrado de cuestionarios en conflicto.
   > También puedes ejecutar `gradle :android-app:test` si no tienes wrapper local.
3. **Pruebas end-to-end del hub:**
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
   El endpoint responde con `202 Accepted` y un cuerpo `JSON` similar a:
   ```json
   {
     "trace_id": "<UUID>",
     "remote_path": "/prevengos/oficial/rrhh",
     "staging_dir": "/var/prevengos/oficial/outgoing/<trace>",
     "archive_dir": "/var/prevengos/oficial/archive/<trace>",
     "pacientes": 1,
     "cuestionarios": 1
   }
   ```
   Los CSV se generan en `RRHH_EXPORT_BASE` y se archivan en `RRHH_EXPORT_ARCHIVE`. Si la entrega remota está deshabilitada `remote_path` vendrá como `null`.
3. Para un recorrido completo sobre automatizaciones y carpetas de drop, revisa [`docs/operations/csv-automation.md`](docs/operations/csv-automation.md).

## 7. Preparar entregables de cliente

* Para la app Android, sigue la guía de construcción y distribución en [`docs/operations/android-build-and-distribution.md`](docs/operations/android-build-and-distribution.md).
* Para la app de escritorio JavaFX, consulta el empaquetado descrito en [`docs/operations/desktop-app-distribution.md`](docs/operations/desktop-app-distribution.md) y el README del módulo [`desktop-app/README.md`](desktop-app/README.md).
* Completa los jobs descritos en [`docs/operations/csv-automation.md`](docs/operations/csv-automation.md) para enviar/recibir CSV sin intervención manual y revisa los runbooks de soporte en `docs/operations` junto con los procedimientos de calidad en `docs/quality`.
