package com.prevengos.plug.android.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.prevengos.plug.android.data.local.dao.CuestionarioDao
import com.prevengos.plug.android.data.local.dao.PacienteDao
import com.prevengos.plug.android.data.local.dao.SyncMetadataDao
import com.prevengos.plug.android.data.local.entity.CuestionarioEntity
import com.prevengos.plug.android.data.local.entity.PacienteEntity
import com.prevengos.plug.android.data.local.entity.SyncMetadata
import com.prevengos.plug.android.data.local.room.JsonConverters

@Database(
    entities = [PacienteEntity::class, CuestionarioEntity::class, SyncMetadata::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(JsonConverters::class)
abstract class PrevengosDatabase : RoomDatabase() {
    abstract fun pacienteDao(): PacienteDao
    abstract fun cuestionarioDao(): CuestionarioDao
    abstract fun syncMetadataDao(): SyncMetadataDao

    companion object {
        private const val DATABASE_NAME = "prevengos.db"

        @Volatile
        private var INSTANCE: PrevengosDatabase? = null

        fun instance(context: Context): PrevengosDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PrevengosDatabase::class.java,
                    DATABASE_NAME
                ).fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
