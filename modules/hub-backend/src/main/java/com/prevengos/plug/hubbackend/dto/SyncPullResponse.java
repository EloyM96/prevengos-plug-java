package com.prevengos.plug.hubbackend.dto;

import java.util.List;

public record SyncPullResponse(List<SyncEventResponse> events, Long nextSyncToken) {
}
