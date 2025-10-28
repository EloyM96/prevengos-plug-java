package com.prevengos.plug.android.data.remote.model;

import java.util.Collections;
import java.util.List;

public class SyncChangeEnvelope {
    private final String entity;
    private final List<SyncChangeItem> items;

    public SyncChangeEnvelope(String entity, List<SyncChangeItem> items) {
        this.entity = entity;
        this.items = items == null ? Collections.emptyList() : items;
    }

    public String getEntity() {
        return entity;
    }

    public List<SyncChangeItem> getItems() {
        return items;
    }
}
