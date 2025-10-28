/*
    Script de inicialización de credenciales Prevengos Plug.
    Requiere ejecutar en SQL Server Management Studio (SSMS) o sqlcmd con modo SQLCMD activo
    para que las variables definidas con :setvar se expandan correctamente.

    Pasos previos:
      1. Reemplaza los valores de las variables según el entorno antes de ejecutar el script.
      2. Ejecuta el archivo completo (no por bloques) desde una cuenta con privilegios sysadmin.
*/

:setvar HubAppLogin "prevengos_hub_app"
:setvar HubAppPassword "Cambiar_Esta_Clave_123!"
:setvar ReportingLogin "prevengos_reporting"
:setvar ReportingPassword "Cambiar_Esta_Clave_456!"

/* Crea el login y usuario de aplicación (lectura/escritura sobre prl_hub) */
IF NOT EXISTS (SELECT 1 FROM sys.server_principals WHERE name = '$(HubAppLogin)')
BEGIN
    PRINT 'Creando login $(HubAppLogin)';
    DECLARE @createHubLogin NVARCHAR(MAX) = 'CREATE LOGIN [$(HubAppLogin)] WITH PASSWORD = ''$(HubAppPassword)'', CHECK_POLICY = ON, CHECK_EXPIRATION = ON;';
    EXEC (@createHubLogin);
END
ELSE
BEGIN
    PRINT 'Login $(HubAppLogin) ya existe, se omite la creación.';
END;
GO

IF DB_ID('prl_hub') IS NULL
BEGIN
    PRINT 'Creando base de datos prl_hub';
    EXEC ('CREATE DATABASE prl_hub');
END
GO

USE prl_hub;
GO

IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = 'prl_hub_app_role')
BEGIN
    CREATE ROLE prl_hub_app_role;
END;
GO

IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = 'prl_hub_reporting_role')
BEGIN
    CREATE ROLE prl_hub_reporting_role;
END;
GO

/* Concede permisos mínimos necesarios a los roles */
GRANT SELECT, INSERT, UPDATE, DELETE ON SCHEMA :: dbo TO prl_hub_app_role;
GRANT SELECT ON SCHEMA :: dbo TO prl_hub_reporting_role;
GO

IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = '$(HubAppLogin)')
BEGIN
    CREATE USER [$(HubAppLogin)] FOR LOGIN [$(HubAppLogin)];
END;
GO

ALTER ROLE prl_hub_app_role ADD MEMBER [$(HubAppLogin)];
GO

/* Usuario de reporting con permisos solo de lectura */
IF NOT EXISTS (SELECT 1 FROM sys.server_principals WHERE name = '$(ReportingLogin)')
BEGIN
    PRINT 'Creando login $(ReportingLogin)';
    DECLARE @createReportingLogin NVARCHAR(MAX) = 'CREATE LOGIN [$(ReportingLogin)] WITH PASSWORD = ''$(ReportingPassword)'', CHECK_POLICY = ON, CHECK_EXPIRATION = ON;';
    EXEC (@createReportingLogin);
END
ELSE
BEGIN
    PRINT 'Login $(ReportingLogin) ya existe, se omite la creación.';
END;
GO

IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = '$(ReportingLogin)')
BEGIN
    CREATE USER [$(ReportingLogin)] FOR LOGIN [$(ReportingLogin)];
END;
GO

ALTER ROLE prl_hub_reporting_role ADD MEMBER [$(ReportingLogin)];
GO

/* Asigna lectura sobre la base Prevengos para habilitar las vistas federadas */
IF DB_ID('Prevengos') IS NOT NULL
BEGIN
    DECLARE @prevengosSql NVARCHAR(MAX) = N'
        USE [Prevengos];

        IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = ''$(HubAppLogin)'')
        BEGIN
            CREATE USER [$(HubAppLogin)] FOR LOGIN [$(HubAppLogin)];
        END;

        IF NOT EXISTS (
            SELECT 1
            FROM sys.database_role_members drm
            INNER JOIN sys.database_principals rp ON drm.role_principal_id = rp.principal_id
            INNER JOIN sys.database_principals mp ON drm.member_principal_id = mp.principal_id
            WHERE rp.name = ''db_datareader'' AND mp.name = ''$(HubAppLogin)''
        )
        BEGIN
            ALTER ROLE db_datareader ADD MEMBER [$(HubAppLogin)];
        END;

        IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = ''$(ReportingLogin)'')
        BEGIN
            CREATE USER [$(ReportingLogin)] FOR LOGIN [$(ReportingLogin)];
        END;

        IF NOT EXISTS (
            SELECT 1
            FROM sys.database_role_members drm
            INNER JOIN sys.database_principals rp ON drm.role_principal_id = rp.principal_id
            INNER JOIN sys.database_principals mp ON drm.member_principal_id = mp.principal_id
            WHERE rp.name = ''db_datareader'' AND mp.name = ''$(ReportingLogin)''
        )
        BEGIN
            ALTER ROLE db_datareader ADD MEMBER [$(ReportingLogin)];
        END;
    ';

    EXEC (@prevengosSql);
END
ELSE
BEGIN
    PRINT 'Advertencia: la base Prevengos no existe. Ejecuta la creación manualmente si se necesita federación.';
END;
GO

PRINT 'Provisionamiento de roles Prevengos Plug completado.';
GO
