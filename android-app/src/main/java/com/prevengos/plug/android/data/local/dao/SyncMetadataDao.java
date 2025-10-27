package com.prevengos.plug.android.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.prevengos.plug.android.data.local.entity.SyncMetadata;

@Dao
public interface SyncMetadataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(SyncMetadata metadata);

    @Query("SELECT * FROM sync_metadata WHERE resource_type = :resourceType")
    SyncMetadata getMetadata(String resourceType);
}
