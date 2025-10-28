package com.prevengos.plug.android.data.local;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.prevengos.plug.android.data.local.dao.PacienteDao;
import com.prevengos.plug.android.data.local.entity.PacienteEntity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class PacienteDaoTest {
    private PrevengosDatabase database;
    private PacienteDao pacienteDao;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, PrevengosDatabase.class)
                .allowMainThreadQueries()
                .build();
        pacienteDao = database.pacienteDao();
    }

    @After
    public void tearDown() throws IOException {
        database.close();
    }

    @Test
    public void upsert_y_consultaDevuelveDatos() {
        PacienteEntity entity = new PacienteEntity(
                "pac-1",
                "12345678A",
                "Ana",
                "García",
                null,
                null,
                "600000000",
                "ana@example.com",
                null,
                null,
                null,
                null,
                null,
                System.currentTimeMillis(),
                null,
                true);
        pacienteDao.upsert(entity);

        List<PacienteEntity> resultados = pacienteDao.dirtyPacientes();
        assertEquals(1, resultados.size());
        assertTrue(resultados.get(0).isDirty());
    }

    @Test
    public void markAsClean_actualizaFlag() {
        PacienteEntity entity = new PacienteEntity(
                "pac-2",
                "12345678Z",
                "Luis",
                "López",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                System.currentTimeMillis(),
                null,
                true);
        pacienteDao.upsert(entity);
        pacienteDao.markAsClean("pac-2");

        PacienteEntity actualizado = pacienteDao.findById("pac-2");
        assertFalse(actualizado.isDirty());
    }
}
