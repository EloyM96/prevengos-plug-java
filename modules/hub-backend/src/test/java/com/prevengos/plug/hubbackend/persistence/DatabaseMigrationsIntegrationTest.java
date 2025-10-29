package com.prevengos.plug.hubbackend.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.testcontainers.containers.MSSQLServerContainer;
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
    private static final MSSQLServerContainer<?> MSSQL = new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:2022-latest")
            .acceptLicense();

    @Test
    void sqlServerMigrationsApplySuccessfully() throws Exception {
        Path migrationsDir = Path.of("..", "..", "migrations", "sqlserver").toAbsolutePath();
        createFederatedSourceDatabase();
        try (Connection connection = DriverManager.getConnection(
                MSSQL.getJdbcUrl(), MSSQL.getUsername(), MSSQL.getPassword())) {
            connection.setAutoCommit(true);
            runSqlServerScript(connection, migrationsDir.resolve("V1__create_views.sql"));
            runSqlServerScript(connection, migrationsDir.resolve("V2__create_prl_hub_tables.sql"));
            runSqlServerScript(connection, migrationsDir.resolve("V3__rrhh_audit_tables.sql"));

            try (Statement stmt = connection.createStatement()) {
                assertThat(count(stmt, "SELECT COUNT(*) FROM sys.views " +
                        "WHERE name IN ('vw_prl_pacientes','vw_prl_citas','vw_prl_cuestionarios','vw_prl_cuestionario_respuestas')"))
                        .isEqualTo(4);
                assertThat(count(stmt, "SELECT COUNT(*) FROM sys.tables " +
                        "WHERE name IN ('pacientes','cuestionarios','sync_events','rrhh_exports','file_drop_log')"))
                        .isEqualTo(5);
                assertThat(count(stmt, "SELECT COUNT(*) FROM sys.indexes " +
                        "WHERE name IN ('idx_pacientes_nif','idx_pacientes_last_modified','idx_cuestionarios_last_modified'," +
                        "'idx_cuestionarios_paciente','idx_sync_events_occurred_at','idx_rrhh_exports_trace','idx_rrhh_exports_created'," +
                        "'idx_file_drop_log_trace','idx_file_drop_log_created')"))
                        .isEqualTo(9);
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

    private static void createFederatedSourceDatabase() throws Exception {
        try (Connection masterConnection = DriverManager.getConnection(
                jdbcUrlForDatabase("master"), MSSQL.getUsername(), MSSQL.getPassword())) {
            masterConnection.setAutoCommit(true);
            try (Statement stmt = masterConnection.createStatement()) {
                stmt.executeUpdate("IF DB_ID('Prevengos') IS NULL CREATE DATABASE Prevengos;");
            }
        }

        try (Connection sourceConnection = DriverManager.getConnection(
                jdbcUrlForDatabase("Prevengos"), MSSQL.getUsername(), MSSQL.getPassword())) {
            sourceConnection.setAutoCommit(true);
            try (Statement stmt = sourceConnection.createStatement()) {
                stmt.execute("""
                        IF OBJECT_ID('dbo.Pacientes', 'U') IS NULL BEGIN
                            CREATE TABLE dbo.Pacientes (
                                PacienteGuid UNIQUEIDENTIFIER NOT NULL,
                                NIF NVARCHAR(16) NULL,
                                Nombre NVARCHAR(160) NULL,
                                Apellidos NVARCHAR(160) NULL,
                                FechaNacimiento DATE NULL,
                                Sexo NVARCHAR(1) NULL,
                                Telefono NVARCHAR(32) NULL,
                                Email NVARCHAR(160) NULL,
                                EmpresaGuid UNIQUEIDENTIFIER NULL,
                                CentroGuid UNIQUEIDENTIFIER NULL,
                                PrevengosId NVARCHAR(128) NULL,
                                UltimaActualizacion DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
                                Activo BIT NOT NULL DEFAULT 1
                            );
                        END;
                        """);
                stmt.execute("""
                        IF OBJECT_ID('dbo.Citas', 'U') IS NULL BEGIN
                            CREATE TABLE dbo.Citas (
                                CitaGuid UNIQUEIDENTIFIER NOT NULL,
                                PacienteGuid UNIQUEIDENTIFIER NOT NULL,
                                FechaHora DATETIME2 NULL,
                                Tipo NVARCHAR(64) NULL,
                                Estado NVARCHAR(64) NULL,
                                Aptitud NVARCHAR(64) NULL,
                                ReferenciaExterna NVARCHAR(128) NULL,
                                UltimaActualizacion DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
                                EsPRL BIT NOT NULL DEFAULT 1
                            );
                        END;
                        """);
                stmt.execute("""
                        IF OBJECT_ID('dbo.Cuestionarios', 'U') IS NULL BEGIN
                            CREATE TABLE dbo.Cuestionarios (
                                CuestionarioGuid UNIQUEIDENTIFIER NOT NULL,
                                PacienteGuid UNIQUEIDENTIFIER NOT NULL,
                                PlantillaCodigo NVARCHAR(64) NULL,
                                Estado NVARCHAR(32) NULL,
                                UltimaActualizacion DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
                                EsPRL BIT NOT NULL DEFAULT 1
                            );
                        END;
                        """);
                stmt.execute("""
                        IF OBJECT_ID('dbo.CuestionarioRespuestas', 'U') IS NULL BEGIN
                            CREATE TABLE dbo.CuestionarioRespuestas (
                                RespuestaGuid UNIQUEIDENTIFIER NOT NULL,
                                CuestionarioGuid UNIQUEIDENTIFIER NOT NULL,
                                PreguntaCodigo NVARCHAR(64) NULL,
                                Valor NVARCHAR(256) NULL,
                                Unidad NVARCHAR(64) NULL,
                                MetadataJson NVARCHAR(MAX) NULL,
                                UltimaActualizacion DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
                                EsPRL BIT NOT NULL DEFAULT 1
                            );
                        END;
                        """);
            }
        }
    }

    private static void runSqlServerScript(Connection connection, Path script) {
        ScriptUtils.executeSqlScript(connection,
                new EncodedResource(new FileSystemResource(script)),
                false,
                false,
                ScriptUtils.DEFAULT_COMMENT_PREFIX,
                "GO",
                ScriptUtils.DEFAULT_BLOCK_COMMENT_START_DELIMITER,
                ScriptUtils.DEFAULT_BLOCK_COMMENT_END_DELIMITER);
    }

    private static int count(Statement stmt, String sql) throws Exception {
        try (ResultSet rs = stmt.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private static String jdbcUrlForDatabase(String databaseName) {
        return MSSQL.getJdbcUrl().replace("databaseName=test", "databaseName=" + databaseName);
    }
}
