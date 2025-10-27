-- Migración PostgreSQL V1: esquema inicial PRL Hub
-- Dependencias: extensión uuid-ossp disponible

BEGIN;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE event_envelopes (
    event_id UUID PRIMARY KEY,
    event_type TEXT NOT NULL,
    event_version INTEGER NOT NULL DEFAULT 1,
    occurred_at TIMESTAMPTZ NOT NULL,
    source TEXT NOT NULL,
    correlation_id UUID,
    payload JSONB NOT NULL,
    received_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT event_envelopes_event_version_positive CHECK (event_version > 0)
);

CREATE INDEX idx_event_envelopes_type_occurred
    ON event_envelopes (event_type, occurred_at DESC);
CREATE INDEX idx_event_envelopes_correlation
    ON event_envelopes (correlation_id);

CREATE TABLE pacientes (
    paciente_id UUID PRIMARY KEY,
    nif VARCHAR(16) NOT NULL,
    nombre VARCHAR(120) NOT NULL,
    apellidos VARCHAR(160) NOT NULL,
    fecha_nacimiento DATE NOT NULL,
    sexo CHAR(1) NOT NULL CHECK (sexo IN ('M','F','X')),
    telefono VARCHAR(32),
    email VARCHAR(160),
    empresa_id UUID,
    centro_id UUID,
    externo_ref TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_pacientes_nif ON pacientes (nif);
CREATE INDEX idx_pacientes_empresa ON pacientes (empresa_id);

CREATE TABLE cuestionarios (
    cuestionario_id UUID PRIMARY KEY,
    paciente_id UUID NOT NULL REFERENCES pacientes (paciente_id) ON DELETE CASCADE,
    plantilla_codigo TEXT NOT NULL,
    estado TEXT NOT NULL CHECK (estado IN ('borrador','completado','validado')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_cuestionarios_paciente ON cuestionarios (paciente_id);
CREATE INDEX idx_cuestionarios_estado ON cuestionarios (estado);

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

CREATE MATERIALIZED VIEW mv_pacientes_citas AS
SELECT
    p.paciente_id,
    p.nombre,
    p.apellidos,
    p.nif,
    MAX(c.fecha) AS ultima_cita,
    MAX(c.estado) FILTER (WHERE c.fecha = MAX(c.fecha)) AS estado_ultima_cita,
    MAX(c.aptitud) FILTER (WHERE c.fecha = MAX(c.fecha)) AS aptitud_ultima_cita
FROM pacientes p
LEFT JOIN citas c ON c.paciente_id = p.paciente_id
GROUP BY p.paciente_id, p.nombre, p.apellidos, p.nif;

CREATE UNIQUE INDEX idx_mv_pacientes_citas ON mv_pacientes_citas (paciente_id);

COMMIT;
