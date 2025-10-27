package com.prevengos.plug.android.data.repository;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.prevengos.plug.android.data.local.PrevengosDatabase;
import com.prevengos.plug.android.data.local.entity.PacienteEntity;
import com.prevengos.plug.android.data.remote.api.PrevengosSyncApi;
import com.prevengos.plug.android.data.remote.model.CuestionarioPayload;
import com.prevengos.plug.android.data.remote.model.PacientePayload;
import com.prevengos.plug.android.data.remote.model.SyncPullResponse;
import com.prevengos.plug.android.data.remote.model.SyncPushRequest;
import com.prevengos.plug.android.data.remote.model.SyncResult;
import com.prevengos.plug.android.data.remote.model.SyncVersion;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Request;
import retrofit2.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class SyncRepositoryTest {
    private PrevengosDatabase database;
    private SyncRepository syncRepository;
    private FakeSyncApi fakeApi;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, PrevengosDatabase.class)
                .allowMainThreadQueries()
                .build();
        fakeApi = new FakeSyncApi();
        syncRepository = new SyncRepository(
                database.pacienteDao(),
                database.cuestionarioDao(),
                database.syncMetadataDao(),
                fakeApi
        );
    }

    @After
    public void tearDown() {
        if (database != null) {
            database.close();
        }
    }

    @Test
    public void sincronizaPacientesMarcandoVersiones() throws IOException {
        PacienteEntity paciente = new PacienteEntity(
                "123",
                "00000000A",
                "Lucía",
                "Pérez",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                1000,
                null,
                true
        );
        database.pacienteDao().upsert(paciente);
        fakeApi.setVersionToReturn(new SyncVersion(paciente.getPacienteId(), 2000, "v1"));

        syncRepository.syncAll();

        PacienteEntity stored = database.pacienteDao().findById(paciente.getPacienteId());
        assertNotNull(stored);
        assertFalse(stored.isDirty());
        assertEquals(2000, stored.getLastModified());
        assertEquals("v1", stored.getSyncToken());
    }

    private static class FakeSyncApi implements PrevengosSyncApi {
        private final AtomicReference<SyncVersion> versionToReturn = new AtomicReference<>();

        void setVersionToReturn(SyncVersion version) {
            versionToReturn.set(version);
        }

        @Override
        public Call<SyncResult> pushPacientes(SyncPushRequest<PacientePayload> request) {
            SyncVersion version = versionToReturn.get();
            List<SyncVersion> versions = version != null ? Collections.singletonList(version) : Collections.emptyList();
            return new ImmediateCall<>(new SyncResult(versions));
        }

        @Override
        public Call<SyncResult> pushCuestionarios(SyncPushRequest<CuestionarioPayload> request) {
            return new ImmediateCall<>(new SyncResult());
        }

        @Override
        public Call<SyncPullResponse> pull(Long since, String syncToken) {
            return new ImmediateCall<>(new SyncPullResponse(Collections.emptyList(), Collections.emptyList(), syncToken, since));
        }
    }

    private static class ImmediateCall<T> implements Call<T> {
        private final T responseBody;

        ImmediateCall(T responseBody) {
            this.responseBody = responseBody;
        }

        @Override
        public Response<T> execute() {
            return Response.success(responseBody);
        }

        @Override
        public void enqueue(Callback<T> callback) {
            throw new UnsupportedOperationException("ImmediateCall does not support enqueue");
        }

        @Override
        public boolean isExecuted() {
            return true;
        }

        @Override
        public void cancel() {
        }

        @Override
        public boolean isCanceled() {
            return false;
        }

        @Override
        public Call<T> clone() {
            return new ImmediateCall<>(responseBody);
        }

        @Override
        public Request request() {
            return new Request.Builder().url("http://localhost/").build();
        }

        @Override
        public okhttp3.Timeout timeout() {
            return new okhttp3.Timeout();
        }
    }
}
