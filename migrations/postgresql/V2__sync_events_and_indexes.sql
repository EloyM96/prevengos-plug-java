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

-- Funciones auxiliares en SQL puro para normalizar updated_at
CREATE OR REPLACE FUNCTION touch_pacientes_updated_at()
RETURNS TRIGGER
LANGUAGE SQL
AS $$
    SELECT ROW(
        NEW.paciente_id,
        NEW.nif,
        NEW.nombre,
        NEW.apellidos,
        NEW.fecha_nacimiento,
        NEW.sexo,
        NEW.telefono,
        NEW.email,
        NEW.empresa_id,
        NEW.centro_id,
        NEW.externo_ref,
        NEW.created_at,
        NOW()
    )::pacientes;
$$;

CREATE OR REPLACE FUNCTION touch_cuestionarios_updated_at()
RETURNS TRIGGER
LANGUAGE SQL
AS $$
    SELECT ROW(
        NEW.cuestionario_id,
        NEW.paciente_id,
        NEW.plantilla_codigo,
        NEW.estado,
        NEW.created_at,
        NOW()
    )::cuestionarios;
$$;

CREATE TRIGGER trg_pacientes_touch_updated
    BEFORE UPDATE ON pacientes
    FOR EACH ROW
    EXECUTE FUNCTION touch_pacientes_updated_at();

CREATE TRIGGER trg_cuestionarios_touch_updated
    BEFORE UPDATE ON cuestionarios
    FOR EACH ROW
    EXECUTE FUNCTION touch_cuestionarios_updated_at();

-- Funciones CDC para poblar sync_events con contratos JSON v1 usando SQL
CREATE OR REPLACE FUNCTION record_sync_event_pacientes_insert()
RETURNS TRIGGER
LANGUAGE SQL
AS $$
    INSERT INTO sync_events (entity_type, entity_id, operation, payload)
    VALUES (
        'pacientes',
        NEW.paciente_id,
        'INSERT',
        jsonb_build_object(
            'paciente_id', NEW.paciente_id,
            'nif', NEW.nif,
            'nombre', NEW.nombre,
            'apellidos', NEW.apellidos,
            'fecha_nacimiento', NEW.fecha_nacimiento,
            'sexo', NEW.sexo,
            'telefono', NEW.telefono,
            'email', NEW.email,
            'empresa_id', NEW.empresa_id,
            'centro_id', NEW.centro_id,
            'externo_ref', NEW.externo_ref,
            'created_at', NEW.created_at,
            'updated_at', NEW.updated_at
        )
    );
    SELECT NEW;
$$;

CREATE OR REPLACE FUNCTION record_sync_event_pacientes_update()
RETURNS TRIGGER
LANGUAGE SQL
AS $$
    INSERT INTO sync_events (entity_type, entity_id, operation, payload)
    VALUES (
        'pacientes',
        NEW.paciente_id,
        'UPDATE',
        jsonb_build_object(
            'paciente_id', NEW.paciente_id,
            'nif', NEW.nif,
            'nombre', NEW.nombre,
            'apellidos', NEW.apellidos,
            'fecha_nacimiento', NEW.fecha_nacimiento,
            'sexo', NEW.sexo,
            'telefono', NEW.telefono,
            'email', NEW.email,
            'empresa_id', NEW.empresa_id,
            'centro_id', NEW.centro_id,
            'externo_ref', NEW.externo_ref,
            'created_at', NEW.created_at,
            'updated_at', NEW.updated_at
        )
    );
    SELECT NEW;
$$;

CREATE OR REPLACE FUNCTION record_sync_event_pacientes_delete()
RETURNS TRIGGER
LANGUAGE SQL
AS $$
    INSERT INTO sync_events (entity_type, entity_id, operation, payload)
    VALUES (
        'pacientes',
        OLD.paciente_id,
        'DELETE',
        jsonb_build_object(
            'paciente_id', OLD.paciente_id,
            'nif', OLD.nif,
            'nombre', OLD.nombre,
            'apellidos', OLD.apellidos,
            'fecha_nacimiento', OLD.fecha_nacimiento,
            'sexo', OLD.sexo,
            'telefono', OLD.telefono,
            'email', OLD.email,
            'empresa_id', OLD.empresa_id,
            'centro_id', OLD.centro_id,
            'externo_ref', OLD.externo_ref,
            'created_at', OLD.created_at,
            'updated_at', OLD.updated_at
        )
    );
    SELECT OLD;
$$;

