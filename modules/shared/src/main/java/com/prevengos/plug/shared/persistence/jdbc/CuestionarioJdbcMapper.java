package com.prevengos.plug.shared.persistence.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.UUID;

public final class CuestionarioJdbcMapper {

    private CuestionarioJdbcMapper() {
    }

    public static CuestionarioRecord mapRecord(ResultSet rs) throws SQLException {
        return new CuestionarioRecord(
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
                rs.getLong("sync_token")
        );
    }

    public static CuestionarioCsvRow mapCsvRow(ResultSet rs) throws SQLException {
        return new CuestionarioCsvRow(
                getUuid(rs, "cuestionario_id"),
                getUuid(rs, "paciente_id"),
                rs.getString("plantilla_codigo"),
                rs.getString("estado"),
                rs.getString("respuestas"),
                rs.getString("firmas"),
                rs.getString("adjuntos"),
                rs.getObject("created_at", OffsetDateTime.class),
                rs.getObject("updated_at", OffsetDateTime.class)
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
