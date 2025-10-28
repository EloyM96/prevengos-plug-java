package com.prevengos.plug.hubbackend;

import com.fasterxml.jackson.databind.JsonNode;
import com.prevengos.plug.hubbackend.dto.CuestionarioDto;
import com.prevengos.plug.hubbackend.dto.PacienteDto;
import com.prevengos.plug.hubbackend.service.CsvExportService;
import com.prevengos.plug.hubbackend.service.CuestionarioService;
import com.prevengos.plug.hubbackend.service.PacienteService;
import com.prevengos.plug.shared.csv.CsvRecord;
import com.prevengos.plug.shared.json.ContractJsonMapper;
import com.prevengos.plug.shared.contracts.v1.Cuestionario;
import com.prevengos.plug.shared.contracts.v1.Paciente;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class CsvExportServiceIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("prevengos_csv")
            .withUsername("prevengos")
            .withPassword("prevengos");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
    }

    @Autowired
    PacienteService pacienteService;

    @Autowired
    CuestionarioService cuestionarioService;

    @Autowired
    CsvExportService csvExportService;

    @Test
    void exportsPersistedEntitiesToCsvContracts() {
        UUID pacienteId = UUID.randomUUID();
        UUID cuestionarioId = UUID.randomUUID();
        UUID empresaId = UUID.randomUUID();
        UUID centroId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.now(ZoneOffset.UTC).minusDays(2);
        OffsetDateTime updatedAt = OffsetDateTime.now(ZoneOffset.UTC).minusDays(1);

        PacienteDto pacienteDto = new PacienteDto(
                pacienteId,
                "12345A",
                "Ana",
                "Prevengos",
                LocalDate.of(1990, 5, 20),
                "F",
                "+34999111222",
                "ana.prevengos@example.com",
                empresaId,
                centroId,
                "ext-001",
                createdAt,
                updatedAt
        );
        pacienteService.upsertPacientes(List.of(pacienteDto), "csv-export");

        CuestionarioDto cuestionarioDto = new CuestionarioDto(
                cuestionarioId,
                pacienteId,
                "ERGONOMIA-2024",
                "completado",
                List.of(new CuestionarioDto.RespuestaDto(
                        "pregunta-1",
                        Map.of("valor", "SI"),
                        "bool",
                        Map.of("observaciones", "Sin incidencias")
                )),
                List.of("firma-base64"),
                List.of("adjunto-123"),
                createdAt.plusDays(1),
                updatedAt.plusDays(1)
        );
        cuestionarioService.upsertCuestionarios(List.of(cuestionarioDto), "csv-export");

        List<CsvRecord> pacienteRecords = csvExportService.exportPacientesSince(createdAt.minusDays(1));
        assertThat(pacienteRecords).hasSize(1);
        CsvRecord pacienteCsv = pacienteRecords.get(0);
        assertThat(pacienteCsv.headers()).isEqualTo(Paciente.CSV_HEADERS);
        Map<String, String> pacienteMap = pacienteCsv.toMap();
        assertThat(pacienteMap.get("nif")).isEqualTo("12345A");
        assertThat(pacienteMap.get("empresa_id")).isEqualTo(empresaId.toString());
        assertThat(pacienteMap.get("created_at")).contains("T");

        List<CsvRecord> cuestionarioRecords = csvExportService.exportCuestionariosSince(createdAt.minusDays(1));
        assertThat(cuestionarioRecords).hasSize(1);
        CsvRecord cuestionarioCsv = cuestionarioRecords.get(0);
        assertThat(cuestionarioCsv.headers()).isEqualTo(Cuestionario.CSV_HEADERS);
        Map<String, String> cuestionarioMap = cuestionarioCsv.toMap();
        JsonNode respuestasNode = ContractJsonMapper.parseNode(cuestionarioMap.get("respuestas"));
        assertThat(respuestasNode).isNotNull();
        assertThat(respuestasNode.isArray()).isTrue();
        assertThat(respuestasNode.get(0).get("pregunta_codigo").asText()).isEqualTo("pregunta-1");
        JsonNode firmasNode = ContractJsonMapper.parseNode(cuestionarioMap.get("firmas"));
        assertThat(firmasNode).isNotNull();
        assertThat(firmasNode.isArray()).isTrue();
        assertThat(firmasNode.get(0).asText()).isEqualTo("firma-base64");
        assertThat(cuestionarioMap.get("estado")).isEqualTo("completado");
    }
}
