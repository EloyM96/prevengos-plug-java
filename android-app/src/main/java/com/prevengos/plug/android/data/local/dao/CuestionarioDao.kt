package com.prevengos.plug.android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.prevengos.plug.android.data.local.entity.CuestionarioEntity

@Dao
interface CuestionarioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(cuestionario: CuestionarioEntity)

    @Query("SELECT * FROM cuestionarios WHERE completadoEn IS NULL")
    suspend fun pendientes(): List<CuestionarioEntity>

    @Query("SELECT * FROM cuestionarios WHERE actualizadoEn > :timestamp")
    suspend fun updatedSince(timestamp: Long): List<CuestionarioEntity>
}
