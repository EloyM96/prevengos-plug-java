package com.prevengos.plug.hubbackend.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prevengos.plug.gateway.csv.CsvFileWriter;
import com.prevengos.plug.gateway.filetransfer.FileTransferClient;
import com.prevengos.plug.gateway.sqlserver.CuestionarioGateway;
import com.prevengos.plug.gateway.sqlserver.JdbcCuestionarioGateway;
import com.prevengos.plug.gateway.sqlserver.JdbcPacienteGateway;
import com.prevengos.plug.gateway.sqlserver.JdbcSyncEventGateway;
import com.prevengos.plug.gateway.sqlserver.PacienteGateway;
import com.prevengos.plug.gateway.sqlserver.RrhhAuditGateway;
import com.prevengos.plug.gateway.sqlserver.SyncEventGateway;
import com.prevengos.plug.hubbackend.config.RrhhExportProperties;
import com.prevengos.plug.hubbackend.job.RrhhCsvExportJob;
import com.prevengos.plug.shared.persistence.jdbc.CuestionarioCsvRow;
import com.prevengos.plug.shared.persistence.jdbc.CuestionarioRecord;
import com.prevengos.plug.shared.persistence.jdbc.PacienteCsvRow;
import com.prevengos.plug.shared.persistence.jdbc.PacienteRecord;
import com.prevengos.plug.shared.persistence.jdbc.SyncEventRecord;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Files;
import java.nio.file.Path;
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

    @BeforeAll
    static void setUpDatabases() {
        SqlServerTestResource.initializeDatabases(SQL_SERVER);
        SqlServerTestResource.applyHubMigrations(SQL_SERVER);
        DriverManagerDataSource hubDataSource = SqlServerTestResource.createDataSource(SQL_SERVER, "prl_hub");
        DriverManagerDataSource prevengosDataSource = SqlServerTestResource.createDataSource(SQL_SERVER, "Prevengos");
        hubJdbcTemplate = new NamedParameterJdbcTemplate(hubDataSource);
        prevengosJdbcTemplate = new NamedParameterJdbcTemplate(prevengosDataSource);
    }

    @BeforeEach
    void cleanDatabase() {
        SqlServerTestResource.cleanHubSchema(hubJdbcTemplate);
        SqlServerTestResource.cleanPrevengosSchema(prevengosJdbcTemplate);
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

        RrhhExportProperties properties = new RrhhExportProperties();
        properties.setBaseDir(tempDir.resolve("outgoing"));
        properties.setArchiveDir(tempDir.resolve("archive"));
        properties.setProcessName("test-process");
        properties.setLookbackHours(48);
        properties.getDelivery().setEnabled(false);

        FileTransferClient transferClient = Mockito.mock(FileTransferClient.class);
        RrhhAuditGateway auditGateway = Mockito.mock(RrhhAuditGateway.class);

        RrhhCsvExportJob job = new RrhhCsvExportJob(
                pacienteGateway,
                cuestionarioGateway,
                new CsvFileWriter(),
                transferClient,
                auditGateway,
                properties);
        RrhhCsvExportJob.RrhhExportResult result = job.runExport("integration-test");

        Path pacientesCsv = result.stagingDir().resolve("pacientes.csv");
        Path cuestionariosCsv = result.stagingDir().resolve("cuestionarios.csv");

        assertThat(Files.readString(pacientesCsv)).contains("11111111H");
        assertThat(Files.readString(cuestionariosCsv)).contains("CS-02");
        assertThat(Files.exists(pacientesCsv.resolveSibling("pacientes.csv.sha256"))).isTrue();
        assertThat(Files.exists(cuestionariosCsv.resolveSibling("cuestionarios.csv.sha256"))).isTrue();
        assertThat(Files.exists(result.archiveDir().resolve("pacientes.csv"))).isTrue();
    }

}
