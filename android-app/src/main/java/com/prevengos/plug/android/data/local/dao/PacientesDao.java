package com.prevengos.plug.android.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.prevengos.plug.android.data.local.entity.PacienteEntity;

import java.util.List;

@Dao
public interface PacientesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<PacienteEntity> pacientes);

    @Query("SELECT * FROM pacientes WHERE actualizadoEn > :timestamp")
    List<PacienteEntity> updatedSince(long timestamp);
}
