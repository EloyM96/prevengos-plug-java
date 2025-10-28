package com.prevengos.plug.android.data.repository;

import com.prevengos.plug.android.data.local.dao.CuestionarioDao;
import com.prevengos.plug.android.data.local.dao.PacienteDao;
import com.prevengos.plug.android.data.local.dao.SyncMetadataDao;
import com.prevengos.plug.android.data.local.entity.CuestionarioEntity;
import com.prevengos.plug.android.data.local.entity.PacienteEntity;
import com.prevengos.plug.android.data.local.entity.SyncMetadata;
import com.prevengos.plug.android.data.mappers.EntityMappers;
import com.prevengos.plug.android.data.remote.api.PrevengosSyncApi;
import com.prevengos.plug.android.data.remote.model.AsyncJobResponse;
import com.prevengos.plug.android.data.remote.model.CuestionarioPayload;
import com.prevengos.plug.android.data.remote.model.PacientePayload;
import com.prevengos.plug.android.data.remote.model.SyncChangeEnvelope;
import com.prevengos.plug.android.data.remote.model.SyncChangeItem;
import com.prevengos.plug.android.data.remote.model.SyncEntityPushRequest;
import com.prevengos.plug.android.data.remote.model.SyncItem;
import com.prevengos.plug.android.data.remote.model.SyncPullResponse;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonDataException;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import retrofit2.Response;

public class SyncRepository {
    private static final String GLOBAL_RESOURCE = "global";
    private static final String ENTITY_PACIENTES = "pacientes";
    private static final String ENTITY_CUESTIONARIOS = "cuestionarios";
    private static final int PULL_LIMIT = 200;

    private final PacienteDao pacienteDao;
    private final CuestionarioDao cuestionarioDao;
    private final SyncMetadataDao syncMetadataDao;
    private final PrevengosSyncApi syncApi;
    private final JsonAdapter<PacientePayload> pacienteAdapter;
    private final JsonAdapter<CuestionarioPayload> cuestionarioAdapter;
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
        Moshi moshi = new Moshi.Builder().build();
        this.pacienteAdapter = moshi.adapter(PacientePayload.class);
        this.cuestionarioAdapter = moshi.adapter(CuestionarioPayload.class);
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

