package com.prevengos.plug.android.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.prevengos.plug.android.data.local.dao.CuestionarioDao;
import com.prevengos.plug.android.data.local.dao.PacienteDao;
import com.prevengos.plug.android.data.local.dao.SyncMetadataDao;
import com.prevengos.plug.android.data.local.entity.CuestionarioEntity;
import com.prevengos.plug.android.data.local.entity.PacienteEntity;
import com.prevengos.plug.android.data.local.entity.SyncMetadata;
import com.prevengos.plug.android.data.local.room.JsonConverters;

@Database(
        entities = {PacienteEntity.class, CuestionarioEntity.class, SyncMetadata.class},
        version = 1,
        exportSchema = true)
@TypeConverters(JsonConverters.class)
public abstract class PrevengosDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "prevengos.db";

    private static volatile PrevengosDatabase INSTANCE;

    public abstract PacienteDao pacienteDao();

    public abstract CuestionarioDao cuestionarioDao();

    public abstract SyncMetadataDao syncMetadataDao();

    public static PrevengosDatabase instance(Context context) {
        if (INSTANCE != null) {
            return INSTANCE;
        }
        synchronized (PrevengosDatabase.class) {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                                context.getApplicationContext(),
                                PrevengosDatabase.class,
                                DATABASE_NAME)
                        .fallbackToDestructiveMigration()
                        .build();
            }
            return INSTANCE;
        }
    }
}
