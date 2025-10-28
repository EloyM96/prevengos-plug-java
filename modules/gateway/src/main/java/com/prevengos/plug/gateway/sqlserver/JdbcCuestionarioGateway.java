package com.prevengos.plug.gateway.sqlserver;

import com.prevengos.plug.shared.persistence.jdbc.CuestionarioCsvRow;
import com.prevengos.plug.shared.sync.dto.CuestionarioDto;
import com.prevengos.plug.shared.sync.mapper.CuestionarioRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public class JdbcCuestionarioGateway implements CuestionarioGateway {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final CuestionarioRowMapper rowMapper = new CuestionarioRowMapper();

    public JdbcCuestionarioGateway(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void upsert(CuestionarioDto cuestionario, OffsetDateTime lastModified, long syncToken) {
        OffsetDateTime effectiveLastModified = lastModified != null ? lastModified : OffsetDateTime.now();
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("cuestionario_id", cuestionario.cuestionarioId())
                .addValue("paciente_id", uuidToString(cuestionario.pacienteId()))
                .addValue("plantilla_codigo", cuestionario.plantillaCodigo())
                .addValue("estado", cuestionario.estado())
                .addValue("respuestas", cuestionario.respuestas())
                .addValue("firmas", cuestionario.firmas())
                .addValue("adjuntos", cuestionario.adjuntos())
                .addValue("created_at", cuestionario.createdAt() == null ? OffsetDateTime.now() : cuestionario.createdAt())
                .addValue("updated_at", cuestionario.updatedAt() == null ? OffsetDateTime.now() : cuestionario.updatedAt())
                .addValue("last_modified", effectiveLastModified)
                .addValue("sync_token", syncToken);

        String sql = """
                MERGE INTO dbo.cuestionarios AS target
                USING (SELECT :cuestionario_id AS cuestionario_id) AS source
                ON target.cuestionario_id = source.cuestionario_id
                WHEN MATCHED THEN
                    UPDATE SET paciente_id = :paciente_id,
                               plantilla_codigo = :plantilla_codigo,
                               estado = :estado,
                               respuestas = :respuestas,
                               firmas = :firmas,
                               adjuntos = :adjuntos,
                               updated_at = :updated_at,
                               last_modified = :last_modified,
                               sync_token = :sync_token
                WHEN NOT MATCHED THEN
                    INSERT (cuestionario_id, paciente_id, plantilla_codigo, estado, respuestas, firmas, adjuntos,
                            created_at, updated_at, last_modified, sync_token)
                    VALUES (:cuestionario_id, :paciente_id, :plantilla_codigo, :estado, :respuestas, :firmas,
                            :adjuntos, :created_at, :updated_at, :last_modified, :sync_token);
                """;

        jdbcTemplate.update(sql, params);
    }

    @Override
    public List<CuestionarioDto> fetchAfterToken(long token, int limit) {
        String sql = """
                SELECT * FROM dbo.cuestionarios
                WHERE sync_token > :token
                ORDER BY sync_token ASC
                OFFSET 0 ROWS FETCH NEXT :limit ROWS ONLY
                """;
        return jdbcTemplate.query(sql,
                new MapSqlParameterSource()
                        .addValue("token", token)
                        .addValue("limit", limit),
                rowMapper);
    }

    @Override
    public List<CuestionarioCsvRow> fetchForRrhhExport(OffsetDateTime since) {
        String sql = """
                SELECT cuestionario_id, paciente_id, plantilla_codigo, estado, respuestas, firmas, adjuntos, created_at,
                       last_modified
                FROM dbo.cuestionarios
                WHERE last_modified >= :since
                ORDER BY last_modified ASC
                """;
        return jdbcTemplate.query(sql,
                new MapSqlParameterSource().addValue("since", since),
                (rs, rowNum) -> new CuestionarioCsvRow(
                        UUID.fromString(rs.getString("cuestionario_id")),
                        getUuid(rs.getString("paciente_id")),
                        rs.getString("plantilla_codigo"),
                        rs.getString("estado"),
                        rs.getString("respuestas"),
                        rs.getString("firmas"),
                        rs.getString("adjuntos"),
                        rs.getObject("created_at", OffsetDateTime.class),
                        rs.getObject("last_modified", OffsetDateTime.class)
                ));
    }

    @Override
    public CuestionarioDto findById(UUID cuestionarioId) {
        String sql = "SELECT * FROM dbo.cuestionarios WHERE cuestionario_id = :cuestionario_id";
        List<CuestionarioDto> cuestionarios = jdbcTemplate.query(sql,
                new MapSqlParameterSource().addValue("cuestionario_id", cuestionarioId),
                rowMapper);
        return cuestionarios.isEmpty() ? null : cuestionarios.getFirst();
    }

    private UUID getUuid(String value) {
        return value == null ? null : UUID.fromString(value);
    }

    private String uuidToString(UUID value) {
        return value == null ? null : value.toString();
    }
}
