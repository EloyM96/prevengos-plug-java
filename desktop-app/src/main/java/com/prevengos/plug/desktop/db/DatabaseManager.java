package com.prevengos.plug.desktop.db;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;

public class DatabaseManager {

    private final Path databasePath;

    public DatabaseManager(Path databasePath) {
        this.databasePath = databasePath;
    }

    public void initialize() throws IOException {
        Path parent = databasePath.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }

        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS pacientes (
                        paciente_id TEXT PRIMARY KEY,
                        nif TEXT NOT NULL,
                        nombre TEXT NOT NULL,
                        apellidos TEXT NOT NULL,
                        fecha_nacimiento TEXT,
                        sexo TEXT,
                        telefono TEXT,
                        email TEXT,
                        empresa_id TEXT,
                        centro_id TEXT,
                        externo_ref TEXT,
                        created_at TEXT,
                        updated_at TEXT,
                        last_modified INTEGER NOT NULL,
                        sync_token INTEGER NOT NULL,
                        dirty INTEGER NOT NULL DEFAULT 0
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS cuestionarios (
                        cuestionario_id TEXT PRIMARY KEY,
                        paciente_id TEXT NOT NULL,
                        plantilla_codigo TEXT NOT NULL,
                        estado TEXT NOT NULL,
                        respuestas TEXT,
                        firmas TEXT,
                        adjuntos TEXT,
                        created_at TEXT,
                        updated_at TEXT,
                        last_modified INTEGER NOT NULL,
                        sync_token INTEGER NOT NULL,
                        dirty INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY (paciente_id) REFERENCES pacientes(paciente_id) ON DELETE CASCADE
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS sync_events (
                        event_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        entity_type TEXT NOT NULL,
                        entity_id TEXT NOT NULL,
                        event_type TEXT NOT NULL,
                        payload TEXT NOT NULL,
                        source TEXT NOT NULL,
                        created_at TEXT NOT NULL,
                        sync_token INTEGER NOT NULL
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS metadata (
                        key TEXT PRIMARY KEY,
                        value TEXT NOT NULL
                    )
                    """);
        } catch (SQLException e) {
            throw new IOException("Error inicializando la base de datos", e);
        }
    }

    public Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }

    public static long nowEpochMillis() {
        return Instant.now().toEpochMilli();
    }
}
