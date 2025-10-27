# Guía de integraciones Prevengos y terceros

Este documento describe los procedimientos iniciales para habilitar los conectores críticos entre el
Hub PRL, los sistemas Prevengos y terceros. Se priorizan las integraciones de RRHH, analíticas, SOAP,
consultas SQL de solo lectura y drops de ficheros.

## 1. Conector RRHH (Prevengos Legacy / Access)

1. **Provisionar credenciales**: solicitar a Prevengos usuario técnico SFTP y claves para la drop-zone.
2. **Configurar job** `/prevengos/jobs/rrhh` en el Hub con:
   - Ruta de salida (SFTP/SMB) y formato (`.mdb` + export CSV).
   - Plantillas de mapeo desde `pacientes` y `reconocimientos`.
3. **Planificar ejecución**: cron mínimo diario 03:00 CET (fuera de horario productivo).
4. **Validación**: verificar evento `rrhh.drop_generado` en RabbitMQ y confirmación manual del equipo RRHH.
5. **Fallback**: habilitar regeneración manual vía API con `trace_id` para auditoría.

## 2. Analíticas y motor Python

1. **Suscripción a eventos**: conectar a cola `analytics` (ver `docs/messaging/prl-hub-topics.md`).
2. **Provisionar entorno**: desplegar `python-engine` con acceso a MinIO/S3 para adjuntos y a la base PostgreSQL read-only.
3. **Modelos de datos**: reutilizar esquemas `contracts/json` y catálogos de `docs/data-stores/postgresql`.
4. **Salida de resultados**: publicar insights en topic `analytics.insight-generado` (pendiente de definir) o escribir en base read/write dedicada.
5. **Gobernanza**: versionar notebooks y pipelines en repositorio `analytics-prl`, aplicar control de acceso basado en roles.

## 3. Integraciones SOAP (Prevengos Core y terceros legacy)

1. **Catálogo de servicios**: solicitar WSDL a Prevengos (`RRHHService`, `MedicalRecordsService`).
2. **Gateway**: desplegar adaptador SOAP⇄REST en el Hub (p.ej. Apache Camel o Spring Integration).
3. **Seguridad**: certificados cliente (mutual TLS) y WS-Security UsernameToken según requerimiento.
4. **Transformaciones**: mapear WSDL → esquemas `contracts/json` utilizando XSLT o Jolt.
5. **Monitoreo**: habilitar logging estructurado y métricas (`soap_calls_total`, `soap_failures_total`).

## 4. SQL read-only (federación de datos Prevengos)

1. **Conectividad**: solicitar acceso read-only a SQL Server Prevengos (ver `migrations/sqlserver`).
2. **Vistas federadas**: desplegar `V1__create_views.sql` en una base espejo para aislar carga.
3. **Herramientas**: exponer a través de PostgreSQL Foreign Data Wrapper o herramienta BI (Power BI, Superset).
4. **Seguridad**: restringir IPs corporativas, rotar credenciales trimestralmente.
5. **Cache**: habilitar materialized views para indicadores de uso intensivo (p.ej. aptitud diaria, agenda médica).

## 5. Drops de ficheros (SFTP/SMB)

1. **Inventario**: documentar cada proceso que requiere drop (RRHH, mutuas, contratas).
2. **Normalización**: establecer convención `YYYYMMDD/<nombre_proceso>/<archivo>` en el storage.
3. **Automatización**: utilizar jobs del Hub para generar ficheros (CSV, XLSX, PDF) y subirlos vía SFTP.
4. **Notificación**: publicar evento `drop.disponible` con metadatos (ruta, checksum, vencimiento).
5. **Retención**: limpiar drops >30 días y auditar transferencias semanalmente.

## Checklist de activación

- [ ] Contratos `contracts/json` actualizados y compartidos con terceros.
- [ ] Credenciales y endpoints documentados en vault seguro.
- [ ] Métricas y alertas configuradas en Prometheus/Grafana.
- [ ] Procedimientos de soporte (nivel 1/2) definidos y accesibles.
