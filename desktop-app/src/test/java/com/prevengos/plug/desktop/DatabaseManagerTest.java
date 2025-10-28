package com.prevengos.plug.desktop;

import com.prevengos.plug.desktop.repository.DatabaseManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseManagerTest {

    @Test
    void createsRequiredTables(@TempDir Path tempDir) throws SQLException {
        Path dbPath = tempDir.resolve("test.db");
        DatabaseManager databaseManager = new DatabaseManager(dbPath);

        try (Connection connection = databaseManager.getConnection()) {
            assertTrue(tableExists(connection, "patients"));
            assertTrue(tableExists(connection, "questionnaires"));
            assertTrue(tableExists(connection, "metadata"));
            assertTrue(tableExists(connection, "sync_events"));
        }
    }

    private boolean tableExists(Connection connection, String table) throws SQLException {
        try (ResultSet resultSet = connection.getMetaData().getTables(null, null, table, null)) {
            return resultSet.next();
        }
    }
}
