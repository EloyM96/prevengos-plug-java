package com.prevengos.plug.gateway.sqlserver;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Repository
public class JdbcCuestionarioGateway implements CuestionarioGateway {

    private static final String UPSERT_SQL = """
            MERGE INTO cuestionarios AS target
            USING (SELECT :cuestionario_id AS cuestionario_id) AS source
            ON target.cuestionario_id = source.cuestionario_id
            WHEN MATCHED THEN UPDATE SET
                paciente_id = :paciente_id,
                plantilla_codigo = :plantilla_codigo,
                estado = :estado,
                respuestas = :respuestas,
                firmas = :firmas,
                adjuntos = :adjuntos,
                created_at = :created_at,
                updated_at = :updated_at,
                last_modified = :last_modified,
                sync_token = :sync_token
            WHEN NOT MATCHED THEN
                INSERT (cuestionario_id, paciente_id, plantilla_codigo, estado, respuestas, firmas, adjuntos,
                        created_at, updated_at, last_modified, sync_token)
                VALUES (:cuestionario_id, :paciente_id, :plantilla_codigo, :estado, :respuestas, :firmas,
                        :adjuntos, :created_at, :updated_at, :last_modified, :sync_token);
            """;

    private static final String SELECT_UPDATED_SQL = """
            SELECT cuestionario_id, paciente_id, plantilla_codigo, estado, CAST(respuestas AS NVARCHAR(MAX)) AS respuestas,
                   CAST(firmas AS NVARCHAR(MAX)) AS firmas, CAST(adjuntos AS NVARCHAR(MAX)) AS adjuntos,
                   created_at, updated_at, last_modified, sync_token
            FROM cuestionarios
            WHERE (:since IS NULL OR last_modified >= :since)
            ORDER BY last_modified ASC
            OFFSET 0 ROWS FETCH NEXT :limit ROWS ONLY;
            """;

    private static final String SELECT_BY_PACIENTE_SQL = """
            SELECT cuestionario_id, paciente_id, plantilla_codigo, estado, CAST(respuestas AS NVARCHAR(MAX)) AS respuestas,
                   CAST(firmas AS NVARCHAR(MAX)) AS firmas, CAST(adjuntos AS NVARCHAR(MAX)) AS adjuntos,
                   created_at, updated_at, last_modified, sync_token
            FROM cuestionarios
            WHERE paciente_id = :paciente_id
            ORDER BY last_modified ASC;
            """;

    private static final String SELECT_RRHH_VIEW_SQL = """
            SELECT cuestionario_id, paciente_id, plantilla_codigo, estado, updated_at
            FROM dbo.vw_prl_cuestionarios
            WHERE (:since IS NULL OR updated_at >= :since)
            ORDER BY updated_at ASC;
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RowMapper<CuestionarioRecord> cuestionarioRowMapper = new CuestionarioRecordRowMapper();
    private final RowMapper<CuestionarioCsvRow> cuestionarioCsvRowMapper = new CuestionarioCsvRowMapper();

    public JdbcCuestionarioGateway(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void upsertCuestionario(CuestionarioRecord cuestionario) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("cuestionario_id", cuestionario.cuestionarioId())
                .addValue("paciente_id", cuestionario.pacienteId())
                .addValue("plantilla_codigo", cuestionario.plantillaCodigo())
                .addValue("estado", cuestionario.estado())
                .addValue("respuestas", cuestionario.respuestas())
                .addValue("firmas", cuestionario.firmas())
                .addValue("adjuntos", cuestionario.adjuntos())
                .addValue("created_at", cuestionario.createdAt())
                .addValue("updated_at", cuestionario.updatedAt())
                .addValue("last_modified", cuestionario.lastModified())
                .addValue("sync_token", cuestionario.syncToken());
        jdbcTemplate.update(UPSERT_SQL, params);
    }

    @Override
    public List<CuestionarioRecord> findUpdatedSince(OffsetDateTime since, int limit) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("since", since)
                .addValue("limit", limit);
        return jdbcTemplate.query(SELECT_UPDATED_SQL, params, cuestionarioRowMapper);
    }

    @Override
    public List<CuestionarioRecord> findByPacienteId(UUID pacienteId) {
        if (pacienteId == null) {
            return Collections.emptyList();
        }
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("paciente_id", pacienteId);
        return jdbcTemplate.query(SELECT_BY_PACIENTE_SQL, params, cuestionarioRowMapper);
    }

    @Override
    public List<CuestionarioCsvRow> fetchForRrhhExport(OffsetDateTime updatedSince) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("since", updatedSince);
        return jdbcTemplate.query(SELECT_RRHH_VIEW_SQL, params, cuestionarioCsvRowMapper);
    }

    private static class CuestionarioRecordRowMapper implements RowMapper<CuestionarioRecord> {
        @Override
        public CuestionarioRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new CuestionarioRecord(
                    rs.getObject("cuestionario_id", UUID.class),
                    rs.getObject("paciente_id", UUID.class),
                    rs.getString("plantilla_codigo"),
                    rs.getString("estado"),
                    rs.getString("respuestas"),
                    rs.getString("firmas"),
                    rs.getString("adjuntos"),
                    rs.getObject("created_at", OffsetDateTime.class),
                    rs.getObject("updated_at", OffsetDateTime.class),
                    rs.getObject("last_modified", OffsetDateTime.class),
                    rs.getLong("sync_token")
            );
        }
    }

    private static class CuestionarioCsvRowMapper implements RowMapper<CuestionarioCsvRow> {
        @Override
        public CuestionarioCsvRow mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new CuestionarioCsvRow(
                    rs.getObject("cuestionario_id", UUID.class),
                    rs.getObject("paciente_id", UUID.class),
                    rs.getString("plantilla_codigo"),
                    rs.getString("estado"),
                    rs.getObject("updated_at", OffsetDateTime.class)
            );
        }
    }
}
