package com.prevengos.plug.android.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.prevengos.plug.android.data.local.dao.CuestionarioDao
import com.prevengos.plug.android.data.local.dao.PacientesDao
import com.prevengos.plug.android.data.local.entity.CuestionarioEntity
import com.prevengos.plug.android.data.local.entity.PacienteEntity

@Database(
    entities = [PacienteEntity::class, CuestionarioEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PrevengosDatabase : RoomDatabase() {
    abstract fun pacientesDao(): PacientesDao
    abstract fun cuestionarioDao(): CuestionarioDao

    companion object {
        @Volatile
        private var INSTANCE: PrevengosDatabase? = null

        fun instance(context: Context): PrevengosDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                PrevengosDatabase::class.java,
                "prevengos.db"
            ).build().also { INSTANCE = it }
        }
    }
}
