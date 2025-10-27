-- Migración PostgreSQL V2: sync_events e índices de auditoría
-- Requiere V1 ejecutada (tablas pacientes y cuestionarios existentes)

BEGIN;

-- Índices adicionales para consultas por UUID y marcas temporales
CREATE INDEX IF NOT EXISTS idx_pacientes_created_at
    ON pacientes (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_pacientes_updated_at
    ON pacientes (updated_at DESC);

CREATE INDEX IF NOT EXISTS idx_cuestionarios_created_at
    ON cuestionarios (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_cuestionarios_updated_at
    ON cuestionarios (updated_at DESC);

-- Tabla de eventos de sincronización (CDC aplicativo)
CREATE TABLE IF NOT EXISTS sync_events (
    sync_event_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    entity_type TEXT NOT NULL,
    entity_id UUID NOT NULL,
    operation TEXT NOT NULL CHECK (operation IN ('INSERT','UPDATE','DELETE')),
    payload JSONB,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    published_at TIMESTAMPTZ,
    metadata JSONB NOT NULL DEFAULT '{}'::JSONB
);

CREATE INDEX IF NOT EXISTS idx_sync_events_entity
    ON sync_events (entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_sync_events_occurred_at
    ON sync_events (occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_sync_events_published_at
    ON sync_events (published_at DESC NULLS LAST);

-- Función auxiliar para normalizar updated_at
CREATE OR REPLACE FUNCTION touch_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_pacientes_touch_updated
    BEFORE UPDATE ON pacientes
    FOR EACH ROW
    EXECUTE FUNCTION touch_updated_at();

CREATE TRIGGER trg_cuestionarios_touch_updated
    BEFORE UPDATE ON cuestionarios
    FOR EACH ROW
    EXECUTE FUNCTION touch_updated_at();

-- Funciones CDC para poblar sync_events con contratos JSON v1
CREATE OR REPLACE FUNCTION record_sync_event_pacientes()
RETURNS TRIGGER AS $$
DECLARE
    row_data RECORD;
    payload JSONB;
    entity_uuid UUID;
BEGIN
    IF (TG_OP = 'DELETE') THEN
        row_data := OLD;
    ELSE
        row_data := NEW;
    END IF;

    entity_uuid := row_data.paciente_id;

    payload := jsonb_build_object(
        'paciente_id', entity_uuid,
        'nif', row_data.nif,
        'nombre', row_data.nombre,
        'apellidos', row_data.apellidos,
        'fecha_nacimiento', row_data.fecha_nacimiento,
        'sexo', row_data.sexo,
        'telefono', row_data.telefono,
        'email', row_data.email,
        'empresa_id', row_data.empresa_id,
        'centro_id', row_data.centro_id,
        'externo_ref', row_data.externo_ref,
        'created_at', row_data.created_at,
        'updated_at', row_data.updated_at
    );

    INSERT INTO sync_events (entity_type, entity_id, operation, payload)
    VALUES ('pacientes', entity_uuid, TG_OP, payload);

    IF (TG_OP = 'DELETE') THEN
        RETURN OLD;
    ELSE
        RETURN NEW;
    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION record_sync_event_cuestionarios()
RETURNS TRIGGER AS $$
DECLARE
    row_data RECORD;
    payload JSONB;
    entity_uuid UUID;
    respuestas JSONB;
BEGIN
    IF (TG_OP = 'DELETE') THEN
        row_data := OLD;
    ELSE
        row_data := NEW;
    END IF;

    entity_uuid := row_data.cuestionario_id;

    SELECT COALESCE(
        jsonb_agg(
            jsonb_build_object(
                'pregunta_codigo', cr.pregunta_codigo,
                'valor', cr.valor,
                'unidad', cr.unidad,
                'metadata', cr.metadata,
                'created_at', cr.created_at
            ) ORDER BY cr.created_at
        ),
        '[]'::JSONB
    )
    INTO respuestas
    FROM cuestionario_respuestas cr
    WHERE cr.cuestionario_id = entity_uuid;

    payload := jsonb_build_object(
        'cuestionario_id', entity_uuid,
        'paciente_id', row_data.paciente_id,
        'plantilla_codigo', row_data.plantilla_codigo,
        'estado', row_data.estado,
        'respuestas', respuestas,
        'created_at', row_data.created_at,
        'updated_at', row_data.updated_at
    );

    INSERT INTO sync_events (entity_type, entity_id, operation, payload)
    VALUES ('cuestionarios', entity_uuid, TG_OP, payload);

    IF (TG_OP = 'DELETE') THEN
        RETURN OLD;
    ELSE
        RETURN NEW;
    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_pacientes_sync_events
    AFTER INSERT OR UPDATE OR DELETE ON pacientes
    FOR EACH ROW
    EXECUTE FUNCTION record_sync_event_pacientes();

CREATE TRIGGER trg_cuestionarios_sync_events
    AFTER INSERT OR UPDATE OR DELETE ON cuestionarios
    FOR EACH ROW
    EXECUTE FUNCTION record_sync_event_cuestionarios();

COMMIT;
