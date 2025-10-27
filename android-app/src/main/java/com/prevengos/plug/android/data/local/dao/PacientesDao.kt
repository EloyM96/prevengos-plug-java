package com.prevengos.plug.android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.prevengos.plug.android.data.local.entity.PacienteEntity

@Dao
interface PacientesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(pacientes: List<PacienteEntity>)

    @Query("SELECT * FROM pacientes WHERE actualizadoEn > :timestamp")
    suspend fun updatedSince(timestamp: Long): List<PacienteEntity>
}
