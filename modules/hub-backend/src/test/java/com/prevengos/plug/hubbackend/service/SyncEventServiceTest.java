package com.prevengos.plug.hubbackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prevengos.plug.gateway.sqlserver.SyncEventGateway;
import com.prevengos.plug.hubbackend.config.PrlNotifierProperties;
import com.prevengos.plug.shared.persistence.jdbc.SyncEventRecord;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SyncEventServiceTest {

    private SyncEventGateway syncEventGateway;
    private ObjectMapper objectMapper;
    private SimpleMeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        syncEventGateway = mock(SyncEventGateway.class);
        objectMapper = new ObjectMapper();
        meterRegistry = new SimpleMeterRegistry();
    }

    @Test
    void registerEventEnrichesMetadataWhenPrlNotifierIsEnabled() {
        PrlNotifierProperties properties = new PrlNotifierProperties();
        properties.setEnabled(true);
        properties.setEventsChannel("hub.sync");
        properties.setEventsWebhook("/api/hooks/hub-sync");
        properties.setSharedSecret("super-secret");

        SyncEventMetadataContributor contributor = new PrlNotifierMetadataContributor(properties);
        SyncEventService service = new SyncEventService(syncEventGateway, objectMapper, meterRegistry, List.of(contributor));

        when(syncEventGateway.registerEvent(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SyncEventRecord record = service.registerEvent("paciente-upserted", null, null, null, null, null);

        JsonNode metadata = record.metadata();
        assertThat(metadata).isNotNull();
        assertThat(metadata.get("channel").asText()).isEqualTo("hub.sync");
        assertThat(metadata.get("webhook").asText()).isEqualTo("/api/hooks/hub-sync");
        assertThat(metadata.get("webhook_secret_set").asBoolean()).isTrue();
    }

    @Test
    void registerEventPreservesMetadataWhenNoContributor() {
        SyncEventService service = new SyncEventService(syncEventGateway, objectMapper, meterRegistry, List.of());
        when(syncEventGateway.registerEvent(any())).thenAnswer(invocation -> invocation.getArgument(0));

        JsonNode metadata = objectMapper.createObjectNode().put("source", "android");
        SyncEventRecord record = service.registerEvent("paciente-upserted", null, "android", UUID.randomUUID(), null, metadata);

        assertThat(record.metadata().get("source").asText()).isEqualTo("android");
    }
}
