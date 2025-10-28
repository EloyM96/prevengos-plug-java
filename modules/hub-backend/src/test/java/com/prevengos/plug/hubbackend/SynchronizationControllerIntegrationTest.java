package com.prevengos.plug.hubbackend;

import com.prevengos.plug.hubbackend.dto.BatchSyncResponse;
import com.prevengos.plug.hubbackend.dto.SyncPullResponse;
import com.prevengos.plug.hubbackend.dto.SyncEventResponse;
import com.prevengos.plug.shared.dto.PacienteDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SynchronizationControllerIntegrationTest extends SqlServerIntegrationTestSupport {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Test
    void pushPacientesAndPullEvents() {
        UUID pacienteId = UUID.randomUUID();
        UUID empresaId = UUID.randomUUID();
        UUID centroId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC).withNano(0);

        PacienteDto paciente = new PacienteDto(
                pacienteId,
                "12345A",
                "Ana",
                "Prevengos",
                LocalDate.of(1990, 5, 12),
                "F",
                "+34123456789",
                "ana.prevengos@example.com",
                empresaId,
                centroId,
                "EXT-1",
                now,
                now
        );

        ResponseEntity<BatchSyncResponse> response = restTemplate.postForEntity(
                "/sincronizacion/pacientes",
                List.of(paciente),
                BatchSyncResponse.class
        );

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().processed()).isEqualTo(1);
        assertThat(response.getBody().identifiers()).containsExactly(pacienteId);

        Long storedSyncToken = jdbcTemplate.queryForObject(
                "SELECT sync_token FROM pacientes WHERE paciente_id = :paciente_id",
                new MapSqlParameterSource().addValue("paciente_id", pacienteId),
                Long.class
        );
        assertThat(storedSyncToken).isNotNull();

        ResponseEntity<SyncPullResponse> pullResponse = restTemplate.exchange(
                "/sincronizacion/pull?limit=10",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(pullResponse.getStatusCode().is2xxSuccessful()).isTrue();
        SyncPullResponse pullBody = pullResponse.getBody();
        assertThat(pullBody).isNotNull();
        assertThat(pullBody.events()).hasSize(1);

        SyncEventResponse event = pullBody.events().get(0);
        assertThat(event.eventType()).isEqualTo("paciente-upserted");
        assertThat(event.payload().get("paciente_id").asText()).isEqualTo(pacienteId.toString());
        assertThat(event.syncToken()).isEqualTo(storedSyncToken);
        assertThat(pullBody.nextSyncToken()).isEqualTo(event.syncToken());

        ResponseEntity<SyncPullResponse> emptyPull = restTemplate.exchange(
                "/sincronizacion/pull?syncToken=" + event.syncToken() + "&limit=10",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(emptyPull.getStatusCode().is2xxSuccessful()).isTrue();
        SyncPullResponse emptyBody = emptyPull.getBody();
        assertThat(emptyBody).isNotNull();
        assertThat(emptyBody.events()).isEmpty();
        assertThat(emptyBody.nextSyncToken()).isEqualTo(event.syncToken());

        Integer eventCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM sync_events",
                new MapSqlParameterSource(),
                Integer.class
        );
        assertThat(eventCount).isEqualTo(1);
    }
}
