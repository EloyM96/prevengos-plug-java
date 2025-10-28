package com.prevengos.plug.hubbackend;

import com.prevengos.plug.hubbackend.web.RrhhExchangeController;
import com.prevengos.plug.hubbackend.dto.BatchSyncResponse;
import com.prevengos.plug.shared.dto.CuestionarioDto;
import com.prevengos.plug.shared.dto.PacienteDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RrhhExchangeControllerIntegrationTest extends SqlServerIntegrationTestSupport {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Test
    void exportEndpointGeneratesCsvFiles() throws Exception {
        UUID pacienteId = UUID.randomUUID();
        UUID cuestionarioId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC).withNano(0);

        PacienteDto paciente = new PacienteDto(
                pacienteId,
                "98765Z",
                "Juan",
                "Prevengos",
                LocalDate.of(1985, 3, 8),
                "M",
                "+34987654321",
                "juan.prevengos@example.com",
                UUID.randomUUID(),
                UUID.randomUUID(),
                "EXT-2",
                now,
                now
        );

        ResponseEntity<BatchSyncResponse> pacienteSync = restTemplate.postForEntity(
                "/sincronizacion/pacientes",
                List.of(paciente),
                BatchSyncResponse.class
        );
        assertThat(pacienteSync.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(pacienteSync.getBody()).isNotNull();
        assertThat(pacienteSync.getBody().processed()).isEqualTo(1);

        CuestionarioDto cuestionario = new CuestionarioDto(
                cuestionarioId,
                pacienteId,
                "CS-01",
                "completado",
                Map.of("apto", true),
                List.of(Map.of("firmado", true)),
                List.of(),
                now,
                now
        );

        ResponseEntity<BatchSyncResponse> cuestionarioSync = restTemplate.postForEntity(
                "/sincronizacion/cuestionarios",
                List.of(cuestionario),
                BatchSyncResponse.class
        );
        assertThat(cuestionarioSync.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(cuestionarioSync.getBody()).isNotNull();
        assertThat(cuestionarioSync.getBody().processed()).isEqualTo(1);

        Integer exportsBefore = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM rrhh_exports",
                new MapSqlParameterSource(),
                Integer.class
        );

        ResponseEntity<RrhhExchangeController.RrhhExportResponse> exportResponse = restTemplate.postForEntity(
                "/rrhh/export",
                null,
                RrhhExchangeController.RrhhExportResponse.class
        );

        assertThat(exportResponse.getStatusCode().is2xxSuccessful()).isTrue();
        RrhhExchangeController.RrhhExportResponse body = exportResponse.getBody();
        assertThat(body).isNotNull();
        assertThat(body.pacientes()).isGreaterThanOrEqualTo(1);
        assertThat(body.cuestionarios()).isGreaterThanOrEqualTo(1);

        Path stagingDir = Path.of(body.stagingDir());
        Path archiveDir = Path.of(body.archiveDir());
        Path pacientesCsv = stagingDir.resolve("pacientes.csv");
        Path cuestionariosCsv = stagingDir.resolve("cuestionarios.csv");

        assertThat(Files.exists(pacientesCsv)).isTrue();
        assertThat(Files.exists(cuestionariosCsv)).isTrue();
        assertThat(Files.readString(pacientesCsv)).contains("paciente_id", pacienteId.toString());
        assertThat(Files.readString(cuestionariosCsv)).contains("cuestionario_id", cuestionarioId.toString());
        assertThat(Files.exists(pacientesCsv.resolveSibling("pacientes.csv.sha256"))).isTrue();
        assertThat(Files.exists(cuestionariosCsv.resolveSibling("cuestionarios.csv.sha256"))).isTrue();

        assertThat(Files.exists(archiveDir.resolve("pacientes.csv"))).isTrue();
        assertThat(Files.exists(archiveDir.resolve("cuestionarios.csv"))).isTrue();

        Integer exportsAfter = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM rrhh_exports",
                new MapSqlParameterSource(),
                Integer.class
        );
        assertThat(exportsAfter).isEqualTo(exportsBefore + 1);
    }
}
