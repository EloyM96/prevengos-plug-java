-- Migración PostgreSQL V2: triggers e índices adicionales para sincronización
-- Requiere V1 ejecutada previamente

BEGIN;

CREATE OR REPLACE FUNCTION touch_last_modified()
RETURNS TRIGGER AS $$
BEGIN
    NEW.last_modified = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_pacientes_touch_last_modified
    BEFORE UPDATE ON pacientes
    FOR EACH ROW
    EXECUTE FUNCTION touch_last_modified();

CREATE TRIGGER trg_cuestionarios_touch_last_modified
    BEFORE UPDATE ON cuestionarios
    FOR EACH ROW
    EXECUTE FUNCTION touch_last_modified();

COMMIT;
