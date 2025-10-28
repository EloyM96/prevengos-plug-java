package com.prevengos.plug.gateway.sqlserver;

import com.prevengos.plug.shared.persistence.jdbc.FileDropLogRecord;
import com.prevengos.plug.shared.persistence.jdbc.RrhhExportRecord;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcRrhhAuditGateway implements RrhhAuditGateway {

    private static final String INSERT_EXPORT_SQL = """
            INSERT INTO rrhh_exports (export_id, trace_id, trigger_type, process_name, origin, operator,
                                      remote_path, archive_path, pacientes_count, cuestionarios_count,
                                      status, message, created_at)
            VALUES (:export_id, :trace_id, :trigger_type, :process_name, :origin, :operator,
                    :remote_path, :archive_path, :pacientes_count, :cuestionarios_count,
                    :status, :message, :created_at);
            """;

    private static final String INSERT_FILE_DROP_SQL = """
            INSERT INTO file_drop_log (log_id, trace_id, process_name, protocol, remote_path, file_name,
                                       checksum, status, message, created_at)
            VALUES (:log_id, :trace_id, :process_name, :protocol, :remote_path, :file_name,
                    :checksum, :status, :message, :created_at);
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcRrhhAuditGateway(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void recordExport(RrhhExportRecord record) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("export_id", record.exportId())
                .addValue("trace_id", record.traceId())
                .addValue("trigger_type", record.trigger())
                .addValue("process_name", record.processName())
                .addValue("origin", record.origin())
                .addValue("operator", record.operator())
                .addValue("remote_path", record.remotePath())
                .addValue("archive_path", record.archivePath())
                .addValue("pacientes_count", record.pacientesCount())
                .addValue("cuestionarios_count", record.cuestionariosCount())
                .addValue("status", record.status())
                .addValue("message", record.message())
                .addValue("created_at", record.createdAt());
        jdbcTemplate.update(INSERT_EXPORT_SQL, params);
    }

    @Override
    public void recordFileDrop(FileDropLogRecord record) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("log_id", record.logId())
                .addValue("trace_id", record.traceId())
                .addValue("process_name", record.processName())
                .addValue("protocol", record.protocol())
                .addValue("remote_path", record.remotePath())
                .addValue("file_name", record.fileName())
                .addValue("checksum", record.checksum())
                .addValue("status", record.status())
                .addValue("message", record.message())
                .addValue("created_at", record.createdAt());
        jdbcTemplate.update(INSERT_FILE_DROP_SQL, params);
    }
}
