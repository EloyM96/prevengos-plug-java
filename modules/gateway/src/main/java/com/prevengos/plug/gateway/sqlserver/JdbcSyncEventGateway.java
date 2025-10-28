package com.prevengos.plug.gateway.sqlserver;

import com.prevengos.plug.shared.sync.dto.SyncEventDto;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public class JdbcSyncEventGateway implements SyncEventGateway {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcSyncEventGateway(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public long registerEvent(UUID eventId, String eventType, int version, OffsetDateTime occurredAt, String source,
                              UUID correlationId, UUID causationId, String payload, String metadata) {
        String sql = """
                INSERT INTO dbo.sync_events (event_id, event_type, version, occurred_at, source, correlation_id,
                                             causation_id, payload, metadata)
                VALUES (:event_id, :event_type, :version, :occurred_at, :source, :correlation_id, :causation_id,
                        :payload, :metadata)
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("event_id", eventId)
                .addValue("event_type", eventType)
                .addValue("version", version)
                .addValue("occurred_at", occurredAt)
                .addValue("source", source)
                .addValue("correlation_id", correlationId)
                .addValue("causation_id", causationId)
                .addValue("payload", payload)
                .addValue("metadata", metadata);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, params, keyHolder, new String[]{"sync_token"});
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("No se obtuvo sync_token generado");
        }
        return key.longValue();
    }

    @Override
    public List<SyncEventDto> fetchAfter(long token, int limit) {
        String sql = """
                SELECT sync_token, event_id, event_type, version, occurred_at, source, correlation_id, causation_id,
                       payload, metadata
                FROM dbo.sync_events
                WHERE sync_token > :token
                ORDER BY sync_token ASC
                OFFSET 0 ROWS FETCH NEXT :limit ROWS ONLY
                """;
        return jdbcTemplate.query(sql,
                new MapSqlParameterSource()
                        .addValue("token", token)
                        .addValue("limit", limit),
                (rs, rowNum) -> new SyncEventDto(
                        rs.getLong("sync_token"),
                        UUID.fromString(rs.getString("event_id")),
                        rs.getString("event_type"),
                        rs.getInt("version"),
                        rs.getObject("occurred_at", OffsetDateTime.class),
                        rs.getString("source"),
                        getUuid(rs.getString("correlation_id")),
                        getUuid(rs.getString("causation_id")),
                        rs.getString("payload"),
                        rs.getString("metadata")
                ));
    }

    private UUID getUuid(String value) {
        return value == null ? null : UUID.fromString(value);
    }
}
