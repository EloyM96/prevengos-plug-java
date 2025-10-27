CREATE TABLE pacientes (
    paciente_id UUID PRIMARY KEY,
    nif VARCHAR(16) NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    apellidos VARCHAR(255) NOT NULL,
    fecha_nacimiento DATE,
    sexo VARCHAR(1) NOT NULL,
    telefono VARCHAR(32),
    email VARCHAR(255),
    empresa_id UUID,
    centro_id UUID,
    externo_ref VARCHAR(255),
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    last_modified TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    sync_token BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE cuestionarios (
    cuestionario_id UUID PRIMARY KEY,
    paciente_id UUID NOT NULL,
    plantilla_codigo VARCHAR(255) NOT NULL,
    estado VARCHAR(32) NOT NULL,
    respuestas JSONB NOT NULL,
    firmas JSONB,
    adjuntos JSONB,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    last_modified TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    sync_token BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_cuestionario_paciente FOREIGN KEY (paciente_id) REFERENCES pacientes(paciente_id)
);

CREATE TABLE sync_events (
    sync_token BIGSERIAL PRIMARY KEY,
    event_id UUID NOT NULL,
    event_type VARCHAR(128) NOT NULL,
    version INTEGER NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    source VARCHAR(128) NOT NULL,
    correlation_id UUID,
    causation_id UUID,
    payload JSONB NOT NULL,
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_sync_events_occurred_at ON sync_events (occurred_at);
