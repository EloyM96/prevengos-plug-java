-- Migración PostgreSQL V3: tablas de auditoría RRHH

CREATE TABLE IF NOT EXISTS rrhh_exports (
    export_id UUID PRIMARY KEY,
    trace_id UUID NOT NULL,
    trigger_type VARCHAR(64) NOT NULL,
    process_name VARCHAR(64) NOT NULL,
    origin VARCHAR(128) NOT NULL,
    operator VARCHAR(128) NOT NULL,
    remote_path TEXT,
    archive_path TEXT,
    pacientes_count INTEGER NOT NULL,
    cuestionarios_count INTEGER NOT NULL,
    status VARCHAR(32) NOT NULL,
    message TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_rrhh_exports_trace ON rrhh_exports (trace_id);
CREATE INDEX IF NOT EXISTS idx_rrhh_exports_created ON rrhh_exports (created_at);

CREATE TABLE IF NOT EXISTS file_drop_log (
    log_id UUID PRIMARY KEY,
    trace_id UUID NOT NULL,
    process_name VARCHAR(64) NOT NULL,
    protocol VARCHAR(32) NOT NULL,
    remote_path TEXT NOT NULL,
    file_name VARCHAR(160) NOT NULL,
    checksum VARCHAR(128),
    status VARCHAR(32) NOT NULL,
    message TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_file_drop_log_trace ON file_drop_log (trace_id);
CREATE INDEX IF NOT EXISTS idx_file_drop_log_created ON file_drop_log (created_at);
