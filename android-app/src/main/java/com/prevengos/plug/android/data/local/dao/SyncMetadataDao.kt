package com.prevengos.plug.android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.prevengos.plug.android.data.local.entity.SyncMetadata

@Dao
interface SyncMetadataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(metadata: SyncMetadata)

    @Query("SELECT * FROM sync_metadata WHERE resource_type = :resourceType")
    suspend fun getMetadata(resourceType: String): SyncMetadata?
}
