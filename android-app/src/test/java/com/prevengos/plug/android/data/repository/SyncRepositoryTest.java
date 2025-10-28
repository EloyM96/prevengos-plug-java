package com.prevengos.plug.android.data.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.prevengos.plug.android.data.local.dao.CuestionarioDao;
import com.prevengos.plug.android.data.local.dao.PacienteDao;
import com.prevengos.plug.android.data.local.dao.SyncMetadataDao;
import com.prevengos.plug.android.data.local.entity.CuestionarioEntity;
import com.prevengos.plug.android.data.local.entity.PacienteEntity;
import com.prevengos.plug.android.data.local.entity.RespuestaLocal;
import com.prevengos.plug.android.data.local.entity.SyncMetadata;
import com.prevengos.plug.android.data.remote.api.PrevengosSyncApi;
import com.prevengos.plug.android.data.remote.model.AsyncJobResponse;
import com.prevengos.plug.android.data.remote.model.SyncChangeEnvelope;
import com.prevengos.plug.android.data.remote.model.SyncChangeItem;
import com.prevengos.plug.android.data.remote.model.SyncEntityPushRequest;
import com.prevengos.plug.android.data.remote.model.SyncPullResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import retrofit2.mock.Calls;

public class SyncRepositoryTest {

    private PacienteDao pacienteDao;
    private CuestionarioDao cuestionarioDao;
    private SyncMetadataDao syncMetadataDao;
    private PrevengosSyncApi syncApi;

    private SyncRepository repository;

    @Before
    public void setUp() {
        pacienteDao = mock(PacienteDao.class);
        cuestionarioDao = mock(CuestionarioDao.class);
        syncMetadataDao = mock(SyncMetadataDao.class);
        syncApi = mock(PrevengosSyncApi.class);
        repository = new SyncRepository(pacienteDao, cuestionarioDao, syncMetadataDao, syncApi, "android-e2e");
    }

