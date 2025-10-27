package com.prevengos.plug.hubbackend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prevengos.plug.hubbackend.domain.SyncEvent;
import com.prevengos.plug.hubbackend.dto.SyncEventResponse;
import com.prevengos.plug.hubbackend.dto.SyncPullResponse;
import com.prevengos.plug.hubbackend.repository.SyncEventRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    private final SyncEventRepository syncEventRepository;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;
    private final Timer syncPullTimer;

    public SyncEventService(SyncEventRepository syncEventRepository,
                           ObjectMapper objectMapper,
                           MeterRegistry meterRegistry) {
        this.syncEventRepository = syncEventRepository;
        this.objectMapper = objectMapper;
        this.meterRegistry = meterRegistry;
        this.syncPullTimer = Timer
                .builder("hub.sync.pull.duration")
                .description("Tiempo en construir la respuesta de pull de sincronización")
                .register(meterRegistry);
    }

    @Transactional
    public SyncEvent registerEvent(String eventType, Object payload, String source,
                                   UUID correlationId, UUID causationId, JsonNode metadata) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);
            String metadataJson = metadata != null ? objectMapper.writeValueAsString(metadata) : null;
            String resolvedSource = source != null && !source.isBlank() ? source : DEFAULT_SOURCE;
            SyncEvent event = new SyncEvent(
                    UUID.randomUUID(),
                    eventType,
                    DEFAULT_EVENT_VERSION,
                    OffsetDateTime.now(ZoneOffset.UTC),
                    resolvedSource,
                    correlationId,
                    causationId,
                    payloadJson,
                    metadataJson
            );
            SyncEvent savedEvent = syncEventRepository.save(event);
            meterRegistry.counter("hub.sync.events.registered",
                    "event_type", eventType,
                    "source", resolvedSource)
                    .increment();
            logger.info("Evento de sincronización registrado",
                    StructuredArguments.kv("eventType", eventType),
                    StructuredArguments.kv("source", resolvedSource),
                    StructuredArguments.kv("syncToken", savedEvent.getSyncToken()),
                    StructuredArguments.kv("eventId", savedEvent.getEventId()));
            return savedEvent;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize event payload", e);
        }
    }

    @Transactional(readOnly = true)
    public SyncPullResponse pull(Long syncToken, OffsetDateTime since, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "syncToken"));
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            Page<SyncEvent> page = syncEventRepository.findNextEvents(syncToken, since, pageable);
            List<SyncEventResponse> responses = page.stream()
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

    private SyncEventResponse toResponse(SyncEvent event) {
        return new SyncEventResponse(
                event.getSyncToken(),
                event.getEventId(),
                event.getEventType(),
                event.getVersion(),
                event.getOccurredAt(),
                event.getSource(),
                event.getCorrelationId(),
                event.getCausationId(),
                readTree(event.getPayload()),
                event.getMetadata() != null ? readTree(event.getMetadata()) : objectMapper.nullNode()
        );
    }

    private JsonNode readTree(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to deserialize event payload", e);
        }
    }
}
