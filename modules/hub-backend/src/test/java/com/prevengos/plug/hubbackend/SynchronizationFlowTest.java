package com.prevengos.plug.hubbackend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prevengos.plug.hubbackend.dto.CuestionarioDto;
import com.prevengos.plug.hubbackend.dto.PacienteDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
class SynchronizationFlowTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("prevengos_hub")
            .withUsername("prevengos")
            .withPassword("prevengos");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void offlineCaptureSyncsAndPullsEvents() throws Exception {
        UUID pacienteId = UUID.randomUUID();
        UUID cuestionarioId = UUID.randomUUID();
        UUID empresaId = UUID.randomUUID();
        UUID centroId = UUID.randomUUID();

        List<PacienteDto> pacientes = List.of(
                new PacienteDto(
                        pacienteId,
                        "12345A",
                        "Ana",
                        "Prevengos",
                        LocalDate.of(1990, 5, 20),
                        "F",
                        "+34123456789",
                        "ana.prevengos@example.com",
                        empresaId,
                        centroId,
                        "ext-001",
                        OffsetDateTime.now(ZoneOffset.UTC).minusDays(1),
                        OffsetDateTime.now(ZoneOffset.UTC))
        );

        List<CuestionarioDto> cuestionarios = List.of(
                new CuestionarioDto(
                        cuestionarioId,
                        pacienteId,
                        "ERGONOMIA-2024",
                        "completo",
                        List.of(new CuestionarioDto.RespuestaDto(
                                "pregunta-1",
                                5,
                                null,
                                Map.of("observaciones", "Sin incidencias")
                        )),
                        List.of("firma-1"),
                        List.of("adjunto-1"),
                        OffsetDateTime.now(ZoneOffset.UTC).minusHours(1),
                        OffsetDateTime.now(ZoneOffset.UTC)
                )
        );

        mockMvc.perform(MockMvcRequestBuilders.post("/sincronizacion/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Source-System", "offline-tablet")
                        .content(objectMapper.writeValueAsString(pacientes)))
                .andExpect(status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.post("/sincronizacion/cuestionarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Source-System", "offline-tablet")
                        .content(objectMapper.writeValueAsString(cuestionarios)))
                .andExpect(status().isOk());

        Integer pacienteCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM pacientes", Integer.class);
        Integer cuestionarioCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM cuestionarios", Integer.class);
        Integer syncEventsCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sync_events", Integer.class);

        assertThat(pacienteCount).isEqualTo(1);
        assertThat(cuestionarioCount).isEqualTo(1);
        assertThat(syncEventsCount).isEqualTo(2);

        MvcResult firstPull = mockMvc.perform(MockMvcRequestBuilders.get("/sincronizacion/pull")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode firstBody = objectMapper.readTree(firstPull.getResponse().getContentAsString());
        assertThat(firstBody.get("events")).hasSize(2);
        long nextToken = firstBody.get("nextToken").asLong();

        MvcResult secondPull = mockMvc.perform(MockMvcRequestBuilders.get("/sincronizacion/pull")
                        .param("syncToken", String.valueOf(nextToken))
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode secondBody = objectMapper.readTree(secondPull.getResponse().getContentAsString());
        assertThat(secondBody.get("events")).isEmpty();
        assertThat(secondBody.get("nextToken").asLong()).isEqualTo(nextToken);
    }
}
