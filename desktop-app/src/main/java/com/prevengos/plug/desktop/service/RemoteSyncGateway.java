package com.prevengos.plug.desktop.service;

import com.prevengos.plug.desktop.service.dto.PullResponse;
import com.prevengos.plug.desktop.service.dto.SyncBatch;

/**
 * Contrato para interactuar con los endpoints de sincronización remotos descritos en
 * {@code docs/data-stores/sync-flows.md}.
 */
public interface RemoteSyncGateway {

    SyncBatch pushBatch(SyncBatch batch);

    PullResponse pull(String syncToken, String since, int limit);
}
