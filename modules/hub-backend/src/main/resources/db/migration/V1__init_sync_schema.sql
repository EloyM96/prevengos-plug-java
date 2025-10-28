CREATE TABLE pacientes (
    paciente_id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
    nif NVARCHAR(16) NOT NULL,
    nombre NVARCHAR(255) NOT NULL,
    apellidos NVARCHAR(255) NOT NULL,
    fecha_nacimiento DATE NULL,
    sexo NVARCHAR(1) NOT NULL,
    telefono NVARCHAR(32) NULL,
    email NVARCHAR(255) NULL,
    empresa_id UNIQUEIDENTIFIER NULL,
    centro_id UNIQUEIDENTIFIER NULL,
    externo_ref NVARCHAR(255) NULL,
    created_at DATETIMEOFFSET(7) NULL,
    updated_at DATETIMEOFFSET(7) NULL,
    last_modified DATETIMEOFFSET(7) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    sync_token BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE cuestionarios (
    cuestionario_id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
    paciente_id UNIQUEIDENTIFIER NOT NULL,
    plantilla_codigo NVARCHAR(255) NOT NULL,
    estado NVARCHAR(32) NOT NULL,
    respuestas NVARCHAR(MAX) NOT NULL,
    firmas NVARCHAR(MAX) NULL,
    adjuntos NVARCHAR(MAX) NULL,
    created_at DATETIMEOFFSET(7) NULL,
    updated_at DATETIMEOFFSET(7) NULL,
    last_modified DATETIMEOFFSET(7) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    sync_token BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_cuestionario_paciente FOREIGN KEY (paciente_id) REFERENCES pacientes(paciente_id)
);

CREATE TABLE sync_events (
    sync_token BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    event_id UNIQUEIDENTIFIER NOT NULL,
    event_type NVARCHAR(128) NOT NULL,
    version INT NOT NULL,
    occurred_at DATETIMEOFFSET(7) NOT NULL,
    source NVARCHAR(128) NOT NULL,
    correlation_id UNIQUEIDENTIFIER NULL,
    causation_id UNIQUEIDENTIFIER NULL,
    payload NVARCHAR(MAX) NOT NULL,
    metadata NVARCHAR(MAX) NULL,
    created_at DATETIMEOFFSET(7) NOT NULL DEFAULT SYSDATETIMEOFFSET()
);

CREATE TABLE rrhh_exports (
    export_id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
    trace_id UNIQUEIDENTIFIER NOT NULL,
    trigger_type NVARCHAR(32) NOT NULL,
    process_name NVARCHAR(64) NOT NULL,
    origin NVARCHAR(64) NOT NULL,
    operator NVARCHAR(64) NOT NULL,
    remote_path NVARCHAR(512) NULL,
    archive_path NVARCHAR(512) NULL,
    pacientes_count INT NOT NULL,
    cuestionarios_count INT NOT NULL,
    status NVARCHAR(32) NOT NULL,
    message NVARCHAR(1024) NULL,
    created_at DATETIMEOFFSET(7) NOT NULL DEFAULT SYSDATETIMEOFFSET()
);

CREATE TABLE file_drop_log (
    log_id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
    trace_id UNIQUEIDENTIFIER NOT NULL,
    process_name NVARCHAR(64) NOT NULL,
    protocol NVARCHAR(16) NOT NULL,
    remote_path NVARCHAR(512) NULL,
    file_name NVARCHAR(255) NULL,
    checksum NVARCHAR(128) NULL,
    status NVARCHAR(32) NOT NULL,
    message NVARCHAR(1024) NULL,
    created_at DATETIMEOFFSET(7) NOT NULL DEFAULT SYSDATETIMEOFFSET()
);

CREATE INDEX idx_sync_events_occurred_at ON sync_events (occurred_at);
