-- Consulta rápida de la última exportación RRHH realizada por el hub
SELECT trace_id,
       remote_path,
       pacientes_count,
       cuestionarios_count,
       status,
       created_at
FROM rrhh_exports
ORDER BY created_at DESC
LIMIT 1;

SELECT log_id,
       file_name,
       protocol,
       status,
       message,
       created_at
FROM file_drop_log
WHERE trace_id = (
    SELECT trace_id
    FROM rrhh_exports
    ORDER BY created_at DESC
    LIMIT 1
);
