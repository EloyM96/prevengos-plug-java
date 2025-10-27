package com.prevengos.plug.hubbackend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class SynchronizationControllerTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void syncEndpointsPersistEventsAndExposePull() throws Exception {
        Map<String, Object> paciente = Map.of(
                "paciente_id", UUID.randomUUID().toString(),
                "nif", "76543B",
                "nombre", "Luis",
                "apellidos", "García",
                "fecha_nacimiento", "1985-05-20",
                "sexo", "M",
                "telefono", "+34987654321",
                "email", "luis.garcia@example.com",
                "empresa_id", UUID.randomUUID().toString(),
                "centro_id", UUID.randomUUID().toString(),
                "externo_ref", "EXT-456",
                "created_at", OffsetDateTime.now().minusDays(2).toString(),
                "updated_at", OffsetDateTime.now().minusDays(1).toString()
        );

        mockMvc.perform(MockMvcRequestBuilders.post("/sincronizacion/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Source-System", "integration-test")
                        .content(objectMapper.writeValueAsString(List.of(paciente))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processed").value(1));

        Map<String, Object> respuesta = Map.of(
                "pregunta_codigo", "P1",
                "valor", "SI"
        );
        Map<String, Object> cuestionario = Map.of(
                "cuestionario_id", UUID.randomUUID().toString(),
                "paciente_id", paciente.get("paciente_id"),
                "plantilla_codigo", "CS-01",
                "estado", "completado",
                "respuestas", List.of(respuesta),
                "created_at", OffsetDateTime.now().minusDays(1).toString(),
                "updated_at", OffsetDateTime.now().toString()
        );

        mockMvc.perform(MockMvcRequestBuilders.post("/sincronizacion/cuestionarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Source-System", "integration-test")
                        .content(objectMapper.writeValueAsString(List.of(cuestionario))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processed").value(1));

        var pullResult = mockMvc.perform(MockMvcRequestBuilders.get("/sincronizacion/pull")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events").isArray())
                .andReturn();

        JsonNode root = objectMapper.readTree(pullResult.getResponse().getContentAsString());
        JsonNode events = root.get("events");
        assertThat(events.size()).isGreaterThanOrEqualTo(2);
        long firstToken = events.get(0).get("sync_token").asLong();
        OffsetDateTime firstOccurred = OffsetDateTime.parse(events.get(0).get("occurred_at").asText());

        mockMvc.perform(MockMvcRequestBuilders.get("/sincronizacion/pull")
                        .param("syncToken", String.valueOf(firstToken))
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events[0].sync_token").value(events.get(1).get("sync_token").asLong()));

        mockMvc.perform(MockMvcRequestBuilders.get("/sincronizacion/pull")
                        .param("since", firstOccurred.plusSeconds(1).toString())
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events").isArray())
                .andExpect(jsonPath("$.events.length()").value(0));
    }

    @Test
    void openApiDocumentationContainsSynchronizationEndpoints() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/sincronizacion/pacientes']").exists())
                .andExpect(jsonPath("$.paths['/sincronizacion/cuestionarios']").exists())
                .andExpect(jsonPath("$.paths['/sincronizacion/pull']").exists());
    }
}
