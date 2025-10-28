package com.prevengos.plug.desktop.repository;

import com.prevengos.plug.desktop.model.SyncEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistencia básica de eventos de sincronización para diagnósticos y exportación manual.
 */
public class SyncEventRepository {

    private final DatabaseManager databaseManager;

    public SyncEventRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void record(String entityType, String entityId, String eventType, String payload, Long syncToken) {
        String sql = """
                INSERT INTO sync_events(entity_type, entity_id, event_type, payload, sync_token, created_at)
                VALUES(?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, entityType);
            statement.setString(2, entityId);
            statement.setString(3, eventType);
            statement.setString(4, payload);
            if (syncToken == null) {
                statement.setObject(5, null);
            } else {
                statement.setLong(5, syncToken);
            }
            statement.setString(6, Instant.now().toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo registrar el evento de sincronización", e);
        }
    }

    public List<SyncEvent> findAll() {
        String sql = "SELECT id, entity_type, entity_id, event_type, payload, sync_token, created_at FROM sync_events ORDER BY id DESC";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            List<SyncEvent> events = new ArrayList<>();
            while (rs.next()) {
                events.add(new SyncEvent(
                        rs.getLong("id"),
                        rs.getString("entity_type"),
                        rs.getString("entity_id"),
                        rs.getString("event_type"),
                        rs.getString("payload"),
                        rs.getObject("sync_token") != null ? rs.getLong("sync_token") : null,
                        Instant.parse(rs.getString("created_at"))
                ));
            }
            return events;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo obtener eventos de sincronización", e);
        }
    }
}
