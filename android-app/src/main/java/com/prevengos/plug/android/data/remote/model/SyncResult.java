package com.prevengos.plug.android.data.remote.model;

import java.util.Collections;
import java.util.List;

public class SyncResult {
    private final List<SyncVersion> updated;

    public SyncResult() {
        this(Collections.emptyList());
    }

    public SyncResult(List<SyncVersion> updated) {
        this.updated = updated;
    }

    public List<SyncVersion> getUpdated() {
        return updated;
    }
}
