package com.prevengos.plug.shared.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "pacientes")
public class PacienteEntity {

    @Id
    @Column(name = "paciente_id", nullable = false)
    private UUID pacienteId;

    @Column(name = "nif", nullable = false, length = 16)
    private String nif;

    @Column(name = "nombre", nullable = false, length = 160)
    private String nombre;

    @Column(name = "apellidos", nullable = false, length = 160)
    private String apellidos;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "sexo", nullable = false, length = 1)
    private String sexo;

    @Column(name = "telefono", length = 32)
    private String telefono;

    @Column(name = "email", length = 160)
    private String email;

    @Column(name = "empresa_id")
    private UUID empresaId;

    @Column(name = "centro_id")
    private UUID centroId;

    @Column(name = "externo_ref", length = 128)
    private String externoRef;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "last_modified", nullable = false)
    private OffsetDateTime lastModified;

    @Column(name = "sync_token", nullable = false)
    private long syncToken;

    protected PacienteEntity() {
        // JPA only
    }

    public PacienteEntity(UUID pacienteId,
                          String nif,
                          String nombre,
                          String apellidos,
                          LocalDate fechaNacimiento,
                          String sexo,
                          String telefono,
                          String email,
                          UUID empresaId,
                          UUID centroId,
                          String externoRef,
                          OffsetDateTime createdAt,
                          OffsetDateTime updatedAt,
                          OffsetDateTime lastModified,
                          long syncToken) {
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

    public UUID getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(UUID pacienteId) {
        this.pacienteId = pacienteId;
    }

    public String getNif() {
        return nif;
    }

    public void setNif(String nif) {
        this.nif = nif;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UUID getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(UUID empresaId) {
        this.empresaId = empresaId;
    }

    public UUID getCentroId() {
        return centroId;
    }

    public void setCentroId(UUID centroId) {
        this.centroId = centroId;
    }

    public String getExternoRef() {
        return externoRef;
    }

    public void setExternoRef(String externoRef) {
        this.externoRef = externoRef;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public OffsetDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(OffsetDateTime lastModified) {
        this.lastModified = lastModified;
    }

    public long getSyncToken() {
        return syncToken;
    }

    public void setSyncToken(long syncToken) {
        this.syncToken = syncToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PacienteEntity that)) {
            return false;
        }
        return Objects.equals(pacienteId, that.pacienteId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pacienteId);
    }

    @Override
    public String toString() {
        return "PacienteEntity{" +
                "pacienteId=" + pacienteId +
                ", nif='" + nif + '\'' +
                ", nombre='" + nombre + '\'' +
                ", apellidos='" + apellidos + '\'' +
                ", fechaNacimiento=" + fechaNacimiento +
                ", sexo='" + sexo + '\'' +
                ", telefono='" + telefono + '\'' +
                ", email='" + email + '\'' +
                ", empresaId=" + empresaId +
                ", centroId=" + centroId +
                ", externoRef='" + externoRef + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", lastModified=" + lastModified +
                ", syncToken=" + syncToken +
                '}';
    }
}
