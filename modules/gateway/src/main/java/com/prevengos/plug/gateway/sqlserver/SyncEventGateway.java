package com.prevengos.plug.gateway.sqlserver;

import com.prevengos.plug.shared.persistence.jdbc.SyncEventRecord;

import java.time.OffsetDateTime;
import java.util.List;

public interface SyncEventGateway {

    SyncEventRecord registerEvent(SyncEventRecord event);

    List<SyncEventRecord> fetchNextEvents(Long syncToken, OffsetDateTime since, int limit);
}
