package com.prevengos.plug.android.data.remote.model;

import com.squareup.moshi.Json;

public class PacientePayload {
    @Json(name = "paciente_id")
    private final String pacienteId;
    private final String nif;
    private final String nombre;
    private final String apellidos;
    @Json(name = "fecha_nacimiento")
    private final String fechaNacimiento;
    private final String sexo;
    private final String telefono;
    private final String email;
    @Json(name = "empresa_id")
    private final String empresaId;
    @Json(name = "centro_id")
    private final String centroId;
    @Json(name = "externo_ref")
    private final String externoRef;
    @Json(name = "created_at")
    private final String createdAt;
    @Json(name = "updated_at")
    private final String updatedAt;
    @Json(name = "last_modified")
    private final long lastModified;
    @Json(name = "sync_token")
    private final String syncToken;

    public PacientePayload(
            String pacienteId,
            String nif,
            String nombre,
            String apellidos,
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
            String syncToken) {
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
    }

    public String getPacienteId() {
        return pacienteId;
    }

    public String getNif() {
        return nif;
    }

    public String getNombre() {
        return nombre;
    }

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
}
