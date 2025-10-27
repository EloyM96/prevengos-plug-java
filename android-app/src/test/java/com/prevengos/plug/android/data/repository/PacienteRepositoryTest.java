package com.prevengos.plug.android.data.repository;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.prevengos.plug.android.data.local.PrevengosDatabase;
import com.prevengos.plug.android.data.local.entity.PacienteEntity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class PacienteRepositoryTest {
    private PrevengosDatabase database;
    private PacienteRepository repository;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, PrevengosDatabase.class)
                .allowMainThreadQueries()
                .build();
        repository = new PacienteRepository(database.pacienteDao());
    }

    @After
    public void tearDown() {
        if (database != null) {
            database.clearAllTables();
            database.close();
        }
    }

    @Test
    public void crearPacienteMarcaRegistroComoPendiente() {
        PacienteEntity paciente = repository.createPaciente(
                "12345678A",
                "María",
                "García",
                "600000000",
                "maria@example.com"
        );

        PacienteEntity stored = database.pacienteDao().findById(paciente.getPacienteId());
        assertNotNull(stored);
        assertEquals(paciente.getPacienteId(), stored.getPacienteId());
        assertTrue(stored.isDirty());
    }
}
