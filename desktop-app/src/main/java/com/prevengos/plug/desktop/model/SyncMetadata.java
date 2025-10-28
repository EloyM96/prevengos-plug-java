package com.prevengos.plug.desktop.model;

import java.time.OffsetDateTime;

public record SyncMetadata(
        Long lastSyncToken,
        OffsetDateTime lastPullAt,
        OffsetDateTime lastPushAt
) {
}
