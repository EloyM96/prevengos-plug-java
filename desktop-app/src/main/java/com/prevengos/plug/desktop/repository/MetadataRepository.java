package com.prevengos.plug.desktop.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Repositorio para valores simples almacenados en la tabla {@code metadata}.
 */
public class MetadataRepository {

    private final DatabaseManager databaseManager;

    public MetadataRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public Optional<String> get(String key) {
        String sql = "SELECT value FROM metadata WHERE key = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, key);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.ofNullable(rs.getString("value"));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo leer metadata", e);
        }
    }

    public void put(String key, String value) {
        String sql = "INSERT INTO metadata(key, value) VALUES(?, ?) ON CONFLICT(key) DO UPDATE SET value = excluded.value";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, key);
            statement.setString(2, value);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo almacenar metadata", e);
        }
    }
}
