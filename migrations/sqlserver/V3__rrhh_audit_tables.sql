/* Migración SQL Server V3: auditoría de exportaciones RRHH y drops */

IF OBJECT_ID('dbo.rrhh_exports', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.rrhh_exports (
        export_id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
        trace_id UNIQUEIDENTIFIER NOT NULL,
        trigger_type NVARCHAR(64) NOT NULL,
        process_name NVARCHAR(64) NOT NULL,
        origin NVARCHAR(128) NOT NULL,
        operator NVARCHAR(128) NOT NULL,
        remote_path NVARCHAR(512) NULL,
        archive_path NVARCHAR(512) NULL,
        pacientes_count INT NOT NULL,
        cuestionarios_count INT NOT NULL,
        status NVARCHAR(32) NOT NULL,
        message NVARCHAR(512) NULL,
        created_at DATETIMEOFFSET(7) NOT NULL DEFAULT SYSUTCDATETIME()
    );

    CREATE INDEX idx_rrhh_exports_trace ON dbo.rrhh_exports (trace_id);
    CREATE INDEX idx_rrhh_exports_created ON dbo.rrhh_exports (created_at);
END;
GO

IF OBJECT_ID('dbo.file_drop_log', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.file_drop_log (
        log_id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
        trace_id UNIQUEIDENTIFIER NOT NULL,
        process_name NVARCHAR(64) NOT NULL,
        protocol NVARCHAR(32) NOT NULL,
        remote_path NVARCHAR(512) NOT NULL,
        file_name NVARCHAR(160) NOT NULL,
        checksum NVARCHAR(128) NULL,
        status NVARCHAR(32) NOT NULL,
        message NVARCHAR(512) NULL,
        created_at DATETIMEOFFSET(7) NOT NULL DEFAULT SYSUTCDATETIME()
    );

    CREATE INDEX idx_file_drop_log_trace ON dbo.file_drop_log (trace_id);
    CREATE INDEX idx_file_drop_log_created ON dbo.file_drop_log (created_at);
END;
GO
