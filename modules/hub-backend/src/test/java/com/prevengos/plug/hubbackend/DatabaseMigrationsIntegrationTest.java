package com.prevengos.plug.hubbackend;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class DatabaseMigrationsIntegrationTest {

    private static final Path POSTGRESQL_MIGRATIONS = Path.of("migrations", "postgresql").toAbsolutePath();
    private static final Path SQLITE_MIGRATIONS = Path.of("migrations", "sqlite").toAbsolutePath();
    private static final Path SQLSERVER_MIGRATIONS = Path.of("migrations", "sqlserver").toAbsolutePath();

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("prevengos_schema")
            .withUsername("prevengos")
            .withPassword("prevengos");

    @Container
    static final MSSQLServerContainer<?> SQLSERVER = new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:2022-CU12-ubuntu-22.04")
            .acceptLicense()
            .withPassword("Prevengos#2024")
            .withDatabaseName("prevengos_schema");

    @Test
    void postgresMigrationsCreateExpectedSchema() throws SQLException {
        Flyway flyway = Flyway.configure()
                .cleanDisabled(false)
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("filesystem:" + POSTGRESQL_MIGRATIONS)
                .load();
        flyway.clean();
        flyway.migrate();

        try (Connection connection = DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())) {
            DatabaseMetaData metaData = connection.getMetaData();
            assertColumns(metaData, "pacientes",
                    "paciente_id", "nif", "nombre", "apellidos", "fecha_nacimiento", "sexo",
                    "telefono", "email", "empresa_id", "centro_id", "externo_ref",
                    "created_at", "updated_at", "last_modified", "sync_token");
            assertColumns(metaData, "cuestionarios",
                    "cuestionario_id", "paciente_id", "plantilla_codigo", "estado", "respuestas",
                    "firmas", "adjuntos", "created_at", "updated_at", "last_modified", "sync_token");
            assertColumns(metaData, "sync_events",
                    "sync_token", "event_id", "event_type", "version", "occurred_at",
                    "source", "correlation_id", "causation_id", "payload", "metadata", "created_at");
        }
    }

    @Test
    void sqliteMigrationAlignsWithPlan() throws SQLException, IOException {
        Path databaseFile = Files.createTempFile("prevengos-sqlite-", ".db");
        String jdbcUrl = "jdbc:sqlite:" + databaseFile.toAbsolutePath();
        try {
            Flyway flyway = Flyway.configure()
                    .cleanDisabled(false)
                    .dataSource(jdbcUrl, "", "")
                    .locations("filesystem:" + SQLITE_MIGRATIONS)
                    .load();
            flyway.clean();
            flyway.migrate();

            try (Connection connection = DriverManager.getConnection(jdbcUrl)) {
                assertThat(pragmaColumns(connection, "pacientes")).contains("paciente_id", "last_modified", "sync_token");
                assertThat(pragmaColumns(connection, "cuestionarios")).contains("respuestas", "last_modified", "sync_token");
            }
        } finally {
            Files.deleteIfExists(databaseFile);
        }
    }

    @Test
    void sqlServerViewsAreCreated() throws SQLException {
        Flyway flyway = Flyway.configure()
                .cleanDisabled(false)
                .dataSource(SQLSERVER.getJdbcUrl(), SQLSERVER.getUsername(), SQLSERVER.getPassword())
                .locations("filesystem:" + SQLSERVER_MIGRATIONS)
                .load();
        flyway.clean();
        flyway.migrate();

        try (Connection connection = DriverManager.getConnection(SQLSERVER.getJdbcUrl(), SQLSERVER.getUsername(), SQLSERVER.getPassword())) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.VIEWS WHERE TABLE_SCHEMA = 'dbo'")) {
                try (ResultSet rs = ps.executeQuery()) {
                    List<String> views = new ArrayList<>();
                    while (rs.next()) {
                        views.add(rs.getString(1));
                    }
                    assertThat(views)
                            .contains("vw_prl_pacientes", "vw_prl_cuestionarios", "vw_prl_citas");
                }
            }
        }
    }

    private static void assertColumns(DatabaseMetaData metaData, String table, String... expected) throws SQLException {
        Set<String> columns = new HashSet<>();
        try (ResultSet rs = metaData.getColumns(null, "public", table, null)) {
            while (rs.next()) {
                columns.add(rs.getString("COLUMN_NAME"));
            }
        }
        assertThat(columns).contains(expected);
    }

    private static List<String> pragmaColumns(Connection connection, String table) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("PRAGMA table_info('" + table + "')")) {
            List<String> columns = new ArrayList<>();
            while (resultSet.next()) {
                columns.add(resultSet.getString("name"));
            }
            return columns;
        }
    }
}
