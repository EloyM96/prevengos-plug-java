package com.prevengos.plug.hubbackend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.prevengos.plug.gateway.sqlserver.SyncEventGateway;
import com.prevengos.plug.shared.persistence.jdbc.SyncEventRecord;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

class SyncEventServiceTest {

    private SyncEventGateway syncEventGateway;
    private ObjectMapper objectMapper;
    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        syncEventGateway = mock(SyncEventGateway.class);
        objectMapper = new ObjectMapper();
        meterRegistry = new SimpleMeterRegistry();
    }

    @Test
    void registerEventAugmentsMetadataAndRecordsMetrics() {
        SyncEventMetadataContributor contributor = (mapper, currentMetadata) -> {
            ObjectNode enriched = mapper.createObjectNode();
            if (currentMetadata != null && !currentMetadata.isNull()) {
                enriched.set("original", currentMetadata);
            }
            enriched.put("channel", "ops-alerts");
            return enriched;
        };

        when(syncEventGateway.registerEvent(any())).thenAnswer(invocation -> {
            SyncEventRecord submitted = invocation.getArgument(0);
            return new SyncEventRecord(
                    42L,
                    submitted.eventId(),
                    submitted.eventType(),
                    submitted.version(),
                    submitted.occurredAt(),
                    submitted.source(),
                    submitted.correlationId(),
                    submitted.causationId(),
                    submitted.payload(),
                    submitted.metadata()
            );
        });

        SyncEventService service = new SyncEventService(syncEventGateway, objectMapper, meterRegistry, List.of(contributor));
        SyncEventRecord stored = service.registerEvent(
                "paciente-upserted",
                java.util.Map.of("paciente_id", "abc-123"),
                "android-app",
                UUID.randomUUID(),
                null,
                objectMapper.createObjectNode().put("severity", "info")
        );

        assertThat(stored.syncToken()).isEqualTo(42L);

        ArgumentCaptor<SyncEventRecord> eventCaptor = ArgumentCaptor.forClass(SyncEventRecord.class);
        verify(syncEventGateway).registerEvent(eventCaptor.capture());
        SyncEventRecord persisted = eventCaptor.getValue();
        assertThat(persisted.source()).isEqualTo("android-app");
        assertThat(persisted.payload().get("paciente_id").asText()).isEqualTo("abc-123");
        assertThat(persisted.metadata().get("channel").asText()).isEqualTo("ops-alerts");
        assertThat(persisted.metadata().get("original").get("severity").asText()).isEqualTo("info");

        double counter = meterRegistry.find("hub.sync.events.registered")
                .tags("event_type", "paciente-upserted", "source", "android-app")
                .counter()
                .count();
        assertThat(counter).isEqualTo(1.0d);
    }

    @Test
    void pullBuildsResponsesAndCollectsMetrics() {
        SyncEventRecord record = new SyncEventRecord(
                7L,
                UUID.randomUUID(),
                "cuestionario-upserted",
                1,
                OffsetDateTime.now(ZoneOffset.UTC),
                "desktop-app",
                null,
                null,
                objectMapper.createObjectNode().put("cuestionario_id", "quest-1"),
                objectMapper.createObjectNode().put("channel", "ops-alerts")
        );
        when(syncEventGateway.fetchNextEvents(null, null, 5)).thenReturn(List.of(record));

        SyncEventService service = new SyncEventService(syncEventGateway, objectMapper, meterRegistry, List.of());
        var response = service.pull(null, null, 5);

        assertThat(response.events()).hasSize(1);
        assertThat(response.events().get(0).eventType()).isEqualTo("cuestionario-upserted");
        assertThat(response.events().get(0).metadata().get("channel").asText()).isEqualTo("ops-alerts");
        assertThat(response.nextToken()).isEqualTo(7L);

        double pullCounter = meterRegistry.find("hub.sync.pull.requests").counter().count();
        assertThat(pullCounter).isEqualTo(1.0d);
        long batchMeasurements = meterRegistry.find("hub.sync.pull.batch.size").summary().count();
        assertThat(batchMeasurements).isEqualTo(1L);
    }
}
