package com.prevengos.plug.gateway.sqlserver;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public class JdbcSyncEventGateway implements SyncEventGateway {

    private static final String INSERT_SQL = """
            INSERT INTO sync_events (event_id, event_type, version, occurred_at, source,
                                     correlation_id, causation_id, payload, metadata, created_at)
            VALUES (:event_id, :event_type, :version, :occurred_at, :source,
                    :correlation_id, :causation_id, :payload, :metadata, :created_at);
            """;

    private static final String SELECT_NEXT_SQL = """
            SELECT sync_token, event_id, event_type, version, occurred_at, source,
                   correlation_id, causation_id, payload, metadata
            FROM sync_events
            WHERE (:sync_token IS NULL OR sync_token > :sync_token)
              AND (:since IS NULL OR occurred_at >= :since)
            ORDER BY sync_token ASC
            OFFSET 0 ROWS FETCH NEXT :limit ROWS ONLY;
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final RowMapper<SyncEventRecord> rowMapper = new SyncEventRowMapper();

    public JdbcSyncEventGateway(NamedParameterJdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public SyncEventRecord registerEvent(SyncEventRecord event) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("event_id", event.eventId())
                .addValue("event_type", event.eventType())
                .addValue("version", event.version())
                .addValue("occurred_at", event.occurredAt())
                .addValue("source", event.source())
                .addValue("correlation_id", event.correlationId())
                .addValue("causation_id", event.causationId())
                .addValue("payload", event.payload() != null ? event.payload().toString() : null)
                .addValue("metadata", event.metadata() != null ? event.metadata().toString() : null)
                .addValue("created_at", OffsetDateTime.now());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(INSERT_SQL, params, keyHolder, new String[]{"sync_token"});
        Number key = keyHolder.getKey();
        Long syncToken = key != null ? key.longValue() : null;
        return new SyncEventRecord(
                syncToken,
                event.eventId(),
                event.eventType(),
                event.version(),
                event.occurredAt(),
                event.source(),
                event.correlationId(),
                event.causationId(),
                event.payload(),
                event.metadata()
        );
    }

    @Override
    public List<SyncEventRecord> fetchNextEvents(Long syncToken, OffsetDateTime since, int limit) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("sync_token", syncToken)
                .addValue("since", since)
                .addValue("limit", limit);
        return jdbcTemplate.query(SELECT_NEXT_SQL, params, rowMapper);
    }

    private class SyncEventRowMapper implements RowMapper<SyncEventRecord> {
        @Override
        public SyncEventRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new SyncEventRecord(
                    rs.getLong("sync_token"),
                    getUuid(rs, "event_id"),
                    rs.getString("event_type"),
                    rs.getInt("version"),
                    rs.getObject("occurred_at", OffsetDateTime.class),
                    rs.getString("source"),
                    getUuid(rs, "correlation_id"),
                    getUuid(rs, "causation_id"),
                    readJson(rs.getString("payload")),
                    readJson(rs.getString("metadata"))
            );
        }

        private JsonNode readJson(String value) throws SQLException {
            if (value == null || value.isBlank()) {
                return objectMapper.nullNode();
            }
            try {
                return objectMapper.readTree(value);
            } catch (Exception e) {
                throw new SQLException("Unable to parse JSON column", e);
            }
        }

        private java.util.UUID getUuid(ResultSet rs, String column) throws SQLException {
            Object value = rs.getObject(column);
            if (value == null) {
                return null;
            }
            if (value instanceof java.util.UUID uuid) {
                return uuid;
            }
            return java.util.UUID.fromString(value.toString());
        }
    }
}
