package com.prevengos.plug.android.data.repository

import com.prevengos.plug.android.data.local.dao.CuestionarioDao
import com.prevengos.plug.android.data.local.dao.PacienteDao
import com.prevengos.plug.android.data.local.dao.SyncMetadataDao
import com.prevengos.plug.android.data.local.entity.SyncMetadata
import com.prevengos.plug.android.data.mappers.toEntity
import com.prevengos.plug.android.data.mappers.toPayload
import com.prevengos.plug.android.data.remote.api.PrevengosSyncApi
import com.prevengos.plug.android.data.remote.model.SyncPushRequest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class SyncRepository(
    private val pacienteDao: PacienteDao,
    private val cuestionarioDao: CuestionarioDao,
    private val syncMetadataDao: SyncMetadataDao,
    private val syncApi: PrevengosSyncApi,
    private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun syncAll() = withContext(ioDispatcher) {
        pushPacientes()
        pushCuestionarios()
        pullUpdates()
    }

    private suspend fun pushPacientes() {
        val dirty = pacienteDao.dirtyPacientes()
        if (dirty.isEmpty()) return
        val response = syncApi.pushPacientes(SyncPushRequest(items = dirty.map { it.toPayload() }))
        response.updated.forEach { version ->
            pacienteDao.markAsSynced(version.id, version.lastModified, version.syncToken)
        }
    }

    private suspend fun pushCuestionarios() {
        val dirty = cuestionarioDao.dirtyCuestionarios()
        if (dirty.isEmpty()) return
        val response = syncApi.pushCuestionarios(SyncPushRequest(items = dirty.map { it.toPayload() }))
        response.updated.forEach { version ->
            cuestionarioDao.markAsSynced(version.id, version.lastModified, version.syncToken)
        }
    }

    private suspend fun pullUpdates() {
        val metadata = syncMetadataDao.getMetadata(GLOBAL_RESOURCE)
        val result = syncApi.pull(metadata?.lastSyncedAt, metadata?.syncToken)
        result.pacientes.forEach { payload ->
            pacienteDao.upsert(payload.toEntity(isDirty = false))
        }
        result.cuestionarios.forEach { payload ->
            cuestionarioDao.upsert(payload.toEntity(isDirty = false))
        }
        val newMetadata = SyncMetadata(
            resourceType = GLOBAL_RESOURCE,
            lastSyncedAt = result.lastSyncedAt ?: System.currentTimeMillis(),
            syncToken = result.syncToken ?: metadata?.syncToken
        )
        syncMetadataDao.upsert(newMetadata)
    }

    companion object {
        private const val GLOBAL_RESOURCE = "global"
    }
}