    @Test
    public void syncAllPushesDirtyEntitiesAndAppliesPull() throws IOException {
        SyncMetadata metadata = new SyncMetadata("global", 1700000000000L, "token-1");
        when(syncMetadataDao.getMetadata("global")).thenReturn(metadata);

        PacienteEntity dirtyPaciente = new PacienteEntity(
                "paciente-dirty-1",
                "12345678A",
                "Ana",
                "Prevengos",
                "1990-01-01",
                "F",
                "+34911222333",
                "ana.prevengos@example.com",
                "empresa-1",
                "centro-1",
                "EXT-1",
                "2024-03-01T09:00:00Z",
                "2024-03-01T09:00:00Z",
                1700000005000L,
                "token-local",
                true
        );
        when(pacienteDao.dirtyPacientes()).thenReturn(Collections.singletonList(dirtyPaciente));

        CuestionarioEntity dirtyCuestionario = new CuestionarioEntity(
                "cuestionario-dirty-1",
                dirtyPaciente.getPacienteId(),
                "CS-01",
                "completado",
                Collections.singletonList(new RespuestaLocal("peso", "70", "kg", Collections.singletonMap("equipo", "balanza"))),
                Collections.singletonList("dr.prevengos"),
                Collections.singletonList("informe.pdf"),
                "2024-03-01T10:00:00Z",
                "2024-03-01T10:00:00Z",
                1700000006000L,
                "token-local",
                true
        );
        when(cuestionarioDao.dirtyCuestionarios()).thenReturn(Collections.singletonList(dirtyCuestionario));

        when(syncApi.pushPacientes(any())).thenReturn(Calls.response(new AsyncJobResponse(
                "job-1",
                "accepted",
                UUID.randomUUID().toString(),
                "2024-03-01T10:05:00Z"
        )));
        when(syncApi.pushCuestionarios(any())).thenReturn(Calls.response(new AsyncJobResponse(
                "job-2",
                "accepted",
                UUID.randomUUID().toString(),
                "2024-03-01T10:05:00Z"
        )));

        Map<String, Object> pacientePayload = new HashMap<>();
        pacientePayload.put("paciente_id", "remote-paciente-1");
        pacientePayload.put("nif", "87654321B");
        pacientePayload.put("nombre", "Lucía");
        pacientePayload.put("apellidos", "Prevengos");
        pacientePayload.put("fecha_nacimiento", "1992-07-21");
        pacientePayload.put("sexo", "F");
        pacientePayload.put("telefono", "+34910000000");
        pacientePayload.put("email", "lucia.prevengos@example.com");
        pacientePayload.put("empresa_id", "empresa-remote");
        pacientePayload.put("centro_id", "centro-remote");
        pacientePayload.put("externo_ref", "EXT-200");
        pacientePayload.put("created_at", "2024-03-04T09:30:00Z");
        pacientePayload.put("updated_at", "2024-03-04T11:15:00Z");
        pacientePayload.put("last_modified", 1709550900000L);
        pacientePayload.put("sync_token", "remote-token");

        Map<String, Object> cuestionarioPayload = new HashMap<>();
        cuestionarioPayload.put("cuestionario_id", "remote-cuestionario-1");
        cuestionarioPayload.put("paciente_id", "remote-paciente-1");
        cuestionarioPayload.put("plantilla_codigo", "AUTO-CS-01");
        cuestionarioPayload.put("estado", "validado");
        cuestionarioPayload.put("respuestas", Collections.emptyList());
        cuestionarioPayload.put("firmas", Collections.singletonList("dr.prevengos"));
        cuestionarioPayload.put("adjuntos", Collections.singletonList("informe-prevengos.pdf"));
        cuestionarioPayload.put("created_at", "2024-03-04T11:30:00Z");
        cuestionarioPayload.put("updated_at", "2024-03-04T11:30:00Z");
        cuestionarioPayload.put("last_modified", 1709551800000L);
        cuestionarioPayload.put("sync_token", "remote-token");

        SyncChangeItem pacienteChange = new SyncChangeItem(
                UUID.randomUUID().toString(),
                1L,
                pacientePayload,
                false,
                "2024-03-04T12:00:00Z"
        );
        SyncChangeItem cuestionarioDeleted = new SyncChangeItem(
                UUID.randomUUID().toString(),
                1L,
                cuestionarioPayload,
                true,
                "2024-03-04T12:05:00Z"
        );

        Map<String, List<SyncChangeEnvelope>> changes = new HashMap<>();
        changes.put("pacientes", Collections.singletonList(new SyncChangeEnvelope("pacientes", Collections.singletonList(pacienteChange))));
        changes.put("cuestionarios", Collections.singletonList(new SyncChangeEnvelope("cuestionarios", Collections.singletonList(cuestionarioDeleted))));

        SyncPullResponse pullResponse = new SyncPullResponse(
                "2024-03-04T12:10:00Z",
                "token-2",
                changes,
                Collections.singletonList("Conflicto resuelto en servidor")
        );
        when(syncApi.pull(any(), any(), any())).thenReturn(Calls.response(pullResponse));

        repository.syncAll();

        ArgumentCaptor<SyncEntityPushRequest> pacienteRequest = ArgumentCaptor.forClass(SyncEntityPushRequest.class);
        verify(syncApi).pushPacientes(pacienteRequest.capture());
        assertEquals("android-e2e", pacienteRequest.getValue().getClientId());
        assertEquals(1, pacienteRequest.getValue().getItems().size());
        assertEquals("token-1", pacienteRequest.getValue().getLastSyncToken());

        verify(pacienteDao).markAsClean(dirtyPaciente.getPacienteId());
        verify(cuestionarioDao).markAsClean(dirtyCuestionario.getCuestionarioId());

        verify(pacienteDao).upsert(argThat(entity -> "remote-paciente-1".equals(entity.getPacienteId())));
        verify(cuestionarioDao).deleteById("remote-cuestionario-1");

        ArgumentCaptor<SyncMetadata> metadataCaptor = ArgumentCaptor.forClass(SyncMetadata.class);
        verify(syncMetadataDao).upsert(metadataCaptor.capture());
        SyncMetadata savedMetadata = metadataCaptor.getValue();
        assertEquals("global", savedMetadata.getResourceType());
        assertEquals("token-2", savedMetadata.getSyncToken());
        assertNotNull(savedMetadata.getLastSyncedAt());
    }
}
