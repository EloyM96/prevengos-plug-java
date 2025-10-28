# Lista de comprobación para piloto con Prevengos

Esta guía resume los pasos obligatorios antes de declarar que el despliegue de Prevengos Plug está listo para pruebas piloto con clientes reales. Complementa la [automatización de CSV](csv-automation.md), el [Quickstart](../quickstart.md) y los runbooks operativos existentes.

## 1. Parametrizar jobs RRHH con rutas y credenciales reales

1. **Revisar propiedades de Spring**: actualizar `hub.jobs.rrhh-export.*` y `hub.jobs.rrhh-import.*` en el `application.yml` (o `application-*.yml`) del entorno destino para que apunten a las rutas oficiales asignadas por Prevengos (`base-dir`, `archive-dir`, `inbox-dir`, `error-dir`). Las rutas por defecto (`/var/prevengos/oficial/*`) son solo demostrativas y deben reemplazarse en cada despliegue.【F:modules/hub-backend/src/main/resources/application.yml†L19-L47】
2. **Sincronizar variables de entorno**: definir los equivalentes `RRHH_EXPORT_*` y `RRHH_IMPORT_*` en Docker Compose, Kubernetes u orquestadores similares para que el backend arranque con credenciales válidas (usuario/clave o llaves SSH, dominio SMB, rutas remotas). Documentar los secretos en el almacén corporativo siguiendo la guía de seguridad.【F:modules/hub-backend/src/main/resources/application.yml†L25-L45】
3. **Validar configuración efectiva**: ejecutar el backend con `--debug` o revisar los logs de arranque para confirmar que `hub.jobs.rrhh-*` carga los valores esperados y que las rutas locales existen. Cualquier advertencia sobre directorios ausentes en `RrhhCsvImportJob` debe resolverse antes del piloto.【F:modules/hub-backend/src/main/java/com/prevengos/plug/hubbackend/job/RrhhCsvImportJob.java†L49-L79】

## 2. Garantizar propagación de importaciones hacia los clientes

1. **Registrar eventos de sincronización**: el job `RrhhCsvImportJob` importa pacientes y cuestionarios llamando a los gateways con `syncToken` 0, lo que impide que los clientes vean esos registros en pulls incrementales. Antes del piloto, ampliar el job para generar eventos (`SyncEvent`) o recalcular el token con el valor real posterior al upsert y dejar la decisión documentada en el repositorio.【F:modules/hub-backend/src/main/java/com/prevengos/plug/hubbackend/job/RrhhCsvImportJob.java†L99-L166】
2. **Prueba de extremo a extremo**: tras ajustar la generación de eventos, ejecutar `/sincronizacion/pull` desde Android (WorkManager) y escritorio (SyncService) verificando que los registros importados aparecen con el token correcto. Registrar evidencia (logs, capturas) en la bitácora operativa.
3. **Auditoría en base de datos**: consultar `sync_events` y tablas `pacientes`/`cuestionarios` para confirmar que los nuevos tokens quedan asociados a las entidades importadas. Seguir el checklist manual para dejar trazabilidad de consultas y resultados.【F:docs/quality/manual-sync-checklist.md†L33-L66】

## 3. Verificar transferencia con infraestructura oficial

1. **Probar SFTP y SMB reales**: el `DefaultFileTransferClient` admite ambos protocolos, pero es imprescindible levantar recorridos usando certificados, claves y cuotas entregadas por Prevengos. Ejecutar exportaciones/importaciones reales y validar permisos en los servidores oficiales.【F:modules/gateway/src/main/java/com/prevengos/plug/gateway/filetransfer/DefaultFileTransferClient.java†L31-L128】
2. **Registrar evidencias operativas**: documentar en la wiki interna o acta de despliegue las rutas remotas usadas, el fingerprint de los servidores y los resultados de transferencia (logs `hub-backend`, métricas `hub_rrhh_*`).
3. **Actualizar runbooks**: incorporar cualquier particularidad detectada (por ejemplo, limitaciones de cuota, ventanas horarias o requisitos de VPN) en [`docs/operations/csv-automation.md`](csv-automation.md) o en los runbooks de soporte.

## 4. Completar pruebas integrales multi-cliente

1. **Seguir el Quickstart completo**: levantar el entorno, ejecutar sincronizaciones manuales y validar métricas conforme a [`docs/quickstart.md`](../quickstart.md).
2. **Ejecutar pruebas automatizadas**: lanzar `./gradlew :modules:hub-backend:test`, `./gradlew :android-app:test` y la suite Playwright (`npm test` en `tests/e2e`) contra el entorno configurado para confirmar que los endpoints reales responden correctamente.【F:docs/quickstart.md†L44-L74】
3. **Validación cruzada Android/desktop**: en Android, revisar que `WorkManager` completa los trabajos programados y que Retrofit utiliza el endpoint real; en escritorio, verificar que `SyncService` procesa pulls/pushes y que los CSV se generan/consumen sin errores. Registrar los resultados en el checklist manual antes de dar acceso a usuarios finales.【F:docs/operations/android-app-onboarding.md†L32-L44】【F:desktop-app/README.md†L1-L80】

Cumplir con estos puntos asegura que la funcionalidad base del repositorio está operativa con infraestructura real y que el intercambio con Prevengos puede iniciarse en fase piloto con respaldo documental.
