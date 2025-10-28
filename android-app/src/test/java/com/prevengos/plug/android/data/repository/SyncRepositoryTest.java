package com.prevengos.plug.android.data.repository;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
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
import com.prevengos.plug.android.data.remote.model.SyncPullResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.mock.Calls;

public class SyncRepositoryTest {
    private PacienteDao pacienteDao;
    private CuestionarioDao cuestionarioDao;
    private SyncMetadataDao syncMetadataDao;
    private PrevengosSyncApi syncApi;
    private SyncRepository repository;

    @Before
    public void setUp() {
        pacienteDao = Mockito.mock(PacienteDao.class);
        cuestionarioDao = Mockito.mock(CuestionarioDao.class);
        syncMetadataDao = Mockito.mock(SyncMetadataDao.class);
        syncApi = Mockito.mock(PrevengosSyncApi.class);
        repository = new SyncRepository(pacienteDao, cuestionarioDao, syncMetadataDao, syncApi, "client-1");
    }

    @Test
    public void syncAll_enviaCambiosYLlenaCache() throws Exception {
        PacienteEntity dirtyPaciente = new PacienteEntity(
                "pac-local",
                "00000000A",
                "María",
                "López",
                null,
                null,
                "600000000",
                "maria@example.com",
                null,
                null,
                null,
                null,
                null,
                System.currentTimeMillis(),
                null,
                true);
        CuestionarioEntity dirtyCuestionario = new CuestionarioEntity(
                "cue-local",
                "pac-local",
                "anamnesis",
                "borrador",
                Collections.singletonList(new RespuestaLocal("nota", "", null, null)),
                Collections.emptyList(),
                Collections.emptyList(),
                null,
                null,
                System.currentTimeMillis(),
                null,
                true);
        when(pacienteDao.dirtyPacientes()).thenReturn(Collections.singletonList(dirtyPaciente));
        when(cuestionarioDao.dirtyCuestionarios()).thenReturn(Collections.singletonList(dirtyCuestionario));
        when(syncMetadataDao.getMetadata("global")).thenReturn(new SyncMetadata("global", null, "token-1"));
        when(syncApi.pushPacientes(any())).thenReturn(Calls.response(new AsyncJobResponse("job", "queued", "trace", "2024")));
        when(syncApi.pushCuestionarios(any())).thenReturn(Calls.response(new AsyncJobResponse("job", "queued", "trace", "2024")));

        Map<String, Object> pacientePayload = new HashMap<>();
        pacientePayload.put("paciente_id", "pac-remoto");
        pacientePayload.put("nif", "11111111B");
        pacientePayload.put("nombre", "Juan");
        pacientePayload.put("apellidos", "Pérez");
        pacientePayload.put("fecha_nacimiento", null);
        pacientePayload.put("sexo", null);
        pacientePayload.put("telefono", null);
        pacientePayload.put("email", null);
        pacientePayload.put("empresa_id", null);
        pacientePayload.put("centro_id", null);
        pacientePayload.put("externo_ref", null);
        pacientePayload.put("created_at", null);
        pacientePayload.put("updated_at", null);
        pacientePayload.put("last_modified", 10L);
        pacientePayload.put("sync_token", "tok-remote");

        Map<String, Object> cuestionarioPayload = new HashMap<>();
        cuestionarioPayload.put("cuestionario_id", "cue-remoto");
        cuestionarioPayload.put("paciente_id", "pac-remoto");
        cuestionarioPayload.put("plantilla_codigo", "anamnesis");
        cuestionarioPayload.put("estado", "firmado");
        List<Map<String, Object>> respuestas = new ArrayList<>();
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("pregunta_codigo", "nota_inicial");
        respuesta.put("valor", "observación");
        respuesta.put("unidad", null);
        respuesta.put("metadata", null);
        respuestas.add(respuesta);
        cuestionarioPayload.put("respuestas", respuestas);
        cuestionarioPayload.put("firmas", Collections.emptyList());
        cuestionarioPayload.put("adjuntos", Collections.emptyList());
        cuestionarioPayload.put("created_at", null);
        cuestionarioPayload.put("updated_at", null);
        cuestionarioPayload.put("last_modified", 10L);
        cuestionarioPayload.put("sync_token", "tok-cue");

        SyncChangeItem pacienteItem = new SyncChangeItem("ev-1", 1L, pacientePayload, false, "2024-01-01T00:00:00Z");
        SyncChangeItem cuestionarioItem = new SyncChangeItem("ev-2", 1L, cuestionarioPayload, false, "2024-01-01T00:00:00Z");
        SyncChangeEnvelope pacienteEnvelope = new SyncChangeEnvelope("pacientes", Collections.singletonList(pacienteItem));
        SyncChangeEnvelope cuestionarioEnvelope = new SyncChangeEnvelope("cuestionarios", Collections.singletonList(cuestionarioItem));
        Map<String, List<SyncChangeEnvelope>> cambios = new HashMap<>();
        cambios.put("pacientes", Collections.singletonList(pacienteEnvelope));
        cambios.put("cuestionarios", Collections.singletonList(cuestionarioEnvelope));
        SyncPullResponse pullResponse = new SyncPullResponse("2024-01-01T00:00:00Z", "token-2", cambios, Collections.emptyList());
        when(syncApi.pull(anyString(), anyString(), anyInt())).thenReturn(Calls.response(pullResponse));

        repository.syncAll();

        verify(pacienteDao).markAsClean("pac-local");
        verify(cuestionarioDao).markAsClean("cue-local");
        verify(pacienteDao).upsert(any(PacienteEntity.class));
        verify(cuestionarioDao).upsert(any(CuestionarioEntity.class));
        ArgumentCaptor<SyncMetadata> metadataCaptor = ArgumentCaptor.forClass(SyncMetadata.class);
        verify(syncMetadataDao).upsert(metadataCaptor.capture());
        assertEquals("token-2", metadataCaptor.getValue().getSyncToken());
    }
}
