package com.prevengos.plug.android.data.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
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
import com.prevengos.plug.shared.sync.dto.CuestionarioDto;
import com.prevengos.plug.shared.sync.dto.PacienteDto;
import com.prevengos.plug.shared.sync.dto.SyncPullResponse;
import com.prevengos.plug.shared.sync.dto.SyncPushResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
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
    public void syncAllPushesAndPullsUsingNewContract() throws IOException {
        SyncMetadata metadata = new SyncMetadata("global", 1700000000000L, "10");
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
                "8",
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
                "8",
                true
        );
        when(cuestionarioDao.dirtyCuestionarios()).thenReturn(Collections.singletonList(dirtyCuestionario));

        SyncPushResponse pushResponse = new SyncPushResponse(1, 1, 25L, List.of(UUID.randomUUID()));
        when(syncApi.push(any())).thenReturn(Calls.response(pushResponse));

        PacienteDto remotePaciente = new PacienteDto(
                UUID.randomUUID(),
                "87654321B",
                "Lucía",
                "Prevengos",
                OffsetDateTime.parse("1992-07-21T00:00:00Z").toLocalDate(),
                "F",
                "+34910000000",
                "lucia.prevengos@example.com",
                UUID.randomUUID(),
                UUID.randomUUID(),
                "EXT-200",
                OffsetDateTime.parse("2024-03-04T09:30:00Z"),
                OffsetDateTime.parse("2024-03-04T11:15:00Z"),
                OffsetDateTime.parse("2024-03-04T11:15:00Z"),
                30L
        );

        CuestionarioDto remoteCuestionario = new CuestionarioDto(
                UUID.randomUUID(),
                remotePaciente.pacienteId(),
                "AUTO-CS-01",
                "validado",
                "[]",
                "[\"dr.prevengos\"]",
                "[\"informe-prevengos.pdf\"]",
                OffsetDateTime.parse("2024-03-04T11:30:00Z"),
                OffsetDateTime.parse("2024-03-04T11:30:00Z"),
                OffsetDateTime.parse("2024-03-04T11:30:00Z"),
                31L
        );

        SyncPullResponse pullResponse = new SyncPullResponse(
                List.of(remotePaciente),
                List.of(remoteCuestionario),
                List.of(),
                32L
        );
        when(syncApi.pull(any(), any())).thenReturn(Calls.response(pullResponse));

        repository.syncAll();

        verify(syncApi).push(any());
        verify(pacienteDao).markAsClean(dirtyPaciente.getPacienteId());
        verify(cuestionarioDao).markAsClean(dirtyCuestionario.getCuestionarioId());

        verify(pacienteDao).upsert(any(PacienteEntity.class));
        verify(cuestionarioDao).upsert(any(CuestionarioEntity.class));

        ArgumentCaptor<SyncMetadata> metadataCaptor = ArgumentCaptor.forClass(SyncMetadata.class);
        verify(syncMetadataDao).upsert(metadataCaptor.capture());
        SyncMetadata savedMetadata = metadataCaptor.getValue();
        assertEquals("global", savedMetadata.getResourceType());
        assertEquals("32", savedMetadata.getSyncToken());
        assertNotNull(savedMetadata.getLastSyncedAt());
    }
}
