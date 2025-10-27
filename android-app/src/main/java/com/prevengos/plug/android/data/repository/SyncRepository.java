package com.prevengos.plug.android.data.repository;

import com.prevengos.plug.android.data.local.dao.CuestionarioDao;
import com.prevengos.plug.android.data.local.dao.PacienteDao;
import com.prevengos.plug.android.data.local.dao.SyncMetadataDao;
import com.prevengos.plug.android.data.local.entity.CuestionarioEntity;
import com.prevengos.plug.android.data.local.entity.PacienteEntity;
import com.prevengos.plug.android.data.local.entity.SyncMetadata;
import com.prevengos.plug.android.data.mappers.EntityMappers;
import com.prevengos.plug.android.data.remote.api.PrevengosSyncApi;
import com.prevengos.plug.android.data.remote.model.CuestionarioPayload;
import com.prevengos.plug.android.data.remote.model.PacientePayload;
import com.prevengos.plug.android.data.remote.model.SyncPullResponse;
import com.prevengos.plug.android.data.remote.model.SyncPushRequest;
import com.prevengos.plug.android.data.remote.model.SyncResult;
import com.prevengos.plug.android.data.remote.model.SyncVersion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class SyncRepository {
    private static final String GLOBAL_RESOURCE = "global";

    private final PacienteDao pacienteDao;
    private final CuestionarioDao cuestionarioDao;
    private final SyncMetadataDao syncMetadataDao;
    private final PrevengosSyncApi syncApi;

    public SyncRepository(PacienteDao pacienteDao,
                          CuestionarioDao cuestionarioDao,
                          SyncMetadataDao syncMetadataDao,
                          PrevengosSyncApi syncApi) {
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
        if (dirty == null || dirty.isEmpty()) {
            return;
        }
        List<PacientePayload> payloads = new ArrayList<>();
        for (PacienteEntity entity : dirty) {
            payloads.add(EntityMappers.toPayload(entity));
        }
        SyncPushRequest<PacientePayload> request = new SyncPushRequest<>(payloads, null);
        Call<SyncResult> call = syncApi.pushPacientes(request);
        Response<SyncResult> response = call.execute();
        if (!response.isSuccessful() || response.body() == null) {
            throw new IOException("Error al sincronizar pacientes: " + response.code());
        }
        for (SyncVersion version : response.body().getUpdated()) {
            pacienteDao.markAsSynced(version.getId(), version.getLastModified(), version.getSyncToken());
        }
    }

    private void pushCuestionarios() throws IOException {
        List<CuestionarioEntity> dirty = cuestionarioDao.dirtyCuestionarios();
        if (dirty == null || dirty.isEmpty()) {
            return;
        }
        List<CuestionarioPayload> payloads = new ArrayList<>();
        for (CuestionarioEntity entity : dirty) {
            payloads.add(EntityMappers.toPayload(entity));
        }
        SyncPushRequest<CuestionarioPayload> request = new SyncPushRequest<>(payloads, null);
        Call<SyncResult> call = syncApi.pushCuestionarios(request);
        Response<SyncResult> response = call.execute();
        if (!response.isSuccessful() || response.body() == null) {
            throw new IOException("Error al sincronizar cuestionarios: " + response.code());
        }
        for (SyncVersion version : response.body().getUpdated()) {
            cuestionarioDao.markAsSynced(version.getId(), version.getLastModified(), version.getSyncToken());
        }
    }

    private void pullUpdates() throws IOException {
        SyncMetadata metadata = syncMetadataDao.getMetadata(GLOBAL_RESOURCE);
        Long since = metadata != null ? metadata.getLastSyncedAt() : null;
        String syncToken = metadata != null ? metadata.getSyncToken() : null;
        Call<SyncPullResponse> call = syncApi.pull(since, syncToken);
        Response<SyncPullResponse> response = call.execute();
        if (!response.isSuccessful() || response.body() == null) {
            throw new IOException("Error al recuperar actualizaciones: " + response.code());
        }
        SyncPullResponse body = response.body();
        for (PacientePayload payload : body.getPacientes()) {
            pacienteDao.upsert(EntityMappers.toEntity(payload, false));
        }
        for (CuestionarioPayload payload : body.getCuestionarios()) {
            cuestionarioDao.upsert(EntityMappers.toEntity(payload, false));
        }
        long lastSyncedAt = body.getLastSyncedAt() != null
                ? body.getLastSyncedAt()
                : System.currentTimeMillis();
        String newSyncToken = body.getSyncToken() != null ? body.getSyncToken() : (metadata != null ? metadata.getSyncToken() : null);
        SyncMetadata newMetadata = new SyncMetadata(GLOBAL_RESOURCE, lastSyncedAt, newSyncToken);
        syncMetadataDao.upsert(newMetadata);
    }
}
