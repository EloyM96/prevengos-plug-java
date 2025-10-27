package com.prevengos.plug.hubbackend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prevengos.plug.hubbackend.domain.SyncEvent;
import com.prevengos.plug.hubbackend.dto.SyncEventResponse;
import com.prevengos.plug.hubbackend.dto.SyncPullResponse;
import com.prevengos.plug.hubbackend.repository.SyncEventRepository;
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
    private static final String DEFAULT_SOURCE = "hub-backend";

    private final SyncEventRepository syncEventRepository;
    private final ObjectMapper objectMapper;

    public SyncEventService(SyncEventRepository syncEventRepository, ObjectMapper objectMapper) {
        this.syncEventRepository = syncEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public SyncEvent registerEvent(String eventType, Object payload, String source,
                                   UUID correlationId, UUID causationId, JsonNode metadata) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);
            String metadataJson = metadata != null ? objectMapper.writeValueAsString(metadata) : null;
            SyncEvent event = new SyncEvent(
                    UUID.randomUUID(),
                    eventType,
                    DEFAULT_EVENT_VERSION,
                    OffsetDateTime.now(ZoneOffset.UTC),
                    source != null ? source : DEFAULT_SOURCE,
                    correlationId,
                    causationId,
                    payloadJson,
                    metadataJson
            );
            return syncEventRepository.save(event);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize event payload", e);
        }
    }

    @Transactional(readOnly = true)
    public SyncPullResponse pull(Long syncToken, OffsetDateTime since, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "syncToken"));
        Page<SyncEvent> page = syncEventRepository.findNextEvents(syncToken, since, pageable);
        List<SyncEventResponse> responses = page.stream()
                .map(this::toResponse)
                .toList();
        Long nextToken = responses.isEmpty() ? syncToken : responses.get(responses.size() - 1).syncToken();
        return new SyncPullResponse(responses, nextToken);
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
