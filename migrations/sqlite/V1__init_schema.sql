-- Migración SQLite V1: esquema local PRL Desktop alineado con el hub
PRAGMA foreign_keys = ON;

BEGIN TRANSACTION;

CREATE TABLE IF NOT EXISTS sync_metadata (
    key TEXT PRIMARY KEY,
    value TEXT NOT NULL,
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

INSERT OR REPLACE INTO sync_metadata (key, value, updated_at)
VALUES ('schema_version', '1.0.0', datetime('now'));

CREATE TABLE IF NOT EXISTS pacientes (
    paciente_id TEXT PRIMARY KEY,
    nif TEXT NOT NULL,
    nombre TEXT NOT NULL,
    apellidos TEXT NOT NULL,
    fecha_nacimiento TEXT,
    sexo TEXT NOT NULL CHECK (sexo IN ('M','F','X')),
    telefono TEXT,
    email TEXT,
    empresa_id TEXT,
    centro_id TEXT,
    externo_ref TEXT,
    created_at TEXT,
    updated_at TEXT,
    last_modified TEXT NOT NULL DEFAULT (datetime('now')),
    sync_token INTEGER NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_pacientes_nif ON pacientes (nif);
CREATE INDEX IF NOT EXISTS idx_pacientes_last_modified ON pacientes (last_modified DESC);

CREATE TABLE IF NOT EXISTS cuestionarios (
    cuestionario_id TEXT PRIMARY KEY,
    paciente_id TEXT NOT NULL REFERENCES pacientes (paciente_id) ON DELETE CASCADE,
    plantilla_codigo TEXT NOT NULL,
    estado TEXT NOT NULL CHECK (estado IN ('borrador','completado','validado')),
    respuestas TEXT NOT NULL,
    firmas TEXT,
    adjuntos TEXT,
    created_at TEXT,
    updated_at TEXT,
    last_modified TEXT NOT NULL DEFAULT (datetime('now')),
    sync_token INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_cuestionarios_paciente ON cuestionarios (paciente_id);
CREATE INDEX IF NOT EXISTS idx_cuestionarios_last_modified ON cuestionarios (last_modified DESC);

CREATE TABLE IF NOT EXISTS cuestionario_respuestas (
    respuesta_id TEXT PRIMARY KEY,
    cuestionario_id TEXT NOT NULL REFERENCES cuestionarios (cuestionario_id) ON DELETE CASCADE,
    pregunta_codigo TEXT NOT NULL,
    valor TEXT,
    unidad TEXT,
    metadata TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX IF NOT EXISTS idx_cuestionario_respuestas_cuestionario
    ON cuestionario_respuestas (cuestionario_id);

CREATE TABLE IF NOT EXISTS citas (
    cita_id TEXT PRIMARY KEY,
    paciente_id TEXT NOT NULL REFERENCES pacientes (paciente_id) ON DELETE CASCADE,
    fecha TEXT NOT NULL,
    tipo TEXT NOT NULL,
    estado TEXT NOT NULL,
    aptitud TEXT,
    externo_ref TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX IF NOT EXISTS idx_citas_paciente_fecha
    ON citas (paciente_id, fecha DESC);

CREATE TABLE IF NOT EXISTS pending_mutations (
    mutation_id INTEGER PRIMARY KEY AUTOINCREMENT,
    entity_type TEXT NOT NULL,
    entity_id TEXT NOT NULL,
    payload TEXT NOT NULL,
    operation TEXT NOT NULL CHECK (operation IN ('UPSERT','DELETE')),
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX IF NOT EXISTS idx_pending_mutations_entity
    ON pending_mutations (entity_type, entity_id);

COMMIT;
