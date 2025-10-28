package com.prevengos.plug.shared.persistence.jdbc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.UUID;

public final class SyncEventJdbcMapper {

    private SyncEventJdbcMapper() {
    }

    public static SyncEventRecord mapRecord(ResultSet rs, ObjectMapper objectMapper) throws SQLException {
        return new SyncEventRecord(
                rs.getLong("sync_token"),
                getUuid(rs, "event_id"),
                rs.getString("event_type"),
                rs.getInt("version"),
                rs.getObject("occurred_at", OffsetDateTime.class),
                rs.getString("source"),
                getUuid(rs, "correlation_id"),
                getUuid(rs, "causation_id"),
                readJson(objectMapper, rs.getString("payload")),
                readJson(objectMapper, rs.getString("metadata"))
        );
    }

    private static JsonNode readJson(ObjectMapper objectMapper, String value) throws SQLException {
        if (value == null || value.isBlank()) {
            return objectMapper.nullNode();
        }
        try {
            return objectMapper.readTree(value);
        } catch (Exception e) {
            throw new SQLException("Unable to parse JSON column", e);
        }
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
