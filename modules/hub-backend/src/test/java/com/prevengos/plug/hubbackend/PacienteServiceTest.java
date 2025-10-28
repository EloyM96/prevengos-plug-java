package com.prevengos.plug.hubbackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prevengos.plug.gateway.sqlserver.PacienteGateway;
import com.prevengos.plug.hubbackend.dto.BatchSyncResponse;
import com.prevengos.plug.shared.dto.PacienteDto;
import com.prevengos.plug.shared.persistence.jdbc.PacienteRecord;
import com.prevengos.plug.shared.persistence.jdbc.SyncEventRecord;
import com.prevengos.plug.hubbackend.service.PacienteService;
import com.prevengos.plug.hubbackend.service.SyncEventService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PacienteServiceTest {

    @Mock
    private PacienteGateway pacienteGateway;

    @Mock
    private SyncEventService syncEventService;

    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

    private PacienteService pacienteService;

    @BeforeEach
    void setUp() {
        pacienteService = new PacienteService(pacienteGateway, syncEventService, meterRegistry);
    }

    @Test
    void upsertPacienteDelegatesToGateway() {
        UUID pacienteId = UUID.randomUUID();
        PacienteDto dto = new PacienteDto(
                pacienteId,
                "12345A",
                "Ana",
                "Pérez",
                LocalDate.of(1990, 1, 1),
                "F",
                "+34123456789",
                "ana.perez@example.com",
                UUID.randomUUID(),
                UUID.randomUUID(),
                "EXT-123",
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1),
                OffsetDateTime.now(ZoneOffset.UTC)
        );

        ObjectMapper mapper = new ObjectMapper();
        when(syncEventService.registerEvent(any(), any(), any(), any(), any(), any()))
                .thenReturn(new SyncEventRecord(10L, UUID.randomUUID(), "paciente-upserted", 1,
                        OffsetDateTime.now(ZoneOffset.UTC), "test", null, null,
                        mapper.nullNode(), mapper.nullNode()));

        BatchSyncResponse response = pacienteService.upsertPacientes(List.of(dto), "test-suite");

        assertThat(response.processed()).isEqualTo(1);
        ArgumentCaptor<PacienteRecord> captor = ArgumentCaptor.forClass(PacienteRecord.class);
        verify(pacienteGateway).upsertPaciente(captor.capture());
        PacienteRecord saved = captor.getValue();
        assertThat(saved.pacienteId()).isEqualTo(pacienteId);
        assertThat(saved.syncToken()).isEqualTo(10L);
    }
}
