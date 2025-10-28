package com.prevengos.plug.desktop.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prevengos.plug.desktop.db.DatabaseManager;
import com.prevengos.plug.desktop.model.SyncEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class SyncEventRepository {

    private final DatabaseManager databaseManager;
    private final ObjectMapper mapper;

    public SyncEventRepository(DatabaseManager databaseManager) {
        this(databaseManager, new ObjectMapper());
    }

    public SyncEventRepository(DatabaseManager databaseManager, ObjectMapper mapper) {
        this.databaseManager = databaseManager;
        this.mapper = mapper;
    }

    public void append(String entityType, String entityId, String eventType, Object payload, String source, long syncToken) {
        String serialized;
        try {
            serialized = mapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new RepositoryException("No se pudo serializar el evento de sincronización", e);
        }

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     INSERT INTO sync_events (entity_type, entity_id, event_type, payload, source, created_at, sync_token)
                     VALUES (?, ?, ?, ?, ?, ?, ?)
                     """)) {
            statement.setString(1, entityType);
            statement.setString(2, entityId);
            statement.setString(3, eventType);
            statement.setString(4, serialized);
            statement.setString(5, source);
            statement.setString(6, OffsetDateTime.now(ZoneOffset.UTC).toString());
            statement.setLong(7, syncToken);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("No se pudo registrar el evento de sincronización", e);
        }
    }

    public List<SyncEvent> findLatest(int limit) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT * FROM sync_events ORDER BY event_id DESC LIMIT ?
                     """)) {
            statement.setInt(1, limit);
            ResultSet rs = statement.executeQuery();
            List<SyncEvent> events = new ArrayList<>();
            while (rs.next()) {
                events.add(mapRow(rs));
            }
            return events;
        } catch (SQLException e) {
            throw new RepositoryException("No se pudieron consultar los eventos de sincronización", e);
        }
    }

    private SyncEvent mapRow(ResultSet rs) throws SQLException {
        return new SyncEvent(
                rs.getLong("event_id"),
                rs.getString("entity_type"),
                rs.getString("entity_id"),
                rs.getString("event_type"),
                rs.getString("payload"),
                rs.getString("source"),
                OffsetDateTime.parse(rs.getString("created_at")),
                rs.getLong("sync_token")
        );
    }
}
