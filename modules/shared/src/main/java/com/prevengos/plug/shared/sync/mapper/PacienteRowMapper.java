package com.prevengos.plug.shared.sync.mapper;

import com.prevengos.plug.shared.sync.dto.PacienteDto;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * RowMapper reutilizable para mapear pacientes desde JDBC.
 */
public class PacienteRowMapper implements RowMapper<PacienteDto> {
    @Override
    public PacienteDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new PacienteDto(
                getUuid(rs, "paciente_id"),
                rs.getString("nif"),
                rs.getString("nombre"),
                rs.getString("apellidos"),
                rs.getObject("fecha_nacimiento", java.time.LocalDate.class),
                rs.getString("sexo"),
                rs.getString("telefono"),
                rs.getString("email"),
                getUuid(rs, "empresa_id"),
                getUuid(rs, "centro_id"),
                rs.getString("externo_ref"),
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