        if (hasPacientes) {
            pushPacientes(metadata, dirtyPacientes);
        }
        if (hasCuestionarios) {
            pushCuestionarios(metadata, dirtyCuestionarios);
        }
    }

    private void pushPacientes(SyncMetadata metadata, List<PacienteEntity> dirtyPacientes) throws IOException {
        List<SyncItem> items = new ArrayList<>();
        for (PacienteEntity entity : dirtyPacientes) {
            items.add(toSyncItem(entity.getLastModified(), EntityMappers.toPayload(entity)));
        }
        SyncEntityPushRequest request = new SyncEntityPushRequest(
                clientId,
                metadata != null ? metadata.getSyncToken() : null,
                items);
        Response<AsyncJobResponse> response = syncApi.pushPacientes(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Error al sincronizar pacientes: " + response.code());
        }
        for (PacienteEntity entity : dirtyPacientes) {
            pacienteDao.markAsClean(entity.getPacienteId());
        }
    }

    private void pushCuestionarios(SyncMetadata metadata, List<CuestionarioEntity> dirtyCuestionarios) throws IOException {
        List<SyncItem> items = new ArrayList<>();
        for (CuestionarioEntity entity : dirtyCuestionarios) {
            items.add(toSyncItem(entity.getLastModified(), EntityMappers.toPayload(entity)));
        }
        SyncEntityPushRequest request = new SyncEntityPushRequest(
                clientId,
                metadata != null ? metadata.getSyncToken() : null,
                items);
        Response<AsyncJobResponse> response = syncApi.pushCuestionarios(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Error al sincronizar cuestionarios: " + response.code());
        }
        for (CuestionarioEntity entity : dirtyCuestionarios) {
            cuestionarioDao.markAsClean(entity.getCuestionarioId());
        }
    }

    private SyncItem toSyncItem(long lastModified, Object payload) {
        long observed = lastModified > 0 ? lastModified : System.currentTimeMillis();
        return new SyncItem(
                UUID.randomUUID().toString(),
                observed,
                payload,
                false,
                formatInstant(observed));
    }

    private void pullUpdates(SyncMetadata metadata) throws IOException {
        String since = metadata != null ? metadata.getSyncToken() : null;
        Call<SyncPullResponse> call = syncApi.pull(
                since,
                ENTITY_PACIENTES + "," + ENTITY_CUESTIONARIOS,
                PULL_LIMIT);
        Response<SyncPullResponse> response = call.execute();
        if (!response.isSuccessful() || response.body() == null) {
            throw new IOException("Error al recuperar actualizaciones: " + response.code());
        }

        SyncPullResponse body = response.body();
        applyPacienteChanges(extractItems(body.getChanges(), ENTITY_PACIENTES));
        applyCuestionarioChanges(extractItems(body.getChanges(), ENTITY_CUESTIONARIOS));

        long syncedAt = parseInstant(body.getServerTimestamp());
        String nextSince = body.getNextSince() != null ? body.getNextSince() : since;
        SyncMetadata newMetadata = new SyncMetadata(GLOBAL_RESOURCE, syncedAt, nextSince);
        syncMetadataDao.upsert(newMetadata);
    }

    private List<SyncChangeItem> extractItems(Map<String, List<SyncChangeEnvelope>> changes, String entity) {
        if (changes == null || changes.isEmpty()) {
            return Collections.emptyList();
        }
        List<SyncChangeItem> items = new ArrayList<>();
        for (Map.Entry<String, List<SyncChangeEnvelope>> entry : changes.entrySet()) {
            List<SyncChangeEnvelope> envelopes = entry.getValue();
            if (envelopes == null) {
                continue;
            }
            for (SyncChangeEnvelope envelope : envelopes) {
                if (envelope == null) {
                    continue;
                }
                if (!entity.equals(entry.getKey()) && !entity.equals(envelope.getEntity())) {
                    continue;
                }
                List<SyncChangeItem> changeItems = envelope.getItems();
                if (changeItems != null) {
                    items.addAll(changeItems);
                }
            }
        }
        return items;
    }

    private void applyPacienteChanges(List<SyncChangeItem> items) throws IOException {
        for (SyncChangeItem item : items) {
            if (item.getPayload() == null) {
                continue;
            }
            PacientePayload payload = parsePacientePayload(item.getPayload());
            if (item.isDeleted()) {
                pacienteDao.deleteById(payload.getPacienteId());
            } else {
                pacienteDao.upsert(EntityMappers.toEntity(payload, false));
            }
        }
    }

    private void applyCuestionarioChanges(List<SyncChangeItem> items) throws IOException {
        for (SyncChangeItem item : items) {
            if (item.getPayload() == null) {
                continue;
            }
            CuestionarioPayload payload = parseCuestionarioPayload(item.getPayload());
            if (item.isDeleted()) {
                cuestionarioDao.deleteById(payload.getCuestionarioId());
            } else {
                cuestionarioDao.upsert(EntityMappers.toEntity(payload, false));
            }
        }
    }

    private PacientePayload parsePacientePayload(Map<String, Object> payload) throws IOException {
        try {
            PacientePayload parsed = pacienteAdapter.fromJsonValue(payload);
            if (parsed == null) {
                throw new IOException("Payload de paciente vacío");
            }
            return parsed;
        } catch (JsonDataException exception) {
            throw new IOException("Payload de paciente inválido", exception);
        }
    }

    private CuestionarioPayload parseCuestionarioPayload(Map<String, Object> payload) throws IOException {
        try {
            CuestionarioPayload parsed = cuestionarioAdapter.fromJsonValue(payload);
            if (parsed == null) {
                throw new IOException("Payload de cuestionario vacío");
            }
            return parsed;
        } catch (JsonDataException exception) {
            throw new IOException("Payload de cuestionario inválido", exception);
        }
    }

    private String formatInstant(long epochMillis) {
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneOffset.UTC).toString();
    }

    private long parseInstant(String iso) {
        if (iso == null || iso.isEmpty()) {
            return System.currentTimeMillis();
        }
        try {
            return OffsetDateTime.parse(iso).toInstant().toEpochMilli();
        } catch (DateTimeParseException exception) {
            return System.currentTimeMillis();
        }
    }
}
