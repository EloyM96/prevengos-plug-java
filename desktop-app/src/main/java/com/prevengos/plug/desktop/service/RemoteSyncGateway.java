package com.prevengos.plug.desktop.service;

import com.prevengos.plug.shared.sync.dto.SyncPullResponse;
import com.prevengos.plug.shared.sync.dto.SyncPushRequest;
import com.prevengos.plug.shared.sync.dto.SyncPushResponse;

/**
 * Contrato para interactuar con los endpoints de sincronización remotos descritos en
 * {@code docs/data-stores/sync-flows.md}.
 */
public interface RemoteSyncGateway {

    SyncPushResponse push(SyncPushRequest request);

    SyncPullResponse pull(Long syncToken, int limit);
}
