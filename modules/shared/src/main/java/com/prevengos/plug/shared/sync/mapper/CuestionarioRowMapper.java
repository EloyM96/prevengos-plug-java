package com.prevengos.plug.shared.sync.mapper;

import com.prevengos.plug.shared.sync.dto.CuestionarioDto;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * RowMapper para cuestionarios sincronizados.
 */
public class CuestionarioRowMapper implements RowMapper<CuestionarioDto> {
    @Override
    public CuestionarioDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new CuestionarioDto(
                getUuid(rs, "cuestionario_id"),
                getUuid(rs, "paciente_id"),
                rs.getString("plantilla_codigo"),
                rs.getString("estado"),
                rs.getString("respuestas"),
                rs.getString("firmas"),
                rs.getString("adjuntos"),
                rs.getObject("created_at", OffsetDateTime.class),
                rs.getObject("updated_at", OffsetDateTime.class),
                rs.getObject("last_modified", OffsetDateTime.class),
                rs.getObject("sync_token") == null ? null : rs.getLong("sync_token")
        );
    }

    private UUID getUuid(ResultSet rs, String column) throws SQLException {
        String value = rs.getString(column);
        return value == null ? null : UUID.fromString(value);
    }
}
