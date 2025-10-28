package com.prevengos.plug.android.data.remote.model;

import com.squareup.moshi.Json;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SyncPullResponse {
    @Json(name = "server_timestamp")
    private final String serverTimestamp;
    @Json(name = "next_since")
    private final String nextSince;
    private final Map<String, List<SyncChangeEnvelope>> changes;
    private final List<String> warnings;

    public SyncPullResponse(String serverTimestamp,
                            String nextSince,
                            Map<String, List<SyncChangeEnvelope>> changes,
                            List<String> warnings) {
        this.serverTimestamp = serverTimestamp;
        this.nextSince = nextSince;
        this.changes = changes == null ? Collections.emptyMap() : changes;
        this.warnings = warnings == null ? Collections.emptyList() : warnings;
    }

    public String getServerTimestamp() {
        return serverTimestamp;
    }

    public String getNextSince() {
        return nextSince;
    }

    public Map<String, List<SyncChangeEnvelope>> getChanges() {
        return changes;
    }

    public List<String> getWarnings() {
        return warnings;
    }
}
