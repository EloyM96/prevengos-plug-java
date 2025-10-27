package com.prevengos.plug.android.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pacientes")
public class PacienteEntity {
    @PrimaryKey
    @NonNull
    public final String pacienteId;
    @NonNull
    public final String nombre;
    @NonNull
    public final String apellidos;
    @NonNull
    public final String documentoIdentidad;
    public final String empresa;
    public final String centroTrabajo;
    public final long actualizadoEn;

    public PacienteEntity(@NonNull String pacienteId,
                          @NonNull String nombre,
                          @NonNull String apellidos,
                          @NonNull String documentoIdentidad,
                          String empresa,
                          String centroTrabajo,
                          long actualizadoEn) {
        this.pacienteId = pacienteId;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.documentoIdentidad = documentoIdentidad;
        this.empresa = empresa;
        this.centroTrabajo = centroTrabajo;
        this.actualizadoEn = actualizadoEn;
    }
}
