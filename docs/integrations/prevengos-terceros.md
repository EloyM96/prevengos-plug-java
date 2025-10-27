# Guía de integraciones Prevengos y terceros

Este documento resume cómo debe operar el hub Java para sincronizarse con Prevengos y cómo coordinarse con otros proyectos corporativos sin duplicar funcionalidades. Todo el flujo se apoya en SQL Server local y en drops CSV controlados; los conectores avanzados (mensajería, analítica, motor de reglas) residen fuera de este repositorio.

## 1. Conector RRHH (Prevengos Legacy / CSV)

1. **Provisionar credenciales**: solicitar a Prevengos el usuario técnico SFTP/SMB y rutas de drop autorizadas.
2. **Configurar job local** (`modules/hub-backend`):
   - Origen de datos: tablas SQL Server `pacientes`, `contratos`, `reconocimientos`.
   - Plantillas CSV en `contracts/csv/rrhh` (campos en castellano, codificación UTF-8, separador `;`).
   - Ruta de salida con control de permisos (solo lectura para Prevengos, escritura para el hub).
3. **Programación**: cron mínimo diario a las 03:00 CET fuera de horario productivo. Permitir ejecución manual para incidencias.
4. **Validación**: registrar auditoría en SQL Server (tabla `rrhh_exports`) con `trace_id`, fecha y operador.
5. **Fallback**: conservar los últimos 7 drops en carpeta `archive/` por si Prevengos solicita reenvío.

## 2. Integraciones analíticas y notificaciones (fuera de este repositorio)

1. **Coordinación**: proyectos como [`prl-notifier`](https://github.com/prevengos/prl-notifier) consumen los CSV generados o consultan vistas SQL read-only. No deben implementarse adaptadores equivalentes aquí.
2. **Contratos**: compartir con dichos proyectos la misma documentación de CSV/JSON almacenada en `contracts/` para evitar divergencias.
3. **Entrega de datos**: habilitar vistas SQL (`vw_eventos_notificacion`, `vw_alertas_riesgo`) o copiar CSV a una ruta de intercambio. Nunca enviar correos/SMS desde este hub.
4. **Gobernanza**: versionar cambios en contratos y comunicar el número de versión a los equipos externos antes de desplegar.

## 3. Integraciones SOAP (Prevengos Core y terceros legacy)

1. **Catálogo de servicios**: documentar los WSDL autorizados por Prevengos y almacenarlos en `docs/integrations/wsdl/`.
2. **Adaptador Java**: implementar clientes JAX-WS en `modules/gateway` que lean/escriban exclusivamente datos necesarios para sincronizar cuestionarios y reconocimientos. Evitar lógica de negocio duplicada.
3. **Seguridad**: usar certificados cliente (mutual TLS) y credenciales rotadas; registrar peticiones en la tabla `soap_audit` de SQL Server.
4. **Transformaciones**: mapear el XML a los DTO Java existentes; si se requieren CSV intermedios, depositarlos en las mismas rutas controladas de Prevengos.

## 4. SQL read-only (federación de datos Prevengos)

1. **Conectividad**: solicitar acceso read-only a vistas SQL Server autorizadas (`vw_prevengos_citas`, `vw_prevengos_aptitudes`).
2. **Vistas propias**: crear vistas en `modules/hub-backend` que filtren y normalicen los datos para las apps.
3. **Herramientas**: exponer consultas parametrizadas vía API REST del hub o scripts CLI para extracción puntual.
4. **Seguridad**: restringir IPs corporativas y auditar cada conexión (tabla `sql_audit`).
5. **Cache**: cuando sea necesario, generar tablas materializadas en SQL Server con actualización programada desde el hub.

## 5. Drops de ficheros (SFTP/SMB)

1. **Inventario**: documentar cada drop requerido (RRHH, mutuas, contratas) en `docs/integrations/drops.md`.
2. **Convención de nombres**: `YYYYMMDD/<proceso>/<archivo>.csv` con checksums acompañantes (`.sha256`).
3. **Automatización**: los jobs del hub generan ficheros y los suben vía SFTP/SMB; registrar resultado en `file_drop_log`.
4. **Notificación interna**: enviar alerta interna (correo corporativo manual o ticket) cuando falle un drop. La mensajería automática la gestiona `prl-notifier`.
5. **Retención**: limpiar drops >30 días y auditar transferencias semanalmente.

## Checklist de activación

- [ ] Contratos `contracts/` actualizados y compartidos con equipos externos.
- [ ] Credenciales y rutas documentadas en un vault seguro.
- [ ] Auditorías SQL y logs habilitados en el hub.
- [ ] Procedimientos de soporte (nivel 1/2) publicados en la intranet.
