# Aprovisionamiento de usuarios y roles en SQL Server

Este procedimiento permite a equipos operativos crear las credenciales necesarias para que el hub PRL y los usuarios de reporting accedan a las bases de datos `prl_hub` y `Prevengos`. Sigue los pasos en orden y marca cada hito antes de entregar el entorno.

## 1. Requisitos previos

| Elemento | Detalle |
| --- | --- |
| Servidor SQL | Instancia Microsoft SQL Server 2019 o superior con conectividad desde el hub. |
| Permisos | Cuenta con rol `sysadmin` (temporalmente) para ejecutar el script de aprovisionamiento. |
| Bases de datos | `prl_hub` (operativa del hub) y `Prevengos` (fuente corporativa). Si no existen se crearán durante el proceso. |
| Herramienta | SQL Server Management Studio (SSMS) o `sqlcmd` con modo SQLCMD activado. |

## 2. Fichero de aprovisionamiento

El repositorio incluye el script `docs/scripts/sqlserver/bootstrap_prevengos_roles.sql`, diseñado para ejecutarse de principio a fin sin edición manual de código T-SQL. Las únicas modificaciones necesarias son los valores de las variables declaradas con `:setvar` (nombres de login y contraseñas).【F:docs/scripts/sqlserver/bootstrap_prevengos_roles.sql†L1-L88】

```sql
:setvar HubAppLogin "prevengos_hub_app"
:setvar HubAppPassword "Cambiar_Esta_Clave_123!"
:setvar ReportingLogin "prevengos_reporting"
:setvar ReportingPassword "Cambiar_Esta_Clave_456!"
```

> 🛡️ Usa contraseñas que cumplan la política corporativa. Puedes reutilizar los nombres sugeridos u otros definidos por seguridad.

## 3. Pasos a seguir en SSMS

1. Inicia sesión en la instancia de SQL Server con una cuenta `sysadmin`.
2. Abre el archivo `bootstrap_prevengos_roles.sql`.
3. Activa **SQLCMD Mode** (`Query > SQLCMD Mode`). Esto permite que las variables `:setvar` se expandan correctamente.
4. Reemplaza los valores de las variables por las credenciales acordadas.
5. Ejecuta el script completo (`F5`).
6. Verifica que la salida indica `Provisionamiento de roles Prevengos Plug completado.` y que no hay mensajes de error.

## 4. Qué realiza el script automáticamente

* Crea la base de datos `prl_hub` si aún no existe y define los roles `prl_hub_app_role` (lectura/escritura) y `prl_hub_reporting_role` (solo lectura).【F:docs/scripts/sqlserver/bootstrap_prevengos_roles.sql†L17-L56】
* Provisiona dos logins en el servidor (`prevengos_hub_app` y `prevengos_reporting`) y sus usuarios asociados dentro de `prl_hub`.
* Concede los permisos adecuados a cada rol sobre todas las tablas del esquema `dbo`, que incluyen pacientes, cuestionarios, eventos de sincronización y tablas de auditoría definidas en las migraciones del proyecto.【F:migrations/sqlserver/V2__create_prl_hub_tables.sql†L1-L43】【F:migrations/sqlserver/V3__rrhh_audit_tables.sql†L1-L120】
* Crea los usuarios equivalentes en la base `Prevengos` (si existe) y los añade al rol `db_datareader`, requisito indispensable para que las vistas `vw_prl_*` consulten datos corporativos.【F:docs/scripts/sqlserver/bootstrap_prevengos_roles.sql†L58-L88】【F:migrations/sqlserver/V1__create_views.sql†L1-L44】

## 5. Checklist de validación

- [ ] Ejecutar `SELECT name FROM sys.database_principals WHERE name IN ('prl_hub_app_role','prl_hub_reporting_role');` en `prl_hub` y confirmar que devuelve ambas filas.
- [ ] Ejecutar `EXEC sp_helprolemember 'prl_hub_app_role';` y comprobar que incluye el login de aplicación.
- [ ] Validar que el hub puede conectarse usando las credenciales nuevas (por ejemplo, actualizando el archivo de configuración de la app de escritorio o el hub backend y probando una sincronización).
- [ ] Guardar las credenciales en el gestor corporativo (no enviarlas por correo plano).

Con estos pasos, la instancia queda preparada para que el hub PRL almacene información y para que los equipos de reporting consulten los datos sin intervención adicional del equipo técnico.