CREATE OR REPLACE FUNCTION record_sync_event_cuestionarios_insert()
RETURNS TRIGGER
LANGUAGE SQL
AS $$
    INSERT INTO sync_events (entity_type, entity_id, operation, payload)
    VALUES (
        'cuestionarios',
        NEW.cuestionario_id,
        'INSERT',
        jsonb_build_object(
            'cuestionario_id', NEW.cuestionario_id,
            'paciente_id', NEW.paciente_id,
            'plantilla_codigo', NEW.plantilla_codigo,
            'estado', NEW.estado,
            'respuestas', COALESCE(
                (
                    SELECT jsonb_agg(
                        jsonb_build_object(
                            'pregunta_codigo', ordered.pregunta_codigo,
                            'valor', ordered.valor,
                            'unidad', ordered.unidad,
                            'metadata', ordered.metadata,
                            'created_at', ordered.created_at
                        )
                    )
                    FROM (
                        SELECT cr.pregunta_codigo,
                               cr.valor,
                               cr.unidad,
                               cr.metadata,
                               cr.created_at
                        FROM cuestionario_respuestas cr
                        WHERE cr.cuestionario_id = NEW.cuestionario_id
                        ORDER BY cr.created_at
                    ) AS ordered
                ),
                '[]'::JSONB
            ),
            'created_at', NEW.created_at,
            'updated_at', NEW.updated_at
        )
    );
    SELECT NEW;
$$;

CREATE OR REPLACE FUNCTION record_sync_event_cuestionarios_update()
RETURNS TRIGGER
LANGUAGE SQL
AS $$
    INSERT INTO sync_events (entity_type, entity_id, operation, payload)
    VALUES (
        'cuestionarios',
        NEW.cuestionario_id,
        'UPDATE',
        jsonb_build_object(
            'cuestionario_id', NEW.cuestionario_id,
            'paciente_id', NEW.paciente_id,
            'plantilla_codigo', NEW.plantilla_codigo,
            'estado', NEW.estado,
            'respuestas', COALESCE(
                (
                    SELECT jsonb_agg(
                        jsonb_build_object(
                            'pregunta_codigo', ordered.pregunta_codigo,
                            'valor', ordered.valor,
                            'unidad', ordered.unidad,
                            'metadata', ordered.metadata,
                            'created_at', ordered.created_at
                        )
                    )
                    FROM (
                        SELECT cr.pregunta_codigo,
                               cr.valor,
                               cr.unidad,
                               cr.metadata,
                               cr.created_at
                        FROM cuestionario_respuestas cr
                        WHERE cr.cuestionario_id = NEW.cuestionario_id
                        ORDER BY cr.created_at
                    ) AS ordered
                ),
                '[]'::JSONB
            ),
            'created_at', NEW.created_at,
            'updated_at', NEW.updated_at
        )
    );
    SELECT NEW;
$$;

CREATE OR REPLACE FUNCTION record_sync_event_cuestionarios_delete()
RETURNS TRIGGER
LANGUAGE SQL
AS $$
    INSERT INTO sync_events (entity_type, entity_id, operation, payload)
    VALUES (
        'cuestionarios',
        OLD.cuestionario_id,
        'DELETE',
        jsonb_build_object(
            'cuestionario_id', OLD.cuestionario_id,
            'paciente_id', OLD.paciente_id,
            'plantilla_codigo', OLD.plantilla_codigo,
            'estado', OLD.estado,
            'respuestas', COALESCE(
                (
                    SELECT jsonb_agg(
                        jsonb_build_object(
                            'pregunta_codigo', ordered.pregunta_codigo,
                            'valor', ordered.valor,
                            'unidad', ordered.unidad,
                            'metadata', ordered.metadata,
                            'created_at', ordered.created_at
                        )
                    )
                    FROM (
                        SELECT cr.pregunta_codigo,
                               cr.valor,
                               cr.unidad,
                               cr.metadata,
                               cr.created_at
                        FROM cuestionario_respuestas cr
                        WHERE cr.cuestionario_id = OLD.cuestionario_id
                        ORDER BY cr.created_at
                    ) AS ordered
                ),
                '[]'::JSONB
            ),
            'created_at', OLD.created_at,
            'updated_at', OLD.updated_at
        )
    );
    SELECT OLD;
$$;

CREATE TRIGGER trg_pacientes_sync_events_insert
    AFTER INSERT ON pacientes
    FOR EACH ROW
    EXECUTE FUNCTION record_sync_event_pacientes_insert();

CREATE TRIGGER trg_pacientes_sync_events_update
    AFTER UPDATE ON pacientes
    FOR EACH ROW
    EXECUTE FUNCTION record_sync_event_pacientes_update();

CREATE TRIGGER trg_pacientes_sync_events_delete
    AFTER DELETE ON pacientes
    FOR EACH ROW
    EXECUTE FUNCTION record_sync_event_pacientes_delete();

CREATE TRIGGER trg_cuestionarios_sync_events_insert
    AFTER INSERT ON cuestionarios
    FOR EACH ROW
    EXECUTE FUNCTION record_sync_event_cuestionarios_insert();

CREATE TRIGGER trg_cuestionarios_sync_events_update
    AFTER UPDATE ON cuestionarios
    FOR EACH ROW
    EXECUTE FUNCTION record_sync_event_cuestionarios_update();

CREATE TRIGGER trg_cuestionarios_sync_events_delete
    AFTER DELETE ON cuestionarios
    FOR EACH ROW
    EXECUTE FUNCTION record_sync_event_cuestionarios_delete();

COMMIT;
