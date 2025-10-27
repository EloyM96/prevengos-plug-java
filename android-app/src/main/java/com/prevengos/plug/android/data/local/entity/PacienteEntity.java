package com.prevengos.plug.android.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pacientes")
public class PacienteEntity {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "paciente_id")
    private final String pacienteId;

    @NonNull
    private final String nif;

    @NonNull
    private final String nombre;

    @NonNull
    private final String apellidos;

    @ColumnInfo(name = "fecha_nacimiento")
    private final String fechaNacimiento;

    private final String sexo;

    private final String telefono;

    private final String email;

    @ColumnInfo(name = "empresa_id")
    private final String empresaId;

    @ColumnInfo(name = "centro_id")
    private final String centroId;

    @ColumnInfo(name = "externo_ref")
    private final String externoRef;

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

    public PacienteEntity(@NonNull String pacienteId,
                          @NonNull String nif,
                          @NonNull String nombre,
                          @NonNull String apellidos,
                          String fechaNacimiento,
                          String sexo,
                          String telefono,
                          String email,
                          String empresaId,
                          String centroId,
                          String externoRef,
                          String createdAt,
                          String updatedAt,
                          long lastModified,
                          String syncToken,
                          boolean dirty) {
        this.pacienteId = pacienteId;
        this.nif = nif;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.fechaNacimiento = fechaNacimiento;
        this.sexo = sexo;
        this.telefono = telefono;
        this.email = email;
        this.empresaId = empresaId;
        this.centroId = centroId;
        this.externoRef = externoRef;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastModified = lastModified;
        this.syncToken = syncToken;
        this.dirty = dirty;
    }

    @NonNull
    public String getPacienteId() {
        return pacienteId;
    }

    @NonNull
    public String getNif() {
        return nif;
    }

    @NonNull
    public String getNombre() {
        return nombre;
    }

    @NonNull
    public String getApellidos() {
        return apellidos;
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public String getSexo() {
        return sexo;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getEmail() {
        return email;
    }

    public String getEmpresaId() {
        return empresaId;
    }

    public String getCentroId() {
        return centroId;
    }

    public String getExternoRef() {
        return externoRef;
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
