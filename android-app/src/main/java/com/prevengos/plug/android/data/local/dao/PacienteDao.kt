package com.prevengos.plug.android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.prevengos.plug.android.data.local.entity.PacienteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PacienteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(paciente: PacienteEntity)

    @Query("SELECT * FROM pacientes ORDER BY last_modified DESC")
    fun observePacientes(): Flow<List<PacienteEntity>>

    @Query("SELECT * FROM pacientes WHERE paciente_id = :id")
    suspend fun findById(id: String): PacienteEntity?

    @Query("SELECT * FROM pacientes WHERE is_dirty = 1")
    suspend fun dirtyPacientes(): List<PacienteEntity>

    @Query(
        "UPDATE pacientes SET is_dirty = 0, last_modified = :lastModified, sync_token = :syncToken WHERE paciente_id = :id"
    )
    suspend fun markAsSynced(id: String, lastModified: Long, syncToken: String?)
}
