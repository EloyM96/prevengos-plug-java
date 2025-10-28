-- Migración SQLite V2: tablas de auditoría RRHH

BEGIN TRANSACTION;

CREATE TABLE IF NOT EXISTS rrhh_exports (
    export_id TEXT PRIMARY KEY,
    trace_id TEXT NOT NULL,
    trigger_type TEXT NOT NULL,
    process_name TEXT NOT NULL,
    origin TEXT NOT NULL,
    operator TEXT NOT NULL,
    remote_path TEXT,
    archive_path TEXT,
    pacientes_count INTEGER NOT NULL,
    cuestionarios_count INTEGER NOT NULL,
    status TEXT NOT NULL,
    message TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX IF NOT EXISTS idx_rrhh_exports_trace ON rrhh_exports (trace_id);
CREATE INDEX IF NOT EXISTS idx_rrhh_exports_created ON rrhh_exports (created_at);

CREATE TABLE IF NOT EXISTS file_drop_log (
    log_id TEXT PRIMARY KEY,
    trace_id TEXT NOT NULL,
    process_name TEXT NOT NULL,
    protocol TEXT NOT NULL,
    remote_path TEXT NOT NULL,
    file_name TEXT NOT NULL,
    checksum TEXT,
    status TEXT NOT NULL,
    message TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX IF NOT EXISTS idx_file_drop_log_trace ON file_drop_log (trace_id);
CREATE INDEX IF NOT EXISTS idx_file_drop_log_created ON file_drop_log (created_at);

COMMIT;
