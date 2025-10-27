package com.prevengos.plug.android.data.local;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.prevengos.plug.android.data.local.dao.SyncMetadataDao;
import com.prevengos.plug.android.data.local.entity.SyncMetadata;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class DatabaseInstrumentedTest {
    private PrevengosDatabase database;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, PrevengosDatabase.class)
                .allowMainThreadQueries()
                .build();
    }

    @After
    public void tearDown() {
        if (database != null) {
            database.close();
        }
    }

    @Test
    public void syncMetadataDaoGuardaUltimaSincronizacion() {
        SyncMetadataDao dao = database.syncMetadataDao();
        SyncMetadata metadata = new SyncMetadata("global", 123L, "abc");
        dao.upsert(metadata);

        SyncMetadata stored = dao.getMetadata("global");
        assertNotNull(stored);
        assertEquals(metadata.getResourceType(), stored.getResourceType());
        assertEquals(metadata.getLastSyncedAt(), stored.getLastSyncedAt());
        assertEquals(metadata.getSyncToken(), stored.getSyncToken());
    }
}
