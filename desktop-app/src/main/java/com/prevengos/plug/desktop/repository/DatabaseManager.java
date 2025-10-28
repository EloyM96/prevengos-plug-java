package com.prevengos.plug.desktop.repository;

import java.io.Closeable;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Gestiona la conexión y la inicialización de la base de datos SQLite local.
 */
public class DatabaseManager implements Closeable {

    private final String jdbcUrl;

    public DatabaseManager(Path databasePath) {
        this.jdbcUrl = "jdbc:sqlite:" + databasePath.toAbsolutePath();
        initialize();
    }

    private void initialize() {
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS patients (
                        id TEXT PRIMARY KEY,
                        first_name TEXT NOT NULL,
                        last_name TEXT NOT NULL,
                        document_number TEXT,
                        birth_date TEXT,
                        created_at TEXT NOT NULL,
                        updated_at TEXT NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS questionnaires (
                        id TEXT PRIMARY KEY,
                        patient_id TEXT NOT NULL,
                        title TEXT NOT NULL,
                        responses TEXT,
                        created_at TEXT NOT NULL,
                        updated_at TEXT NOT NULL,
                        FOREIGN KEY(patient_id) REFERENCES patients(id) ON DELETE CASCADE
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS sync_events (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        entity_type TEXT NOT NULL,
                        entity_id TEXT NOT NULL,
                        event_type TEXT NOT NULL,
                        payload TEXT NOT NULL,
                        sync_token INTEGER,
                        created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS metadata (
                        key TEXT PRIMARY KEY,
                        value TEXT NOT NULL
                    )
                    """);
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo inicializar la base de datos", e);
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl);
    }

    @Override
    public void close() {
        // SQLite maneja conexiones a demanda, no es necesario cerrar recursos globales.
    }
}
