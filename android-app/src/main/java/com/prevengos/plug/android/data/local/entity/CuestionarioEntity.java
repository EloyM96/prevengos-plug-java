package com.prevengos.plug.android.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cuestionarios")
public class CuestionarioEntity {
    @PrimaryKey
    @NonNull
    public final String cuestionarioId;
    @NonNull
    public final String pacienteId;
    @NonNull
    public final String plantillaCodigo;
    @NonNull
    public final String estado;
    @NonNull
    public final String respuestasJson;
    public final Long completadoEn;
    public final long actualizadoEn;

    public CuestionarioEntity(@NonNull String cuestionarioId,
                              @NonNull String pacienteId,
                              @NonNull String plantillaCodigo,
                              @NonNull String estado,
                              @NonNull String respuestasJson,
                              Long completadoEn,
                              long actualizadoEn) {
        this.cuestionarioId = cuestionarioId;
        this.pacienteId = pacienteId;
        this.plantillaCodigo = plantillaCodigo;
        this.estado = estado;
        this.respuestasJson = respuestasJson;
        this.completadoEn = completadoEn;
        this.actualizadoEn = actualizadoEn;
    }
}
