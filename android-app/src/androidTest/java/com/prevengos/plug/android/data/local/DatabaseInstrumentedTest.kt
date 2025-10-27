package com.prevengos.plug.android.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.prevengos.plug.android.data.local.entity.SyncMetadata
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseInstrumentedTest {
    private lateinit var database: PrevengosDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(context, PrevengosDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun syncMetadataDaoGuardaUltimaSincronizacion() = runBlocking {
        val dao = database.syncMetadataDao()
        val metadata = SyncMetadata(resourceType = "global", lastSyncedAt = 123L, syncToken = "abc")
        dao.upsert(metadata)

        val stored = dao.getMetadata("global")
        assertEquals(metadata, stored)
    }
}
