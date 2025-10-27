package com.prevengos.plug.android.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.prevengos.plug.android.data.local.room.JsonConverters;

import java.util.List;

@Entity(tableName = "cuestionarios")
@TypeConverters(JsonConverters.class)
public class CuestionarioEntity {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "cuestionario_id")
    private final String cuestionarioId;

    @NonNull
    @ColumnInfo(name = "paciente_id")
    private final String pacienteId;

    @NonNull
    @ColumnInfo(name = "plantilla_codigo")
    private final String plantillaCodigo;

    @NonNull
    private final String estado;

    private final List<RespuestaLocal> respuestas;
    private final List<String> firmas;
    private final List<String> adjuntos;

    @ColumnInfo(name = "created_at")
    private final String createdAt;

    @ColumnInfo(name = "updated_at")
    private final String updatedAt;

    @ColumnInfo(name = "last_modified")
    private final long lastModified;

    @ColumnInfo(name = "sync_token")
    private final String syncToken;

    @ColumnInfo(name = "is_dirty")
    private final boolean dirty;

    public CuestionarioEntity(@NonNull String cuestionarioId,
                               @NonNull String pacienteId,
                               @NonNull String plantillaCodigo,
                               @NonNull String estado,
                               List<RespuestaLocal> respuestas,
                               List<String> firmas,
                               List<String> adjuntos,
                               String createdAt,
                               String updatedAt,
                               long lastModified,
                               String syncToken,
                               boolean dirty) {
        this.cuestionarioId = cuestionarioId;
        this.pacienteId = pacienteId;
        this.plantillaCodigo = plantillaCodigo;
        this.estado = estado;
        this.respuestas = respuestas;
        this.firmas = firmas;
        this.adjuntos = adjuntos;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastModified = lastModified;
        this.syncToken = syncToken;
        this.dirty = dirty;
    }

    @NonNull
    public String getCuestionarioId() {
        return cuestionarioId;
    }

    @NonNull
    public String getPacienteId() {
        return pacienteId;
    }

    @NonNull
    public String getPlantillaCodigo() {
        return plantillaCodigo;
    }

    @NonNull
    public String getEstado() {
        return estado;
    }

    public List<RespuestaLocal> getRespuestas() {
        return respuestas;
    }

    public List<String> getFirmas() {
        return firmas;
    }

    public List<String> getAdjuntos() {
        return adjuntos;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public long getLastModified() {
        return lastModified;
    }

    public String getSyncToken() {
        return syncToken;
    }

    public boolean isDirty() {
        return dirty;
    }
}
