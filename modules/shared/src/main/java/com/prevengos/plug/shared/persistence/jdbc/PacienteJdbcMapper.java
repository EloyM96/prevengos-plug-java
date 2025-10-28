package com.prevengos.plug.shared.persistence.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Helper class that centralizes the column-to-record conversion used by both
 * JDBC gateways and import jobs.
 */
public final class PacienteJdbcMapper {

    private PacienteJdbcMapper() {
    }

    public static PacienteRecord mapRecord(ResultSet rs) throws SQLException {
        return new PacienteRecord(
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
                rs.getLong("sync_token")
        );
    }

    public static PacienteCsvRow mapCsvRow(ResultSet rs) throws SQLException {
        return new PacienteCsvRow(
                getUuid(rs, "paciente_id"),
                rs.getString("nif"),
                rs.getString("nombre"),
                rs.getString("apellidos"),
                rs.getString("sexo"),
                rs.getObject("updated_at", OffsetDateTime.class),
                rs.getString("telefono"),
                rs.getString("email"),
                getUuid(rs, "empresa_id"),
                getUuid(rs, "centro_id"),
                rs.getString("externo_ref")
        );
    }

    private static UUID getUuid(ResultSet rs, String column) throws SQLException {
        Object value = rs.getObject(column);
        if (value == null) {
            return null;
        }
        if (value instanceof UUID uuid) {
            return uuid;
        }
        return UUID.fromString(value.toString());
    }
}
