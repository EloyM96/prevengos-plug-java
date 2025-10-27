package com.prevengos.plug.android.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.prevengos.plug.android.data.local.PrevengosDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class PacienteRepositoryTest {
    private lateinit var database: PrevengosDatabase
    private lateinit var repository: PacienteRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(context, PrevengosDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = PacienteRepository(database.pacienteDao())
    }

    @After
    fun tearDown() {
        database.clearAllTables()
        database.close()
    }

    @Test
    fun crearPacienteMarcaRegistroComoPendiente() = runTest {
        val paciente = repository.createPaciente(
            nif = "12345678A",
            nombre = "María",
            apellidos = "García",
            telefono = "600000000",
            email = "maria@example.com"
        )

        val stored = database.pacienteDao().findById(paciente.pacienteId)
        requireNotNull(stored)
        assertEquals(paciente.pacienteId, stored.pacienteId)
        assertTrue(stored.isDirty)
    }
}
