package com.prevengos.plug.desktop.db;

import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class SQLiteConnectionProvider implements ConnectionProvider {

    private static final String SCHEMA_RESOURCE = "/com/prevengos/plug/desktop/db/sqlite_init.sql";

    private final SQLiteDataSource dataSource;

    public SQLiteConnectionProvider(Path databasePath) {
        try {
            Path parent = databasePath.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to create SQLite directory", ex);
        }

        SQLiteConfig config = new SQLiteConfig();
        config.enforceForeignKeys(true);
        config.busyTimeout(5000);

        this.dataSource = new SQLiteDataSource(config);
        this.dataSource.setUrl("jdbc:sqlite:" + databasePath.toAbsolutePath());

        initializeSchema();
    }

    private void initializeSchema() {
        try (Connection connection = dataSource.getConnection()) {
            runSqlScript(connection);
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to initialize SQLite schema", ex);
        }
    }

    private void runSqlScript(Connection connection) {
        try (InputStream in = SQLiteConnectionProvider.class.getResourceAsStream(SCHEMA_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Missing schema resource: " + SCHEMA_RESOURCE);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                StringBuilder statementBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.startsWith("--") || trimmed.isEmpty()) {
                        continue;
                    }
                    statementBuilder.append(line).append('\n');
                    if (trimmed.endsWith(";")) {
                        executeStatement(connection, statementBuilder);
                        statementBuilder.setLength(0);
                    }
                }
                if (statementBuilder.length() > 0) {
                    executeStatement(connection, statementBuilder);
                }
            }
        } catch (IOException | SQLException ex) {
            throw new IllegalStateException("Failed to execute SQLite schema script", ex);
        }
    }

    private void executeStatement(Connection connection, StringBuilder statementBuilder) throws SQLException {
        String sql = statementBuilder.toString().trim();
        if (sql.isEmpty()) {
            return;
        }
        if (sql.endsWith(";")) {
            sql = sql.substring(0, sql.length() - 1).trim();
        }
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
