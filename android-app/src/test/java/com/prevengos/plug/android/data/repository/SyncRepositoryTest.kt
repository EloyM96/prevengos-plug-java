package com.prevengos.plug.android.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.prevengos.plug.android.data.local.PrevengosDatabase
import com.prevengos.plug.android.data.local.entity.PacienteEntity
import com.prevengos.plug.android.data.remote.api.PrevengosSyncApi
import com.prevengos.plug.android.data.remote.model.CuestionarioPayload
import com.prevengos.plug.android.data.remote.model.PacientePayload
import com.prevengos.plug.android.data.remote.model.SyncPullResponse
import com.prevengos.plug.android.data.remote.model.SyncPushRequest
import com.prevengos.plug.android.data.remote.model.SyncResult
import com.prevengos.plug.android.data.remote.model.SyncVersion
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class SyncRepositoryTest {
    private lateinit var database: PrevengosDatabase
    private lateinit var syncRepository: SyncRepository
    private lateinit var fakeApi: FakeSyncApi
    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(context, PrevengosDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        fakeApi = FakeSyncApi()
        syncRepository = SyncRepository(
            pacienteDao = database.pacienteDao(),
            cuestionarioDao = database.cuestionarioDao(),
            syncMetadataDao = database.syncMetadataDao(),
            syncApi = fakeApi,
            ioDispatcher = dispatcher
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun sincronizaPacientesMarcandoVersiones() = runTest {
        val paciente = PacienteEntity(
            pacienteId = "123",
            nif = "00000000A",
            nombre = "Lucía",
            apellidos = "Pérez",
            fechaNacimiento = null,
            sexo = null,
            telefono = null,
            email = null,
            empresaId = null,
            centroId = null,
            externoRef = null,
            createdAt = null,
            updatedAt = null,
            lastModified = 1000,
            syncToken = null,
            isDirty = true
        )
        database.pacienteDao().upsert(paciente)
        fakeApi.versionToReturn = SyncVersion(id = paciente.pacienteId, lastModified = 2000, syncToken = "v1")

        syncRepository.syncAll()

        val stored = database.pacienteDao().findById(paciente.pacienteId)
        requireNotNull(stored)
        assertFalse(stored.isDirty)
        assertEquals(2000, stored.lastModified)
        assertEquals("v1", stored.syncToken)
    }

    private class FakeSyncApi : PrevengosSyncApi {
        var versionToReturn: SyncVersion? = null

        override suspend fun pushPacientes(request: SyncPushRequest<PacientePayload>): SyncResult {
            return SyncResult(updated = listOfNotNull(versionToReturn))
        }

        override suspend fun pushCuestionarios(request: SyncPushRequest<CuestionarioPayload>): SyncResult {
            return SyncResult()
        }

        override suspend fun pull(since: Long?, syncToken: String?): SyncPullResponse {
            return SyncPullResponse(
                pacientes = emptyList(),
                cuestionarios = emptyList(),
                syncToken = syncToken,
                lastSyncedAt = since
            )
        }
    }
}
