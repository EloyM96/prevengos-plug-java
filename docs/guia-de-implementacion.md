# Guía de implementación de Prevengos Plug

Esta guía resume la arquitectura integral del sistema para que los equipos de Android, escritorio y backend trabajen con una visión compartida. Toda la solución es Java y se ejecuta en instalaciones locales de Prevengos.

## Componentes y módulos

### Aplicaciones cliente
- **Android (Java, Room/WorkManager)**: captura formularios médicos en movilidad con soporte offline. Persiste datos en Room y delega en el hub la sincronización con SQL Server y CSV.
- **Escritorio (JavaFX)**: interfaz para personal sanitario/administrativo que consulta y actualiza información en SQL Server. Comparte contratos y validaciones con la app móvil mediante `modules/shared`.

### Hub local (Spring Boot)
- Expone APIs REST para las apps y ejecuta jobs de sincronización (import/export de CSV y reconciliaciones básicas).
- Conecta con SQL Server mediante JDBC y aplica reglas de negocio comunes.
- Utiliza `modules:gateway` y `modules:hub-backend` como puertos/adaptadores para aislar acceso a datos y transformaciones CSV.

### Módulos compartidos (`modules/shared`, `modules/gateway`)
- Definen DTOs, contratos JSON y utilidades de transformación que comparten Android, escritorio y el hub.
- Implementan lógica de validación, mapeo a CSV y helpers de fechas para asegurar consistencia.

### Adaptadores técnicos de referencia
- **CSV (`modules/gateway/csv`)**: `CsvFileWriter` y `CsvFileReader` aplican las convenciones oficiales (separador `;`, comillas dobles, codificación UTF-8) y generan los ficheros `.sha256` en el mismo formato que `sha256sum`. Cualquier cambio en contratos debe partir de estas utilidades.
- **Transferencia de ficheros (`modules/gateway/filetransfer`)**: `FileTransferClient` soporta entregas SFTP/SMB a las carpetas monitorizadas por Prevengos y registra cada drop en `RrhhCsvExportJob`.
- **SQL Server (`modules/gateway/sqlserver`)**: adaptadores JDBC (`JdbcPacienteGateway`, `JdbcCuestionarioGateway`, etc.) encapsulan los `MERGE`, lecturas paginadas y consultas de exportación. Todos los accesos a la BD del fabricante pasan por aquí.
- **API Prevengos (`modules/api-rest`)**: `LoggingPrevengosAdapter` es el stub por defecto para pruebas locales. Para conectarse al “servicio de datos” oficial crea un `RestPrevengosAdapter` que implemente `PrevengosPort` y configúralo vía Spring (bean alternativo o perfil específico) con el endpoint JSON proporcionado por el soporte de Prevengos.
- **Auditoría RRHH (`modules/hub-backend`)**: `RrhhCsvExportJob` orquesta la extracción de datos, genera los CSV, calcula checksums y registra los drops en `RrhhAuditGateway` para trazabilidad completa.

### Integración con Prevengos
- **CSV oficiales**: el hub produce ficheros con el formato requerido por Prevengos (altas de trabajadores, cuestionarios, reconocimientos) y consume los CSV de retorno para reflejar estados o aptitudes.
- **SQL Server local**: las operaciones se realizan sobre tablas autorizadas. El hub controla escrituras y registra auditoría básica (timestamps, usuario, origen).
- **Flujos externos**: la entrega y recogida de CSV puede automatizarse mediante scripts o tareas del servidor; este repositorio solo incluye productores y consumidores.

### Integraciones fuera de alcance
- Conectores hacia Moodle, notificaciones, correo, WhatsApp, analítica o LLM deben residir en repositorios dedicados (p. ej. [`prl-notifier`](https://github.com/prevengos/prl-notifier)) y consumir los datos expuestos por el hub (CSV o vistas SQL).

## Flujos de datos

1. **Captura local**: Android y escritorio almacenan datos en sus cachés (Room o SQL Server) y marcan registros pendientes de sincronizar.
2. **Sincronización con el hub**: jobs locales envían registros pendientes al hub Spring Boot, que los consolida en SQL Server.
3. **Generación de CSV**: el hub empaqueta las novedades en CSV con el formato Prevengos y las deposita en la ruta compartida para importación oficial.
4. **Importación desde Prevengos**: el hub monitoriza la carpeta de retorno o ejecuta un job manual para leer CSV generados por Prevengos y actualizar SQL Server; las apps reciben esos cambios.
5. **Consumo por sistemas externos**: repositorios como `prl-notifier` consultan SQL Server o los CSV publicados para disparar notificaciones, reportes o analítica avanzada.

## Controles operativos y de seguridad

- **Control de acceso**: usar cuentas de servicio con privilegios mínimos para SQL Server y rutas de ficheros restringidas. Registrar auditoría básica en el hub (usuario, cliente y marca de tiempo).
- **Protección de datos (RGPD)**: cifrar datos en reposo en SQL Server, anonimizar CSV antes de compartirlos fuera del entorno sanitario y purgar periódicamente información innecesaria.
- **Resiliencia offline**: Android mantiene colas de sincronización y reintentos; la app de escritorio puede operar desconectada utilizando cachés locales y sincronizar al recuperar conexión.
- **Monitorización ligera**: habilitar logs y métricas locales (Micrometer + JMX/Prometheus) para diagnosticar jobs CSV y conexiones con SQL Server sin añadir stacks complejos.

## Relación con otros proyectos

- `prl-notifier` y proyectos anexos reciben datos desde este hub pero mantienen sus propios pipelines (FastAPI, Redis, notificaciones). Evitar duplicidades: este repositorio se centra en el circuito captura ↔ Prevengos vía SQL/CSV.
- Versionar y comunicar cualquier cambio en contratos JSON o CSV compartidos con otros repositorios.

## Documentación complementaria

- **Quickstart operativo:** [`docs/quickstart.md`](quickstart.md) describe cómo levantar el entorno, ejecutar sincronizaciones manuales y disparar las pruebas.
- **Formatos CSV:** [`docs/integrations/csv-formatos.md`](integrations/csv-formatos.md) resume columnas, ejemplos y validaciones compartidas.
- **Automatización y runbooks:** [`docs/operations/csv-automation.md`](operations/csv-automation.md) detalla los jobs recomendados y cómo conectarlos con la monitorización existente.
- **Pruebas end-to-end:** la suite Playwright (`tests/e2e`) cubre el recorrido de intercambio CSV completo.

