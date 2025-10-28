package com.prevengos.plug.hubbackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prevengos.plug.gateway.sqlserver.SyncEventGateway;
import com.prevengos.plug.gateway.sqlserver.SyncEventRecord;
import com.prevengos.plug.hubbackend.dto.SyncEventResponse;
import com.prevengos.plug.hubbackend.dto.SyncPullResponse;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class SyncEventService {

    private static final int DEFAULT_EVENT_VERSION = 1;
    public static final String DEFAULT_SOURCE = "hub-backend";

    private static final Logger logger = LoggerFactory.getLogger(SyncEventService.class);

    private final SyncEventGateway syncEventGateway;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;
    private final Timer syncPullTimer;

    public SyncEventService(SyncEventGateway syncEventGateway,
                            ObjectMapper objectMapper,
                            MeterRegistry meterRegistry) {
        this.syncEventGateway = syncEventGateway;
        this.objectMapper = objectMapper;
        this.meterRegistry = meterRegistry;
        this.syncPullTimer = Timer
                .builder("hub.sync.pull.duration")
                .description("Tiempo en construir la respuesta de pull de sincronización")
                .register(meterRegistry);
    }

    @Transactional
    public SyncEventRecord registerEvent(String eventType, Object payload, String source,
                                         UUID correlationId, UUID causationId, JsonNode metadata) {
        JsonNode payloadNode = payload != null ? objectMapper.valueToTree(payload) : objectMapper.nullNode();
        JsonNode metadataNode = metadata != null ? metadata : objectMapper.nullNode();
        String resolvedSource = source != null && !source.isBlank() ? source : DEFAULT_SOURCE;
        SyncEventRecord request = new SyncEventRecord(
                null,
                UUID.randomUUID(),
                eventType,
                DEFAULT_EVENT_VERSION,
                OffsetDateTime.now(ZoneOffset.UTC),
                resolvedSource,
                correlationId,
                causationId,
                payloadNode,
                metadataNode
        );
        SyncEventRecord savedEvent = syncEventGateway.registerEvent(request);
        meterRegistry.counter("hub.sync.events.registered",
                "event_type", eventType,
                "source", resolvedSource)
                .increment();
        logger.info("Evento de sincronización registrado",
                StructuredArguments.kv("eventType", eventType),
                StructuredArguments.kv("source", resolvedSource),
                StructuredArguments.kv("syncToken", savedEvent.syncToken()),
                StructuredArguments.kv("eventId", savedEvent.eventId()));
        return savedEvent;
    }

    @Transactional(readOnly = true)
    public SyncPullResponse pull(Long syncToken, OffsetDateTime since, int limit) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            List<SyncEventRecord> events = syncEventGateway.fetchNextEvents(syncToken, since, limit);
            List<SyncEventResponse> responses = events.stream()
                    .map(this::toResponse)
                    .toList();
            Long nextToken = responses.isEmpty() ? syncToken : responses.get(responses.size() - 1).syncToken();
            meterRegistry.counter("hub.sync.pull.requests",
                    "has_since", since != null ? "true" : "false")
                    .increment();
            meterRegistry.summary("hub.sync.pull.batch.size").record(responses.size());
            logger.info("Pull de sincronización ejecutado",
                    StructuredArguments.kv("requestedToken", syncToken),
                    StructuredArguments.kv("resultCount", responses.size()),
                    StructuredArguments.kv("nextToken", nextToken),
                    StructuredArguments.kv("limit", limit));
            return new SyncPullResponse(responses, nextToken);
        } finally {
            sample.stop(syncPullTimer);
        }
    }

    private SyncEventResponse toResponse(SyncEventRecord event) {
        JsonNode metadata = event.metadata() != null ? event.metadata() : objectMapper.nullNode();
        JsonNode payload = event.payload() != null ? event.payload() : objectMapper.nullNode();
        return new SyncEventResponse(
                event.syncToken(),
                event.eventId(),
                event.eventType(),
                event.version(),
                event.occurredAt(),
                event.source(),
                event.correlationId(),
                event.causationId(),
                payload,
                metadata
        );
    }
}
