/* Migración SQL Server V2: tablas operativas para el hub PRL */

IF OBJECT_ID('dbo.pacientes', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.pacientes (
        paciente_id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
        nif NVARCHAR(16) NOT NULL,
        nombre NVARCHAR(160) NOT NULL,
        apellidos NVARCHAR(160) NOT NULL,
        fecha_nacimiento DATE NULL,
        sexo NVARCHAR(1) NOT NULL,
        telefono NVARCHAR(32) NULL,
        email NVARCHAR(160) NULL,
        empresa_id UNIQUEIDENTIFIER NULL,
        centro_id UNIQUEIDENTIFIER NULL,
        externo_ref NVARCHAR(128) NULL,
        created_at DATETIMEOFFSET(7) NOT NULL DEFAULT SYSUTCDATETIME(),
        updated_at DATETIMEOFFSET(7) NOT NULL DEFAULT SYSUTCDATETIME(),
        last_modified DATETIMEOFFSET(7) NOT NULL DEFAULT SYSUTCDATETIME(),
        sync_token BIGINT NOT NULL DEFAULT 0,
        CONSTRAINT chk_pacientes_sexo CHECK (sexo IN ('M', 'F', 'X'))
    );

    CREATE UNIQUE INDEX idx_pacientes_nif ON dbo.pacientes (nif);
    CREATE INDEX idx_pacientes_last_modified ON dbo.pacientes (last_modified);
END;
GO

IF OBJECT_ID('dbo.cuestionarios', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.cuestionarios (
        cuestionario_id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
        paciente_id UNIQUEIDENTIFIER NOT NULL,
        plantilla_codigo NVARCHAR(64) NOT NULL,
        estado NVARCHAR(32) NOT NULL,
        respuestas NVARCHAR(MAX) NULL,
        firmas NVARCHAR(MAX) NULL,
        adjuntos NVARCHAR(MAX) NULL,
        created_at DATETIMEOFFSET(7) NOT NULL DEFAULT SYSUTCDATETIME(),
        updated_at DATETIMEOFFSET(7) NOT NULL DEFAULT SYSUTCDATETIME(),
        last_modified DATETIMEOFFSET(7) NOT NULL DEFAULT SYSUTCDATETIME(),
        sync_token BIGINT NOT NULL DEFAULT 0,
        CONSTRAINT fk_cuestionarios_pacientes FOREIGN KEY (paciente_id) REFERENCES dbo.pacientes (paciente_id),
        CONSTRAINT chk_cuestionarios_estado CHECK (estado IN ('borrador', 'completado', 'validado'))
    );

    CREATE INDEX idx_cuestionarios_last_modified ON dbo.cuestionarios (last_modified);
    CREATE INDEX idx_cuestionarios_paciente ON dbo.cuestionarios (paciente_id);
END;
GO

IF OBJECT_ID('dbo.sync_events', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.sync_events (
        sync_token BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        event_id UNIQUEIDENTIFIER NOT NULL,
        event_type NVARCHAR(128) NOT NULL,
        version INT NOT NULL,
        occurred_at DATETIMEOFFSET(7) NOT NULL,
        source NVARCHAR(128) NOT NULL,
        correlation_id UNIQUEIDENTIFIER NULL,
        causation_id UNIQUEIDENTIFIER NULL,
        payload NVARCHAR(MAX) NULL,
        metadata NVARCHAR(MAX) NULL,
        created_at DATETIMEOFFSET(7) NOT NULL DEFAULT SYSUTCDATETIME()
    );

    CREATE INDEX idx_sync_events_occurred_at ON dbo.sync_events (occurred_at);
END;
GO
