package com.prevengos.plug.android.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_metadata")
data class SyncMetadata(
    @PrimaryKey
    @ColumnInfo(name = "resource_type")
    val resourceType: String,
    @ColumnInfo(name = "last_synced_at")
    val lastSyncedAt: Long?,
    @ColumnInfo(name = "sync_token")
    val syncToken: String?
)
