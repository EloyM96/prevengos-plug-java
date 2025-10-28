package com.prevengos.plug.hubbackend.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class DatabaseMigrationsIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Test
    void postgresqlMigrationsApplySuccessfully() throws Exception {
        Path migrationsDir = Path.of("..", "..", "migrations", "postgresql").toAbsolutePath();
        try (Connection connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(connection,
                    new EncodedResource(new FileSystemResource(migrationsDir.resolve("V1__init_schema.sql"))));
            ScriptUtils.executeSqlScript(connection,
                    new EncodedResource(new FileSystemResource(migrationsDir.resolve("V2__sync_events_and_indexes.sql"))));

            try (Statement stmt = connection.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM information_schema.tables " +
                        "WHERE table_name IN ('pacientes','cuestionarios','sync_events')")) {
                    rs.next();
                    assertThat(rs.getInt(1)).isEqualTo(3);
                }
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM information_schema.triggers " +
                        "WHERE trigger_name LIKE 'trg_%_sync_events'")) {
                    rs.next();
                    assertThat(rs.getInt(1)).isEqualTo(2);
                }
            }
        }
    }

    @Test
    void sqliteMigrationCreatesLocalSchema() throws Exception {
        Class.forName("org.sqlite.JDBC");
        Path migrationsDir = Path.of("..", "..", "migrations", "sqlite").toAbsolutePath();
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(connection,
                    new EncodedResource(new FileSystemResource(migrationsDir.resolve("V1__init_schema.sql"))));

            try (Statement stmt = connection.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM sqlite_master " +
                        "WHERE type='table' AND name IN ('pacientes','cuestionarios','citas')")) {
                    rs.next();
                    assertThat(rs.getInt(1)).isEqualTo(3);
                }
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM sqlite_master " +
                        "WHERE type='index' AND name LIKE 'idx_%'")) {
                    rs.next();
                    assertThat(rs.getInt(1)).isGreaterThanOrEqualTo(4);
                }
            }
        }
    }
}
