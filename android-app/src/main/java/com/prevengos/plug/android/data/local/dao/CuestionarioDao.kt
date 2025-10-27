package com.prevengos.plug.android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.prevengos.plug.android.data.local.entity.CuestionarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CuestionarioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(cuestionario: CuestionarioEntity)

    @Query("SELECT * FROM cuestionarios WHERE paciente_id = :pacienteId")
    fun observeByPaciente(pacienteId: String): Flow<List<CuestionarioEntity>>

    @Query("SELECT * FROM cuestionarios WHERE cuestionario_id = :id")
    suspend fun findById(id: String): CuestionarioEntity?

    @Query("SELECT * FROM cuestionarios WHERE is_dirty = 1")
    suspend fun dirtyCuestionarios(): List<CuestionarioEntity>

    @Query(
        "UPDATE cuestionarios SET is_dirty = 0, last_modified = :lastModified, sync_token = :syncToken WHERE cuestionario_id = :id"
    )
    suspend fun markAsSynced(id: String, lastModified: Long, syncToken: String?)
}
