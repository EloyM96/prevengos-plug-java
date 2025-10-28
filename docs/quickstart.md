# Quickstart Prevengos Plug

Guía unificada para levantar el entorno local, generar datos de prueba y validar el intercambio CSV con Prevengos.

## 1. Preparación del entorno

1. Instala los prerrequisitos:
   * JDK 21
   * Docker Engine 24+ y Docker Compose V2
   * Node.js 20+ (para las pruebas end-to-end)
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

## 3. Ejecutar una sincronización manual

1. Genera payloads `pacientes.json` y `cuestionarios.json` utilizando las plantillas de `contracts/json` o exportando desde la app Android.
2. Sigue el procedimiento detallado en [`docs/quality/manual-sync-checklist.md`](docs/quality/manual-sync-checklist.md) para enviar los datos, verificar SQL Server mediante `sqlcmd` y realizar pulls subsecuentes.

## 4. Validar el intercambio CSV

1. Los formatos oficiales y ejemplos están documentados en [`contracts/csv/rrhh`](contracts/csv/rrhh/README.md) y en la guía funcional [`docs/integrations/csv-formatos.md`](docs/integrations/csv-formatos.md).
2. Para un recorrido completo sobre automatizaciones y carpetas de drop, revisa [`docs/operations/csv-automation.md`](docs/operations/csv-automation.md).

## 5. Ejecutar pruebas end-to-end

1. Instala las dependencias de Playwright:
   ```bash
   cd tests/e2e
   npm install
   npx playwright install --with-deps
   ```
2. Lanza la suite que cubre el recorrido web y las validaciones de CSV:
   ```bash
   npm test
   ```
3. Abre el reporte interactivo si necesitas revisar evidencias:
   ```bash
   npm run test:report
   ```

## 6. Siguientes pasos operativos

* Configura los jobs descritos en [`docs/operations/csv-automation.md`](docs/operations/csv-automation.md) para enviar/recibir CSV sin intervención manual.
* Revisa los runbooks de soporte en `docs/operations` y los procedimientos de calidad en `docs/quality` para garantizar cobertura antes de habilitar el acceso a equipos no técnicos.
