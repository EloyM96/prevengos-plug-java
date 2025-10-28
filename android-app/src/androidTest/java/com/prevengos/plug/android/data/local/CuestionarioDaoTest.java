package com.prevengos.plug.android.data.local;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.prevengos.plug.android.data.local.dao.CuestionarioDao;
import com.prevengos.plug.android.data.local.entity.CuestionarioEntity;
import com.prevengos.plug.android.data.local.entity.RespuestaLocal;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class CuestionarioDaoTest {
    private PrevengosDatabase database;
    private CuestionarioDao cuestionarioDao;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, PrevengosDatabase.class)
                .allowMainThreadQueries()
                .build();
        cuestionarioDao = database.cuestionarioDao();
    }

    @After
    public void tearDown() throws IOException {
        database.close();
    }

    @Test
    public void upsert_y_observePorPacienteDevuelveDatos() {
        CuestionarioEntity entity = new CuestionarioEntity(
                "cues-1",
                "pac-1",
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
        cuestionarioDao.upsert(entity);

        List<CuestionarioEntity> resultados = cuestionarioDao.dirtyCuestionarios();
        assertEquals(1, resultados.size());
        assertTrue(resultados.get(0).isDirty());
    }

    @Test
    public void markAsClean_eliminaBanderaSuciedad() {
        CuestionarioEntity entity = new CuestionarioEntity(
                "cues-2",
                "pac-1",
                "anamnesis",
                "borrador",
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                null,
                null,
                System.currentTimeMillis(),
                null,
                true);
        cuestionarioDao.upsert(entity);
        cuestionarioDao.markAsClean("cues-2");

        CuestionarioEntity actualizado = cuestionarioDao.findById("cues-2");
        assertFalse(actualizado.isDirty());
    }
}
