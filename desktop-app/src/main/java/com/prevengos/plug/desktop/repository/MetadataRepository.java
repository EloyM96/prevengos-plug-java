package com.prevengos.plug.desktop.repository;

import com.prevengos.plug.desktop.db.DatabaseManager;
import com.prevengos.plug.desktop.model.SyncMetadata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

public class MetadataRepository {

    private static final String KEY_LAST_TOKEN = "lastSyncToken";
    private static final String KEY_LAST_PULL = "lastPullAt";
    private static final String KEY_LAST_PUSH = "lastPushAt";

    private final DatabaseManager databaseManager;

    public MetadataRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public SyncMetadata readMetadata() {
        Map<String, String> values = new HashMap<>();
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT key, value FROM metadata")) {
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                values.put(rs.getString("key"), rs.getString("value"));
            }
        } catch (SQLException e) {
            throw new RepositoryException("No se pudo leer la metadata de sincronización", e);
        }
        Long lastToken = values.containsKey(KEY_LAST_TOKEN) ? Long.parseLong(values.get(KEY_LAST_TOKEN)) : null;
        OffsetDateTime lastPull = values.containsKey(KEY_LAST_PULL) ? OffsetDateTime.parse(values.get(KEY_LAST_PULL)) : null;
        OffsetDateTime lastPush = values.containsKey(KEY_LAST_PUSH) ? OffsetDateTime.parse(values.get(KEY_LAST_PUSH)) : null;
        return new SyncMetadata(lastToken, lastPull, lastPush);
    }

    public void updateLastToken(long token) {
        upsert(KEY_LAST_TOKEN, Long.toString(token));
    }

    public void updateLastPull(OffsetDateTime at) {
        upsert(KEY_LAST_PULL, at.toString());
    }

    public void updateLastPush(OffsetDateTime at) {
        upsert(KEY_LAST_PUSH, at.toString());
    }

    private void upsert(String key, String value) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     INSERT INTO metadata(key, value) VALUES(?, ?)
                     ON CONFLICT(key) DO UPDATE SET value = excluded.value
                     """)) {
            statement.setString(1, key);
            statement.setString(2, value);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("No se pudo actualizar la metadata" + key, e);
        }
    }
}
