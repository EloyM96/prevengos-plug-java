package com.prevengos.plug.android.data.repository;

import com.prevengos.plug.android.data.local.dao.CuestionarioDao;
import com.prevengos.plug.android.data.local.dao.PacienteDao;
import com.prevengos.plug.android.data.local.dao.SyncMetadataDao;
import com.prevengos.plug.android.data.local.entity.CuestionarioEntity;
import com.prevengos.plug.android.data.local.entity.PacienteEntity;
import com.prevengos.plug.android.data.local.entity.SyncMetadata;
import com.prevengos.plug.android.data.mappers.EntityMappers;
import com.prevengos.plug.android.data.remote.api.PrevengosSyncApi;
import com.prevengos.plug.android.data.remote.model.SyncPullResponse;
import com.prevengos.plug.android.data.remote.model.SyncPushRequest;
import com.prevengos.plug.android.data.remote.model.SyncResult;
import com.prevengos.plug.android.data.remote.model.SyncVersion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

public class SyncRepository {
    private static final String GLOBAL_RESOURCE = "global";

    private final PacienteDao pacienteDao;
    private final CuestionarioDao cuestionarioDao;
    private final SyncMetadataDao syncMetadataDao;
    private final PrevengosSyncApi syncApi;

    public SyncRepository(
            PacienteDao pacienteDao,
            CuestionarioDao cuestionarioDao,
            SyncMetadataDao syncMetadataDao,
            PrevengosSyncApi syncApi
    ) {
        this.pacienteDao = pacienteDao;
        this.cuestionarioDao = cuestionarioDao;
        this.syncMetadataDao = syncMetadataDao;
        this.syncApi = syncApi;
    }

    public void syncAll() throws IOException {
        pushPacientes();
        pushCuestionarios();
        pullUpdates();
    }

    private void pushPacientes() throws IOException {
        List<PacienteEntity> dirty = pacienteDao.dirtyPacientes();
        if (dirty.isEmpty()) {
            return;
        }
        List<com.prevengos.plug.android.data.remote.model.PacientePayload> payloads = new ArrayList<>();
        for (PacienteEntity entity : dirty) {
            payloads.add(EntityMappers.toPayload(entity));
        }
        Response<SyncResult> response = syncApi.pushPacientes(new SyncPushRequest<>(payloads)).execute();
        if (response.isSuccessful() && response.body() != null) {
            for (SyncVersion version : response.body().getUpdated()) {
                pacienteDao.markAsSynced(version.getId(), version.getLastModified(), version.getSyncToken());
            }
        } else {
            throw new IOException("Error pushing pacientes: " + response.code());
        }
    }

    private void pushCuestionarios() throws IOException {
        List<CuestionarioEntity> dirty = cuestionarioDao.dirtyCuestionarios();
        if (dirty.isEmpty()) {
            return;
        }
        List<com.prevengos.plug.android.data.remote.model.CuestionarioPayload> payloads = new ArrayList<>();
        for (CuestionarioEntity entity : dirty) {
            payloads.add(EntityMappers.toPayload(entity));
        }
        Response<SyncResult> response = syncApi.pushCuestionarios(new SyncPushRequest<>(payloads)).execute();
        if (response.isSuccessful() && response.body() != null) {
            for (SyncVersion version : response.body().getUpdated()) {
                cuestionarioDao.markAsSynced(version.getId(), version.getLastModified(), version.getSyncToken());
            }
        } else {
            throw new IOException("Error pushing cuestionarios: " + response.code());
        }
    }

    private void pullUpdates() throws IOException {
        SyncMetadata metadata = syncMetadataDao.getMetadata(GLOBAL_RESOURCE);
        Long lastSyncedAt = metadata != null ? metadata.getLastSyncedAt() : null;
        String syncToken = metadata != null ? metadata.getSyncToken() : null;
        Response<SyncPullResponse> response = syncApi.pull(lastSyncedAt, syncToken).execute();
        if (!response.isSuccessful() || response.body() == null) {
            throw new IOException("Error pulling updates: " + response.code());
        }
        SyncPullResponse result = response.body();
        for (com.prevengos.plug.android.data.remote.model.PacientePayload payload : result.getPacientes()) {
            pacienteDao.upsert(EntityMappers.toEntity(payload, false));
        }
        for (com.prevengos.plug.android.data.remote.model.CuestionarioPayload payload : result.getCuestionarios()) {
            cuestionarioDao.upsert(EntityMappers.toEntity(payload, false));
        }
        SyncMetadata newMetadata = new SyncMetadata(
                GLOBAL_RESOURCE,
                result.getLastSyncedAt() != null ? result.getLastSyncedAt() : System.currentTimeMillis(),
                result.getSyncToken() != null ? result.getSyncToken() : (metadata != null ? metadata.getSyncToken() : null)
        );
        syncMetadataDao.upsert(newMetadata);
    }
}
