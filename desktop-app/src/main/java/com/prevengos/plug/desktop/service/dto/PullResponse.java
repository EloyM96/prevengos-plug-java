package com.prevengos.plug.desktop.service.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.prevengos.plug.desktop.service.dto.SyncEventPayload;

import java.util.List;

/**
 * Respuesta del endpoint {@code /sincronizacion/pull}.
 */
public final class PullResponse {

    private final List<SyncEventPayload> events;
    private final String nextToken;

    @JsonCreator
    public PullResponse(@JsonProperty("events") List<SyncEventPayload> events,
                        @JsonProperty("nextToken") String nextToken) {
        this.events = List.copyOf(events != null ? events : List.of());
        this.nextToken = nextToken;
    }

    public List<SyncEventPayload> getEvents() {
        return events;
    }

    public String getNextToken() {
        return nextToken;
    }

    public boolean hasEvents() {
        return !events.isEmpty();
    }

    public SyncEventPayload latestEvent() {
        if (events.isEmpty()) {
            return null;
        }
        return events.get(events.size() - 1);
    }
}
