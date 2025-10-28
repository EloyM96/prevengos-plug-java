package com.prevengos.plug.gateway.sqlserver;

import java.time.OffsetDateTime;
import java.util.List;

public interface SyncEventGateway {

    SyncEventRecord registerEvent(SyncEventRecord event);

    List<SyncEventRecord> fetchNextEvents(Long syncToken, OffsetDateTime since, int limit);
}
