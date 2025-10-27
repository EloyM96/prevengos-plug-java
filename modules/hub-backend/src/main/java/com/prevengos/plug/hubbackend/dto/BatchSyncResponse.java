package com.prevengos.plug.hubbackend.dto;

import java.util.List;
import java.util.UUID;

public record BatchSyncResponse(int processed, List<UUID> identifiers) {
}
