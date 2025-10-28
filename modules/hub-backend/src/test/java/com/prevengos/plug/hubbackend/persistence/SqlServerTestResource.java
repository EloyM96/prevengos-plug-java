package com.prevengos.plug.hubbackend.persistence;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.MSSQLServerContainer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Utility helpers shared by SQL Server integration tests to bootstrap the
 * required databases and schemas used by the hub backend.
 */
public final class SqlServerTestResource {

    private static final Path MIGRATIONS_DIR = Path.of("..", "..", "migrations", "sqlserver").toAbsolutePath();
    private static boolean databasesInitialized = false;
    private static boolean hubMigrationsApplied = false;

    private SqlServerTestResource() {
    }

    public static synchronized void initializeDatabases(MSSQLServerContainer<?> container) {
        if (databasesInitialized) {
            return;
        }
        try {
            createDatabaseIfAbsent(container, "Prevengos");
            createDatabaseIfAbsent(container, "prl_hub");
            createPrevengosSchema(container);
            databasesInitialized = true;
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to initialise SQL Server databases", exception);
        }
    }

    public static synchronized void applyHubMigrations(MSSQLServerContainer<?> container) {
        if (hubMigrationsApplied) {
            return;
        }
        try (Connection connection = DriverManager.getConnection(
                container.getJdbcUrl() + ";databaseName=prl_hub",
                container.getUsername(),
                container.getPassword())) {
            runSqlServerScript(connection, MIGRATIONS_DIR.resolve("V2__create_prl_hub_tables.sql"));
            runSqlServerScript(connection, MIGRATIONS_DIR.resolve("V3__rrhh_audit_tables.sql"));
            runSqlServerScript(connection, MIGRATIONS_DIR.resolve("V1__create_views.sql"));
            hubMigrationsApplied = true;
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to apply SQL Server migrations", exception);
        }
    }

