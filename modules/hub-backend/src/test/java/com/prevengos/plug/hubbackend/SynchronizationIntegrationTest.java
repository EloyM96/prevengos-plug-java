package com.prevengos.plug.hubbackend;

import com.prevengos.plug.hubbackend.job.RrhhCsvExportJob;
import com.prevengos.plug.shared.sync.dto.CuestionarioDto;
import com.prevengos.plug.shared.sync.dto.PacienteDto;
import com.prevengos.plug.shared.sync.dto.SyncPullResponse;
import com.prevengos.plug.shared.sync.dto.SyncPushRequest;
import com.prevengos.plug.shared.sync.dto.SyncPushResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
class SynchronizationIntegrationTest {

    @Container
    static MSSQLServerContainer<?> sqlServer = new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:2022-latest")
            .acceptLicense();

    private static final Path exportBaseDir;

    static {
        try {
            exportBaseDir = Files.createTempDirectory("rrhh-exports");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("hub.sqlserver.url", sqlServer::getJdbcUrl);
        registry.add("hub.sqlserver.username", sqlServer::getUsername);
        registry.add("hub.sqlserver.password", sqlServer::getPassword);
        registry.add("hub.sqlserver.driver-class-name", () -> "com.microsoft.sqlserver.jdbc.SQLServerDriver");
        registry.add("hub.jobs.rrhh-export.base-dir", () -> exportBaseDir.toString());
        registry.add("hub.jobs.rrhh-export.archive-dir", () -> exportBaseDir.resolve("archive").toString());
        registry.add("hub.jobs.rrhh-export.delivery.enabled", () -> "false");
    }

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    RrhhCsvExportJob rrhhCsvExportJob;

    @Test
    void pushPullAndExportLifecycle() throws Exception {
        PacienteDto paciente = new PacienteDto(
                UUID.randomUUID(),
                "12345678A",
                "Ana",
                "Prevengos",
                OffsetDateTime.now().minusYears(30).toLocalDate(),
                "F",
                "+34123456789",
                "ana.prevengos@example.com",
                UUID.randomUUID(),
                UUID.randomUUID(),
                "EXT-1",
                OffsetDateTime.now().minusDays(1),
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                null
        );
        CuestionarioDto cuestionario = new CuestionarioDto(
                UUID.randomUUID(),
                paciente.pacienteId(),
                "CS-01",
                "completado",
                "{}",
                null,
                null,
                OffsetDateTime.now().minusHours(2),
                OffsetDateTime.now().minusHours(1),
                OffsetDateTime.now(),
                null
        );

        SyncPushRequest request = new SyncPushRequest("integration-test", UUID.randomUUID(), List.of(paciente), List.of(cuestionario));
        ResponseEntity<SyncPushResponse> pushResponse = restTemplate.postForEntity(baseUrl("/sincronizacion/push"), request, SyncPushResponse.class);
        Assertions.assertThat(pushResponse.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(pushResponse.getBody()).isNotNull();
        long token = pushResponse.getBody().lastSyncToken();
        Assertions.assertThat(token).isGreaterThan(0);

        ResponseEntity<SyncPullResponse> pullResponse = restTemplate.getForEntity(baseUrl("/sincronizacion/pull?syncToken=0&limit=10"), SyncPullResponse.class);
        Assertions.assertThat(pullResponse.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(pullResponse.getBody()).isNotNull();
        Assertions.assertThat(pullResponse.getBody().pacientes()).isNotEmpty();
        Assertions.assertThat(pullResponse.getBody().cuestionarios()).isNotEmpty();
        Assertions.assertThat(pullResponse.getBody().nextSyncToken()).isGreaterThanOrEqualTo(token);

        RrhhCsvExportJob.RrhhExportResult exportResult = rrhhCsvExportJob.runExport("integration-test");
        Assertions.assertThat(Files.exists(exportResult.stagingDir().resolve("pacientes.csv"))).isTrue();
        Assertions.assertThat(Files.exists(exportResult.stagingDir().resolve("cuestionarios.csv"))).isTrue();
    }

    private String baseUrl(String path) {
        return "http://localhost:" + port + path;
    }
}
