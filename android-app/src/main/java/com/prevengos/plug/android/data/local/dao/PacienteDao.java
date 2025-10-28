package com.prevengos.plug.android.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.prevengos.plug.android.data.local.entity.PacienteEntity;

import java.util.List;

@Dao
public interface PacienteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(PacienteEntity paciente);

    @Query("SELECT * FROM pacientes ORDER BY last_modified DESC")
    LiveData<List<PacienteEntity>> observePacientes();

    @Query("SELECT * FROM pacientes WHERE paciente_id = :id")
    PacienteEntity findById(String id);

    @Query("SELECT * FROM pacientes WHERE is_dirty = 1")
    List<PacienteEntity> dirtyPacientes();

    @Query("UPDATE pacientes SET is_dirty = 0 WHERE paciente_id = :id")
    void markAsClean(String id);

    @Query("DELETE FROM pacientes WHERE paciente_id = :id")
    void deleteById(String id);
}
