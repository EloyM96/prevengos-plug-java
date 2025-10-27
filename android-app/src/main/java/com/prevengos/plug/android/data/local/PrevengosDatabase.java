package com.prevengos.plug.android.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.prevengos.plug.android.data.local.dao.CuestionarioDao;
import com.prevengos.plug.android.data.local.dao.PacientesDao;
import com.prevengos.plug.android.data.local.entity.CuestionarioEntity;
import com.prevengos.plug.android.data.local.entity.PacienteEntity;

@Database(
        entities = {PacienteEntity.class, CuestionarioEntity.class},
        version = 1,
        exportSchema = false
)
public abstract class PrevengosDatabase extends RoomDatabase {
    private static volatile PrevengosDatabase INSTANCE;

    public abstract PacientesDao pacientesDao();

    public abstract CuestionarioDao cuestionarioDao();

    public static PrevengosDatabase instance(Context context) {
        PrevengosDatabase result = INSTANCE;
        if (result == null) {
            synchronized (PrevengosDatabase.class) {
                result = INSTANCE;
                if (result == null) {
                    result = Room.databaseBuilder(
                            context.getApplicationContext(),
                            PrevengosDatabase.class,
                            "prevengos.db"
                    ).build();
                    INSTANCE = result;
                }
            }
        }
        return result;
    }
}
