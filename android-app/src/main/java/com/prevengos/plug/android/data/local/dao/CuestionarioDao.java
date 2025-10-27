package com.prevengos.plug.android.data.local.dao;

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

    @Query("SELECT * FROM cuestionarios WHERE completadoEn IS NULL")
    List<CuestionarioEntity> pendientes();

    @Query("SELECT * FROM cuestionarios WHERE actualizadoEn > :timestamp")
    List<CuestionarioEntity> updatedSince(long timestamp);
}