    public static DriverManagerDataSource createDataSource(MSSQLServerContainer<?> container, String databaseName) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        dataSource.setUrl(container.getJdbcUrl() + ";databaseName=" + databaseName);
        dataSource.setUsername(container.getUsername());
        dataSource.setPassword(container.getPassword());
        return dataSource;
    }

    public static void cleanHubSchema(NamedParameterJdbcTemplate jdbcTemplate) {
        jdbcTemplate.getJdbcTemplate().execute("DELETE FROM dbo.file_drop_log");
        jdbcTemplate.getJdbcTemplate().execute("DELETE FROM dbo.rrhh_exports");
        jdbcTemplate.getJdbcTemplate().execute("DELETE FROM dbo.sync_events");
        jdbcTemplate.getJdbcTemplate().execute("DELETE FROM dbo.cuestionarios");
        jdbcTemplate.getJdbcTemplate().execute("DELETE FROM dbo.pacientes");
    }

    public static void cleanPrevengosSchema(NamedParameterJdbcTemplate jdbcTemplate) {
        jdbcTemplate.getJdbcTemplate().execute("DELETE FROM dbo.CuestionarioRespuestas");
        jdbcTemplate.getJdbcTemplate().execute("DELETE FROM dbo.Cuestionarios");
        jdbcTemplate.getJdbcTemplate().execute("DELETE FROM dbo.Citas");
        jdbcTemplate.getJdbcTemplate().execute("DELETE FROM dbo.Pacientes");
    }

    private static void createDatabaseIfAbsent(MSSQLServerContainer<?> container, String databaseName) throws Exception {
        try (Connection connection = DriverManager.getConnection(
                container.getJdbcUrl() + ";databaseName=master",
                container.getUsername(),
                container.getPassword());
             Statement statement = connection.createStatement()) {
            statement.execute("IF DB_ID('" + databaseName + "') IS NULL CREATE DATABASE " + databaseName + ";");
        }
    }

    private static void createPrevengosSchema(MSSQLServerContainer<?> container) throws Exception {
        String url = container.getJdbcUrl() + ";databaseName=Prevengos";
        try (Connection connection = DriverManager.getConnection(url, container.getUsername(), container.getPassword());
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    IF OBJECT_ID('dbo.Pacientes', 'U') IS NULL
                    BEGIN
                        CREATE TABLE dbo.Pacientes (
                            PacienteGuid UNIQUEIDENTIFIER PRIMARY KEY,
                            NIF NVARCHAR(16) NOT NULL,
                            Nombre NVARCHAR(160) NOT NULL,
                            Apellidos NVARCHAR(160) NOT NULL,
                            FechaNacimiento DATE NULL,
                            Sexo NVARCHAR(1) NOT NULL,
                            Telefono NVARCHAR(32) NULL,
                            Email NVARCHAR(160) NULL,
                            EmpresaGuid UNIQUEIDENTIFIER NULL,
                            CentroGuid UNIQUEIDENTIFIER NULL,
                            PrevengosId NVARCHAR(128) NULL,
                            UltimaActualizacion DATETIMEOFFSET(7) NOT NULL DEFAULT SYSUTCDATETIME(),
                            Activo BIT NOT NULL DEFAULT 1
                        );
                    END
                    """);
            statement.execute("""
                    IF OBJECT_ID('dbo.Citas', 'U') IS NULL
                    BEGIN
                        CREATE TABLE dbo.Citas (
                            CitaGuid UNIQUEIDENTIFIER PRIMARY KEY,
                            PacienteGuid UNIQUEIDENTIFIER NOT NULL,
                            FechaHora DATETIMEOFFSET(7) NOT NULL,
                            Tipo NVARCHAR(32) NOT NULL,
                            Estado NVARCHAR(32) NOT NULL,
                            Aptitud NVARCHAR(32) NULL,
                            ReferenciaExterna NVARCHAR(128) NULL,
                            UltimaActualizacion DATETIMEOFFSET(7) NOT NULL DEFAULT SYSUTCDATETIME(),
                            EsPRL BIT NOT NULL DEFAULT 1
                        );
                    END
                    """);
            statement.execute("""
                    IF OBJECT_ID('dbo.Cuestionarios', 'U') IS NULL
                    BEGIN
                        CREATE TABLE dbo.Cuestionarios (
                            CuestionarioGuid UNIQUEIDENTIFIER PRIMARY KEY,
                            PacienteGuid UNIQUEIDENTIFIER NOT NULL,
                            PlantillaCodigo NVARCHAR(64) NOT NULL,
                            Estado NVARCHAR(32) NOT NULL,
                            UltimaActualizacion DATETIMEOFFSET(7) NOT NULL DEFAULT SYSUTCDATETIME(),
                            EsPRL BIT NOT NULL DEFAULT 1
                        );
                    END
                    """);
            statement.execute("""
                    IF OBJECT_ID('dbo.CuestionarioRespuestas', 'U') IS NULL
                    BEGIN
                        CREATE TABLE dbo.CuestionarioRespuestas (
                            RespuestaGuid UNIQUEIDENTIFIER PRIMARY KEY,
                            CuestionarioGuid UNIQUEIDENTIFIER NOT NULL,
                            PreguntaCodigo NVARCHAR(64) NOT NULL,
                            Valor NVARCHAR(MAX) NULL,
                            Unidad NVARCHAR(32) NULL,
                            MetadataJson NVARCHAR(MAX) NULL,
                            UltimaActualizacion DATETIMEOFFSET(7) NOT NULL DEFAULT SYSUTCDATETIME(),
                            EsPRL BIT NOT NULL DEFAULT 1
                        );
                    END
                    """);
        }
    }

    private static void runSqlServerScript(Connection connection, Path script) throws Exception {
        String content = Files.readString(script);
        String[] statements = content.split("(?im)^\\s*GO\\s*$");
        for (String raw : statements) {
            String statement = raw.trim();
            if (statement.isEmpty()) {
                continue;
            }
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(statement);
            }
        }
    }
}
