package com.prevengos.plug.android.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.prevengos.plug.android.data.local.entity.CuestionarioEntity;

import java.util.List;

@Dao
public interface CuestionarioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(CuestionarioEntity cuestionario);

    @Query("SELECT * FROM cuestionarios WHERE paciente_id = :pacienteId")
    LiveData<List<CuestionarioEntity>> observeByPaciente(String pacienteId);

    @Query("SELECT * FROM cuestionarios WHERE cuestionario_id = :id")
    CuestionarioEntity findById(String id);

    @Query("SELECT * FROM cuestionarios WHERE is_dirty = 1")
    List<CuestionarioEntity> dirtyCuestionarios();

    @Query("UPDATE cuestionarios SET is_dirty = 0 WHERE cuestionario_id = :id")
    void markAsClean(String id);

    @Query("DELETE FROM cuestionarios WHERE cuestionario_id = :id")
    void deleteById(String id);
}
