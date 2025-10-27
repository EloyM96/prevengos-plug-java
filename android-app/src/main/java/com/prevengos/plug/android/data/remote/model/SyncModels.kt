package com.prevengos.plug.android.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SyncPushRequest<T>(
    val items: List<T>,
    @Json(name = "sync_token")
    val syncToken: String? = null
)

@JsonClass(generateAdapter = true)
data class SyncResult(
    val updated: List<SyncVersion> = emptyList()
)

@JsonClass(generateAdapter = true)
data class SyncVersion(
    val id: String,
    @Json(name = "last_modified")
    val lastModified: Long,
    @Json(name = "sync_token")
    val syncToken: String?
)

@JsonClass(generateAdapter = true)
data class SyncPullResponse(
    val pacientes: List<PacientePayload> = emptyList(),
    val cuestionarios: List<CuestionarioPayload> = emptyList(),
    @Json(name = "sync_token")
    val syncToken: String? = null,
    @Json(name = "last_synced_at")
    val lastSyncedAt: Long? = null
)
