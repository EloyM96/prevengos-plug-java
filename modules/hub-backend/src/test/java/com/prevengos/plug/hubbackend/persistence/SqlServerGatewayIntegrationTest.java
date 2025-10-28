package com.prevengos.plug.hubbackend.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prevengos.plug.gateway.csv.CsvFileWriter;
import com.prevengos.plug.gateway.sqlserver.CuestionarioGateway;
import com.prevengos.plug.gateway.sqlserver.JdbcCuestionarioGateway;
import com.prevengos.plug.gateway.sqlserver.JdbcPacienteGateway;
import com.prevengos.plug.gateway.sqlserver.JdbcSyncEventGateway;
import com.prevengos.plug.gateway.sqlserver.PacienteGateway;
import com.prevengos.plug.gateway.sqlserver.SyncEventGateway;
import com.prevengos.plug.hubbackend.config.RrhhExportProperties;
import com.prevengos.plug.shared.persistence.jdbc.CuestionarioCsvRow;
import com.prevengos.plug.shared.persistence.jdbc.CuestionarioRecord;
import com.prevengos.plug.shared.persistence.jdbc.PacienteCsvRow;
import com.prevengos.plug.shared.persistence.jdbc.PacienteRecord;
import com.prevengos.plug.shared.persistence.jdbc.SyncEventRecord;
import com.prevengos.plug.hubbackend.job.RrhhCsvExportJob;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class SqlServerGatewayIntegrationTest {

    @Container
    private static final MSSQLServerContainer<?> SQL_SERVER =
            new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:2022-latest")
                    .acceptLicense();

    private static NamedParameterJdbcTemplate hubJdbcTemplate;
    private static NamedParameterJdbcTemplate prevengosJdbcTemplate;
    private static boolean initialized = false;

    @BeforeAll
    static void setUpDatabases() throws Exception {
        if (initialized) {
            return;
        }

        String adminUrl = SQL_SERVER.getJdbcUrl() + ";databaseName=master";
        try (Connection connection = DriverManager.getConnection(adminUrl, SQL_SERVER.getUsername(), SQL_SERVER.getPassword());
             Statement stmt = connection.createStatement()) {
            stmt.execute("IF DB_ID('Prevengos') IS NULL CREATE DATABASE Prevengos;");
            stmt.execute("IF DB_ID('prl_hub') IS NULL CREATE DATABASE prl_hub;");
        }

        createPrevengosSchema();
        applySqlServerMigrations();

        hubJdbcTemplate = new NamedParameterJdbcTemplate(createDataSource("prl_hub"));
        prevengosJdbcTemplate = new NamedParameterJdbcTemplate(createDataSource("Prevengos"));

        initialized = true;
    }

    @BeforeEach
    void cleanDatabase() {
        hubJdbcTemplate.getJdbcTemplate().execute("DELETE FROM dbo.sync_events");
        hubJdbcTemplate.getJdbcTemplate().execute("DELETE FROM dbo.cuestionarios");
        hubJdbcTemplate.getJdbcTemplate().execute("DELETE FROM dbo.pacientes");
        prevengosJdbcTemplate.getJdbcTemplate().execute("DELETE FROM dbo.CuestionarioRespuestas");
        prevengosJdbcTemplate.getJdbcTemplate().execute("DELETE FROM dbo.Cuestionarios");
        prevengosJdbcTemplate.getJdbcTemplate().execute("DELETE FROM dbo.Citas");
        prevengosJdbcTemplate.getJdbcTemplate().execute("DELETE FROM dbo.Pacientes");
    }

    @Test
    void pacienteGatewayPersistsAndReadsData() {
        PacienteGateway gateway = new JdbcPacienteGateway(hubJdbcTemplate);
        UUID pacienteId = UUID.randomUUID();
        OffsetDateTime base = OffsetDateTime.now(ZoneOffset.UTC).withNano(0);

        PacienteRecord record = new PacienteRecord(
                pacienteId,
                "12345678A",
                "Ana",
                "Prevengos",
                LocalDate.of(1990, 1, 1),
                "F",
                "+34911122334",
                "ana.prevengos@example.com",
                UUID.randomUUID(),
                UUID.randomUUID(),
                "EXT-1",
                base.minusDays(1),
                base,
                base,
                1L
        );
        gateway.upsertPaciente(record);

        List<PacienteRecord> fetched = gateway.findByIds(List.of(pacienteId));
        assertThat(fetched).hasSize(1);
        assertThat(fetched.get(0).nombre()).isEqualTo("Ana");

        List<PacienteRecord> updated = gateway.findUpdatedSince(base.minusHours(1), 10);
        assertThat(updated).extracting(PacienteRecord::pacienteId).containsExactly(pacienteId);

        insertPrevengosPaciente(pacienteId, "12345678A", "Ana", "Prevengos",
                base.plusHours(2), "+34911122334", "ana.prevengos@example.com", "EXT-1");
        List<PacienteCsvRow> csvRows = gateway.fetchForRrhhExport(base.minusDays(1));
        assertThat(csvRows).hasSize(1);
        assertThat(csvRows.get(0).nif()).isEqualTo("12345678A");
    }

    @Test
    void cuestionarioGatewayPersistsAndReadsData() {
        CuestionarioGateway gateway = new JdbcCuestionarioGateway(hubJdbcTemplate);
        UUID pacienteId = UUID.randomUUID();
        UUID cuestionarioId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC).withNano(0);

        hubJdbcTemplate.update("INSERT INTO dbo.pacientes (paciente_id, nif, nombre, apellidos, fecha_nacimiento, sexo, " +
                        "telefono, email, empresa_id, centro_id, externo_ref, created_at, updated_at, last_modified, sync_token) " +
                        "VALUES (:paciente_id, '00000000T', 'Test', 'Paciente', :fecha, 'M', NULL, NULL, NULL, NULL, NULL, :created, :updated, :updated, 0)",
                new MapSqlParameterSource()
                        .addValue("paciente_id", pacienteId)
                        .addValue("fecha", LocalDate.of(1985, 5, 20))
                        .addValue("created", now.minusDays(1))
                        .addValue("updated", now.minusHours(1)));

        CuestionarioRecord record = new CuestionarioRecord(
                cuestionarioId,
                pacienteId,
                "CS-01",
                "completado",
                "{\"score\":90}",
                "{}",
                "[]",
                now.minusDays(1),
                now,
                now,
                5L
        );
        gateway.upsertCuestionario(record);

        List<CuestionarioRecord> fetched = gateway.findByPacienteId(pacienteId);
        assertThat(fetched).hasSize(1);
        assertThat(fetched.get(0).estado()).isEqualTo("completado");

        List<CuestionarioRecord> updated = gateway.findUpdatedSince(now.minusHours(2), 10);
        assertThat(updated).extracting(CuestionarioRecord::cuestionarioId).containsExactly(cuestionarioId);

        insertPrevengosCuestionario(cuestionarioId, pacienteId, "CS-02", "validado", now.plusHours(3));
        List<CuestionarioCsvRow> csvRows = gateway.fetchForRrhhExport(now.minusDays(1));
        assertThat(csvRows).hasSize(1);
        assertThat(csvRows.get(0).estado()).isEqualTo("validado");
    }

    @Test
    void syncEventGatewayRegistersAndReadsEvents() {
        ObjectMapper mapper = new ObjectMapper();
        SyncEventGateway gateway = new JdbcSyncEventGateway(hubJdbcTemplate, mapper);
        OffsetDateTime occurredAt = OffsetDateTime.now(ZoneOffset.UTC).withNano(0);
        SyncEventRecord stored = gateway.registerEvent(new SyncEventRecord(
                null,
                UUID.randomUUID(),
                "paciente-upserted",
                1,
                occurredAt,
                "test-suite",
                null,
                null,
                mapper.createObjectNode().put("nif", "11223344B"),
                mapper.createObjectNode()
        ));

        assertThat(stored.syncToken()).isNotNull();

        List<SyncEventRecord> events = gateway.fetchNextEvents(null, occurredAt.minusMinutes(1), 10);
        assertThat(events).hasSize(1);
        assertThat(events.get(0).eventType()).isEqualTo("paciente-upserted");
    }

    @Test
    void rrhhCsvExportJobGeneratesFilesFromDatabase(@TempDir Path tempDir) throws Exception {
        PacienteGateway pacienteGateway = new JdbcPacienteGateway(hubJdbcTemplate);
        CuestionarioGateway cuestionarioGateway = new JdbcCuestionarioGateway(hubJdbcTemplate);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC).withNano(0);

        UUID pacienteId = UUID.randomUUID();
        hubJdbcTemplate.update("INSERT INTO dbo.pacientes (paciente_id, nif, nombre, apellidos, fecha_nacimiento, sexo, " +
                        "telefono, email, empresa_id, centro_id, externo_ref, created_at, updated_at, last_modified, sync_token) " +
                        "VALUES (:paciente_id, '11111111H', 'Helena', 'García', :fecha, 'F', '+34999888776', 'helena@example.com', NULL, NULL, 'EXT-2', :created, :updated, :updated, 0)",
                new MapSqlParameterSource()
                        .addValue("paciente_id", pacienteId)
                        .addValue("fecha", LocalDate.of(1992, 3, 15))
                        .addValue("created", now.minusDays(2))
                        .addValue("updated", now.minusHours(5)));

        UUID cuestionarioId = UUID.randomUUID();
        hubJdbcTemplate.update("INSERT INTO dbo.cuestionarios (cuestionario_id, paciente_id, plantilla_codigo, estado, respuestas, firmas, adjuntos, created_at, updated_at, last_modified, sync_token) " +
                        "VALUES (:cuestionario_id, :paciente_id, 'CS-02', 'validado', '{\"ok\":true}', '{}', '[]', :created, :updated, :updated, 0)",
                new MapSqlParameterSource()
                        .addValue("cuestionario_id", cuestionarioId)
                        .addValue("paciente_id", pacienteId)
                        .addValue("created", now.minusDays(1))
                        .addValue("updated", now.minusHours(1)));

        insertPrevengosPaciente(pacienteId, "11111111H", "Helena", "García",
                now.minusHours(2), "+34999888776", "helena@example.com", "EXT-2");
        insertPrevengosCuestionario(cuestionarioId, pacienteId, "CS-02", "validado", now.minusHours(2));

        RrhhExportProperties properties = new RrhhExportProperties();
        properties.setBaseDir(tempDir);
        properties.setProcessName("test-process");
        properties.setLookbackHours(48);

        RrhhCsvExportJob job = new RrhhCsvExportJob(pacienteGateway, cuestionarioGateway, new CsvFileWriter(), properties);
        job.runExport("integration-test");

        Path dayDir;
        try (var stream = Files.list(tempDir)) {
            dayDir = stream.findFirst().orElseThrow();
        }
        Path processDir;
        try (var stream = Files.list(dayDir)) {
            processDir = stream.findFirst().orElseThrow();
        }
        Path pacientesCsv = processDir.resolve("pacientes.csv");
        Path cuestionariosCsv = processDir.resolve("cuestionarios.csv");

        assertThat(Files.readString(pacientesCsv)).contains("11111111H");
        assertThat(Files.readString(cuestionariosCsv)).contains("CS-02");
        assertThat(Files.exists(pacientesCsv.resolveSibling("pacientes.csv.sha256"))).isTrue();
        assertThat(Files.exists(cuestionariosCsv.resolveSibling("cuestionarios.csv.sha256"))).isTrue();
    }

    private static DriverManagerDataSource createDataSource(String databaseName) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        dataSource.setUrl(SQL_SERVER.getJdbcUrl() + ";databaseName=" + databaseName);
        dataSource.setUsername(SQL_SERVER.getUsername());
        dataSource.setPassword(SQL_SERVER.getPassword());
        return dataSource;
    }

    private static void createPrevengosSchema() throws Exception {
        String url = SQL_SERVER.getJdbcUrl() + ";databaseName=Prevengos";
        try (Connection connection = DriverManager.getConnection(url, SQL_SERVER.getUsername(), SQL_SERVER.getPassword());
             Statement stmt = connection.createStatement()) {
            stmt.execute("""
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

            stmt.execute("""
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

            stmt.execute("""
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

            stmt.execute("""
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

    private static void applySqlServerMigrations() throws Exception {
        Path migrationsDir = Path.of("..", "..", "migrations", "sqlserver").toAbsolutePath();
        String url = SQL_SERVER.getJdbcUrl() + ";databaseName=prl_hub";
        try (Connection connection = DriverManager.getConnection(url, SQL_SERVER.getUsername(), SQL_SERVER.getPassword())) {
            runSqlServerScript(connection, migrationsDir.resolve("V2__create_prl_hub_tables.sql"));
            runSqlServerScript(connection, migrationsDir.resolve("V1__create_views.sql"));
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

    private static void insertPrevengosPaciente(UUID pacienteId, String nif, String nombre, String apellidos,
                                                OffsetDateTime updatedAt, String telefono, String email, String externoRef) {
        prevengosJdbcTemplate.update("INSERT INTO dbo.Pacientes (PacienteGuid, NIF, Nombre, Apellidos, FechaNacimiento, Sexo, " +
                        "Telefono, Email, EmpresaGuid, CentroGuid, PrevengosId, UltimaActualizacion, Activo) " +
                        "VALUES (:id, :nif, :nombre, :apellidos, :fecha, 'F', :telefono, :email, :empresa, :centro, :externo, :updated, 1)",
                new MapSqlParameterSource()
                        .addValue("id", pacienteId)
                        .addValue("nif", nif)
                        .addValue("nombre", nombre)
                        .addValue("apellidos", apellidos)
                        .addValue("fecha", LocalDate.of(1990, 1, 1))
                        .addValue("telefono", telefono)
                        .addValue("email", email)
                        .addValue("empresa", UUID.randomUUID())
                        .addValue("centro", UUID.randomUUID())
                        .addValue("externo", externoRef)
                        .addValue("updated", updatedAt));
    }

    private static void insertPrevengosCuestionario(UUID cuestionarioId, UUID pacienteId, String plantillaCodigo,
                                                    String estado, OffsetDateTime updatedAt) {
        prevengosJdbcTemplate.update("INSERT INTO dbo.Cuestionarios (CuestionarioGuid, PacienteGuid, PlantillaCodigo, Estado, UltimaActualizacion, EsPRL) " +
                        "VALUES (:id, :paciente, :plantilla, :estado, :updated, 1)",
                new MapSqlParameterSource()
                        .addValue("id", cuestionarioId)
                        .addValue("paciente", pacienteId)
                        .addValue("plantilla", plantillaCodigo)
                        .addValue("estado", estado)
                        .addValue("updated", updatedAt));
    }
}
