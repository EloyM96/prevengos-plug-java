package com.prevengos.plug.android.data.remote.model;

import java.util.Collections;
import java.util.List;

public class SyncBatch {
    private final String entity;
    private final List<SyncItem> items;

    public SyncBatch(String entity, List<SyncItem> items) {
        this.entity = entity;
        this.items = items == null ? Collections.emptyList() : items;
    }

    public String getEntity() {
        return entity;
    }

    public List<SyncItem> getItems() {
        return items;
    }
}
