package com.prevengos.plug.hubbackend;

import com.prevengos.plug.hubbackend.domain.Paciente;
import com.prevengos.plug.hubbackend.dto.BatchSyncResponse;
import com.prevengos.plug.hubbackend.dto.PacienteDto;
import com.prevengos.plug.hubbackend.repository.PacienteRepository;
import com.prevengos.plug.hubbackend.repository.SyncEventRepository;
import com.prevengos.plug.hubbackend.service.PacienteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class PacienteServiceTest {

    @Autowired
    private PacienteService pacienteService;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private SyncEventRepository syncEventRepository;

    @Test
    void upsertPacienteIsIdempotent() {
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

        BatchSyncResponse response = pacienteService.upsertPacientes(List.of(dto), "test-suite");
        assertThat(response.processed()).isEqualTo(1);
        Paciente paciente = pacienteRepository.findById(pacienteId).orElseThrow();
        long firstToken = paciente.getSyncToken();
        assertThat(firstToken).isGreaterThan(0);
        assertThat(paciente.getNombre()).isEqualTo("Ana");

        BatchSyncResponse secondResponse = pacienteService.upsertPacientes(List.of(dto), "test-suite");
        assertThat(secondResponse.processed()).isEqualTo(1);
        Paciente updated = pacienteRepository.findById(pacienteId).orElseThrow();
        assertThat(updated.getSyncToken()).isGreaterThanOrEqualTo(firstToken);
        assertThat(syncEventRepository.count()).isEqualTo(2);
    }
}
