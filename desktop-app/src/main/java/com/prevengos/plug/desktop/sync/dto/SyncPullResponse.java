package com.prevengos.plug.desktop.sync.dto;

import java.util.List;

public record SyncPullResponse(
        long nextToken,
        List<SyncChange> changes
) {
}
