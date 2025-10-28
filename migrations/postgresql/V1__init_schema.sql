-- Migración PostgreSQL V1: esquema base para el hub local
-- Dependencias: extensión uuid-ossp disponible en la instancia

BEGIN;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE pacientes (
    paciente_id UUID PRIMARY KEY,
    nif VARCHAR(16) NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    apellidos VARCHAR(255) NOT NULL,
    fecha_nacimiento DATE,
    sexo CHAR(1) NOT NULL CHECK (sexo IN ('M','F','X')),
    telefono VARCHAR(32),
    email VARCHAR(255),
    empresa_id UUID,
    centro_id UUID,
    externo_ref TEXT,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    last_modified TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    sync_token BIGINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX idx_pacientes_nif ON pacientes (nif);
CREATE INDEX idx_pacientes_empresa ON pacientes (empresa_id);
CREATE INDEX idx_pacientes_last_modified ON pacientes (last_modified DESC);

CREATE TABLE cuestionarios (
    cuestionario_id UUID PRIMARY KEY,
    paciente_id UUID NOT NULL REFERENCES pacientes (paciente_id) ON DELETE CASCADE,
    plantilla_codigo TEXT NOT NULL,
    estado TEXT NOT NULL CHECK (estado IN ('borrador','completado','validado')),
    respuestas JSONB NOT NULL,
    firmas JSONB,
    adjuntos JSONB,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    last_modified TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    sync_token BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_cuestionarios_paciente ON cuestionarios (paciente_id);
CREATE INDEX idx_cuestionarios_estado ON cuestionarios (estado);
CREATE INDEX idx_cuestionarios_last_modified ON cuestionarios (last_modified DESC);

CREATE TABLE cuestionario_respuestas (
    respuesta_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    cuestionario_id UUID NOT NULL REFERENCES cuestionarios (cuestionario_id) ON DELETE CASCADE,
    pregunta_codigo TEXT NOT NULL,
    valor JSONB NOT NULL,
    unidad TEXT,
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_cuestionario_respuestas_cuestionario
    ON cuestionario_respuestas (cuestionario_id);
CREATE INDEX idx_cuestionario_respuestas_pregunta
    ON cuestionario_respuestas (pregunta_codigo);

CREATE TABLE citas (
    cita_id UUID PRIMARY KEY,
    paciente_id UUID NOT NULL REFERENCES pacientes (paciente_id) ON DELETE CASCADE,
    fecha TIMESTAMPTZ NOT NULL,
    tipo TEXT NOT NULL CHECK (tipo IN ('inicial','periodico','extraordinario')),
    estado TEXT NOT NULL CHECK (estado IN ('planificada','en_curso','finalizada')),
    aptitud TEXT CHECK (aptitud IN ('apto','apto_con_limitaciones','no_apto','pendiente')),
    externo_ref TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_citas_paciente_fecha
    ON citas (paciente_id, fecha DESC);
CREATE INDEX idx_citas_estado
    ON citas (estado);

CREATE TABLE adjuntos (
    adjunto_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    owner_type TEXT NOT NULL CHECK (owner_type IN ('cuestionario','cita','paciente')),
    owner_id UUID NOT NULL,
    blob_path TEXT NOT NULL,
    checksum_sha256 CHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (owner_type, owner_id, blob_path)
);

CREATE TABLE sync_events (
    sync_token BIGSERIAL PRIMARY KEY,
    event_id UUID NOT NULL,
    event_type TEXT NOT NULL,
    version INTEGER NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    source TEXT NOT NULL,
    correlation_id UUID,
    causation_id UUID,
    payload JSONB NOT NULL,
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_sync_events_event_id ON sync_events (event_id);
CREATE INDEX idx_sync_events_occurred_at ON sync_events (occurred_at DESC);
CREATE INDEX idx_sync_events_created_at ON sync_events (created_at DESC);

COMMIT;
