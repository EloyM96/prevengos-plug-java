package com.prevengos.plug.hubbackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prevengos.plug.hubbackend.dto.BatchSyncResponse;
import com.prevengos.plug.shared.dto.CuestionarioDto;
import com.prevengos.plug.shared.dto.PacienteDto;
import com.prevengos.plug.hubbackend.dto.SyncEventResponse;
import com.prevengos.plug.hubbackend.dto.SyncPullResponse;
import com.prevengos.plug.hubbackend.service.CuestionarioService;
import com.prevengos.plug.hubbackend.service.PacienteService;
import com.prevengos.plug.hubbackend.service.SyncEventService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
class SynchronizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PacienteService pacienteService;

    @MockBean
    private CuestionarioService cuestionarioService;

    @MockBean
    private SyncEventService syncEventService;

    @Test
    void syncPacientesEndpointDelegatesToService() throws Exception {
        when(pacienteService.upsertPacientes(any(), any())).thenReturn(new BatchSyncResponse(1, List.of(UUID.randomUUID())));

        PacienteDto dto = new PacienteDto(
                UUID.randomUUID(),
                "76543B",
                "Luis",
                "García",
                LocalDate.of(1985, 5, 20),
                "M",
                "+34987654321",
                "luis.garcia@example.com",
                UUID.randomUUID(),
                UUID.randomUUID(),
                "EXT-456",
                OffsetDateTime.now().minusDays(2),
                OffsetDateTime.now().minusDays(1)
        );

        mockMvc.perform(MockMvcRequestBuilders.post("/sincronizacion/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Source-System", "integration-test")
                        .content(objectMapper.writeValueAsString(List.of(dto))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processed").value(1));

        verify(pacienteService).upsertPacientes(any(), Mockito.eq("integration-test"));
    }

    @Test
    void syncCuestionariosEndpointDelegatesToService() throws Exception {
        when(cuestionarioService.upsertCuestionarios(any(), any())).thenReturn(new BatchSyncResponse(1, List.of(UUID.randomUUID())));

        CuestionarioDto dto = new CuestionarioDto(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "CS-01",
                "completado",
                List.of(),
                null,
                null,
                OffsetDateTime.now().minusDays(1),
                OffsetDateTime.now()
        );

        mockMvc.perform(MockMvcRequestBuilders.post("/sincronizacion/cuestionarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Source-System", "integration-test")
                        .content(objectMapper.writeValueAsString(List.of(dto))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processed").value(1));

        verify(cuestionarioService).upsertCuestionarios(any(), Mockito.eq("integration-test"));
    }

    @Test
    void pullEndpointReturnsEvents() throws Exception {
        SyncEventResponse response = new SyncEventResponse(
                100L,
                UUID.randomUUID(),
                "paciente-upserted",
                1,
                OffsetDateTime.now(),
                "test",
                null,
                null,
                objectMapper.createObjectNode(),
                objectMapper.createObjectNode()
        );
        when(syncEventService.pull(any(), any(), any())).thenReturn(new SyncPullResponse(List.of(response), 100L));

        mockMvc.perform(MockMvcRequestBuilders.get("/sincronizacion/pull")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events[0].event_type").value("paciente-upserted"));
    }
}
