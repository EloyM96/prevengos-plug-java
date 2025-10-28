package com.prevengos.plug.android.data.repository;

import com.prevengos.plug.android.data.local.dao.CuestionarioDao;
import com.prevengos.plug.android.data.local.dao.PacienteDao;
import com.prevengos.plug.android.data.local.dao.SyncMetadataDao;
import com.prevengos.plug.android.data.local.entity.CuestionarioEntity;
import com.prevengos.plug.android.data.local.entity.PacienteEntity;
import com.prevengos.plug.android.data.local.entity.SyncMetadata;
import com.prevengos.plug.android.data.mappers.EntityMappers;
import com.prevengos.plug.android.data.remote.api.PrevengosSyncApi;
import com.prevengos.plug.shared.sync.dto.CuestionarioDto;
import com.prevengos.plug.shared.sync.dto.PacienteDto;
import com.prevengos.plug.shared.sync.dto.SyncPullResponse;
import com.prevengos.plug.shared.sync.dto.SyncPushRequest;
import com.prevengos.plug.shared.sync.dto.SyncPushResponse;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import retrofit2.Response;

public class SyncRepository {
    private static final String GLOBAL_RESOURCE = "global";
    private static final int PULL_LIMIT = 200;

    private final PacienteDao pacienteDao;
    private final CuestionarioDao cuestionarioDao;
    private final SyncMetadataDao syncMetadataDao;
    private final PrevengosSyncApi syncApi;
    private final String clientId;

    public SyncRepository(PacienteDao pacienteDao,
                          CuestionarioDao cuestionarioDao,
                          SyncMetadataDao syncMetadataDao,
                          PrevengosSyncApi syncApi,
                          String clientId) {
        this.pacienteDao = pacienteDao;
        this.cuestionarioDao = cuestionarioDao;
        this.syncMetadataDao = syncMetadataDao;
        this.syncApi = syncApi;
        this.clientId = clientId;
    }

    public void syncAll() throws IOException {
        SyncMetadata metadata = syncMetadataDao.getMetadata(GLOBAL_RESOURCE);
        pushChanges(metadata);
        pullUpdates(metadata);
    }

    private void pushChanges(SyncMetadata metadata) throws IOException {
        List<PacienteEntity> dirtyPacientes = pacienteDao.dirtyPacientes();
        List<CuestionarioEntity> dirtyCuestionarios = cuestionarioDao.dirtyCuestionarios();
        boolean hasPacientes = dirtyPacientes != null && !dirtyPacientes.isEmpty();
        boolean hasCuestionarios = dirtyCuestionarios != null && !dirtyCuestionarios.isEmpty();
        if (!hasPacientes && !hasCuestionarios) {
            return;
        }

        SyncPushRequest request = new SyncPushRequest(
                clientId,
                UUID.randomUUID(),
                hasPacientes ? dirtyPacientes.stream().map(EntityMappers::toSyncPaciente).toList() : List.of(),
                hasCuestionarios ? dirtyCuestionarios.stream().map(EntityMappers::toSyncCuestionario).toList() : List.of()
        );

        Response<SyncPushResponse> response = syncApi.push(request).execute();
        if (!response.isSuccessful() || response.body() == null) {
            throw new IOException("Error al sincronizar cambios locales: " + response.code());
        }
        long syncToken = response.body().lastSyncToken();

        if (hasPacientes) {
            for (PacienteEntity entity : dirtyPacientes) {
                pacienteDao.markAsClean(entity.getPacienteId());
            }
        }
        if (hasCuestionarios) {
            for (CuestionarioEntity entity : dirtyCuestionarios) {
                cuestionarioDao.markAsClean(entity.getCuestionarioId());
            }
        }

        String tokenAsString = Long.toString(syncToken);
        syncMetadataDao.upsert(new SyncMetadata(GLOBAL_RESOURCE, nowMillis(), tokenAsString));
    }

    private void pullUpdates(SyncMetadata metadata) throws IOException {
        Long currentToken = metadata != null ? parseLong(metadata.getSyncToken()) : null;
        Response<SyncPullResponse> response = syncApi.pull(currentToken, PULL_LIMIT).execute();
        if (!response.isSuccessful() || response.body() == null) {
            throw new IOException("Error al recuperar actualizaciones: " + response.code());
        }

        SyncPullResponse body = response.body();
        for (PacienteDto remoto : body.pacientes()) {
            pacienteDao.upsert(EntityMappers.toEntity(remoto, false));
        }
        for (CuestionarioDto remoto : body.cuestionarios()) {
            cuestionarioDao.upsert(EntityMappers.toEntity(remoto, false));
        }

        long nextToken = body.nextSyncToken();
        syncMetadataDao.upsert(new SyncMetadata(GLOBAL_RESOURCE, nowMillis(), Long.toString(nextToken)));
    }

    private long nowMillis() {
        return OffsetDateTime.now().toInstant().toEpochMilli();
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
