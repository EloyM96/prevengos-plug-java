package com.prevengos.plug.hubbackend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prevengos.plug.hubbackend.persistence.SqlServerTestResource;
import com.prevengos.plug.shared.sync.dto.CuestionarioDto;
import com.prevengos.plug.shared.sync.dto.PacienteDto;
import com.prevengos.plug.shared.sync.dto.SyncEventDto;
import com.prevengos.plug.shared.sync.dto.SyncPullResponse;
import com.prevengos.plug.shared.sync.dto.SyncPushRequest;
import com.prevengos.plug.shared.sync.dto.SyncPushResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.FileSystemUtils;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class HubBackendIntegrationTest {

    @Container
    private static final MSSQLServerContainer<?> SQL_SERVER =
            new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:2022-latest")
                    .acceptLicense();

    private static final Path EXPORT_BASE_DIR = Path.of("build", "integration-tests", "rrhh-export");
    private static final Path EXPORT_ARCHIVE_DIR = EXPORT_BASE_DIR.resolve("archive");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @BeforeAll
    static void prepareDirectories() throws IOException {
        Files.createDirectories(EXPORT_BASE_DIR);
        Files.createDirectories(EXPORT_ARCHIVE_DIR);
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        SqlServerTestResource.initializeDatabases(SQL_SERVER);
        registry.add("hub.sqlserver.url", () -> SQL_SERVER.getJdbcUrl() + ";databaseName=prl_hub");
        registry.add("hub.sqlserver.username", SQL_SERVER::getUsername);
        registry.add("hub.sqlserver.password", SQL_SERVER::getPassword);
        registry.add("spring.flyway.url", () -> SQL_SERVER.getJdbcUrl() + ";databaseName=prl_hub");
        registry.add("spring.flyway.user", SQL_SERVER::getUsername);
        registry.add("spring.flyway.password", SQL_SERVER::getPassword);
        registry.add("hub.jobs.rrhh-export.base-dir", () -> EXPORT_BASE_DIR.toString());
        registry.add("hub.jobs.rrhh-export.archive-dir", () -> EXPORT_ARCHIVE_DIR.toString());
        registry.add("hub.jobs.rrhh-export.delivery.enabled", () -> "false");
        registry.add("hub.jobs.rrhh-export.process-name", () -> "integration-tests");
        registry.add("hub.jobs.rrhh-export.origin", () -> "suite");
        registry.add("hub.jobs.rrhh-export.operator", () -> "integration-bot");
    }

    @BeforeEach
    void cleanDatabase() {
        SqlServerTestResource.cleanHubSchema(jdbcTemplate);
    }

    @AfterEach
    void cleanDirectories() throws IOException {
        FileSystemUtils.deleteRecursively(EXPORT_BASE_DIR);
        Files.createDirectories(EXPORT_BASE_DIR);
        Files.createDirectories(EXPORT_ARCHIVE_DIR);
    }

    @Test
    void pushEndpointPersistsDataAndPullReturnsEntities() throws Exception {
        UUID pacienteId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.now(ZoneOffset.UTC).minusDays(1).withNano(0);
        OffsetDateTime updatedAt = createdAt.plusHours(12);

        PacienteDto paciente = new PacienteDto(
                pacienteId,
                "12345A",
                "Ana",
                "Prevengos",
                LocalDate.of(1990, 1, 1),
                "F",
                "+34911122334",
                "ana.prevengos@example.com",
                UUID.randomUUID(),
                UUID.randomUUID(),
                "EXT-100",
                createdAt,
                updatedAt,
                updatedAt,
                null
        );

        UUID cuestionarioId = UUID.randomUUID();
        CuestionarioDto cuestionario = new CuestionarioDto(
                cuestionarioId,
                pacienteId,
                "CS-01",
                "completado",
                "{\"resultado\":95}",
                "[{\"firma\":\"dr.prevengos\"}]",
                "[]",
                createdAt,
                updatedAt.plusHours(2),
                updatedAt.plusHours(2),
                null
        );

        SyncPushRequest pushRequest = new SyncPushRequest(
                "integration-suite",
                UUID.randomUUID(),
                List.of(paciente),
                List.of(cuestionario)
        );

        MvcResult pushResult = mockMvc.perform(post("/sincronizacion/push")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pushRequest)))
                .andExpect(status().isOk())
                .andReturn();
        SyncPushResponse pushResponse = objectMapper.readValue(
                pushResult.getResponse().getContentAsByteArray(),
                SyncPushResponse.class);
        assertThat(pushResponse.processedPacientes()).isEqualTo(1);
        assertThat(pushResponse.processedCuestionarios()).isEqualTo(1);
        assertThat(pushResponse.lastSyncToken()).isGreaterThan(0);

        MapSqlParameterSource pacienteParams = new MapSqlParameterSource("paciente_id", pacienteId);
        Long pacienteSyncToken = jdbcTemplate.queryForObject(
                "SELECT sync_token FROM dbo.pacientes WHERE paciente_id = :paciente_id",
                pacienteParams,
                Long.class);
        assertThat(pacienteSyncToken).isNotNull();

        MapSqlParameterSource cuestionarioParams = new MapSqlParameterSource("cuestionario_id", cuestionarioId);
        Long cuestionarioSyncToken = jdbcTemplate.queryForObject(
                "SELECT sync_token FROM dbo.cuestionarios WHERE cuestionario_id = :cuestionario_id",
                cuestionarioParams,
                Long.class);
        assertThat(cuestionarioSyncToken).isNotNull();

        List<Map<String, Object>> events = jdbcTemplate.getJdbcTemplate().queryForList(
                "SELECT sync_token, event_type FROM dbo.sync_events ORDER BY sync_token ASC");
        assertThat(events).hasSize(2);
        assertThat(events).extracting(entry -> entry.get("event_type")).containsExactly(
                "paciente-upserted",
                "cuestionario-upserted");

        MvcResult pullResult = mockMvc.perform(get("/sincronizacion/pull")
                        .param("limit", "10")
                        .param("syncToken", "0"))
                .andExpect(status().isOk())
                .andReturn();
        SyncPullResponse pullResponse = objectMapper.readValue(
                pullResult.getResponse().getContentAsByteArray(),
                SyncPullResponse.class);
        assertThat(pullResponse.pacientes()).extracting(PacienteDto::pacienteId).contains(pacienteId);
        assertThat(pullResponse.cuestionarios()).extracting(CuestionarioDto::cuestionarioId).contains(cuestionarioId);
        assertThat(pullResponse.events()).extracting(SyncEventDto::eventType)
                .containsExactly("paciente-upserted", "cuestionario-upserted");
        long lastToken = pullResponse.nextSyncToken();
        assertThat(lastToken).isGreaterThanOrEqualTo(pushResponse.lastSyncToken());

        MvcResult secondPull = mockMvc.perform(get("/sincronizacion/pull")
                        .param("limit", "5")
                        .param("syncToken", String.valueOf(lastToken)))
                .andExpect(status().isOk())
                .andReturn();
        SyncPullResponse emptyResponse = objectMapper.readValue(
                secondPull.getResponse().getContentAsByteArray(),
                SyncPullResponse.class);
        assertThat(emptyResponse.pacientes()).isEmpty();
        assertThat(emptyResponse.cuestionarios()).isEmpty();
        assertThat(emptyResponse.events()).isEmpty();
        assertThat(emptyResponse.nextSyncToken()).isEqualTo(lastToken);

        MvcResult exportResult = mockMvc.perform(post("/rrhh/export"))
                .andExpect(status().isAccepted())
                .andReturn();

        JsonNode exportJson = objectMapper.readTree(
                exportResult.getResponse().getContentAsByteArray());
        assertThat(exportJson.get("trace_id").asText()).isNotBlank();
        assertThat(exportJson.get("remote_path").isNull()).isTrue();

        Path pacientesCsv = Path.of(exportJson.get("staging_dir").asText(), "pacientes.csv");
        Path cuestionariosCsv = Path.of(exportJson.get("staging_dir").asText(), "cuestionarios.csv");
        Path archiveDir = Path.of(exportJson.get("archive_dir").asText());

        assertThat(Files.exists(pacientesCsv)).isTrue();
        assertThat(Files.exists(cuestionariosCsv)).isTrue();
        assertThat(Files.readString(pacientesCsv)).contains("12345A");
        assertThat(Files.readString(cuestionariosCsv)).contains("CS-01");
        assertThat(Files.exists(archiveDir.resolve("pacientes.csv"))).isTrue();
        assertThat(Files.exists(archiveDir.resolve("cuestionarios.csv"))).isTrue();

        Integer exportsLogged = jdbcTemplate.getJdbcTemplate().queryForObject(
                "SELECT COUNT(*) FROM dbo.rrhh_exports",
                Integer.class);
        assertThat(exportsLogged).isEqualTo(1);
    }
}
