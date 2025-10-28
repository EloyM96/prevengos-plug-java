package com.prevengos.plug.shared.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Lob;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "cuestionarios")
public class CuestionarioEntity {

    @Id
    @Column(name = "cuestionario_id", nullable = false)
    private UUID cuestionarioId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "paciente_id", nullable = false)
    private PacienteEntity paciente;

    @Column(name = "plantilla_codigo", nullable = false, length = 64)
    private String plantillaCodigo;

    @Column(name = "estado", nullable = false, length = 32)
    private String estado;

    @Lob
    @Column(name = "respuestas")
    private String respuestas;

    @Lob
    @Column(name = "firmas")
    private String firmas;

    @Lob
    @Column(name = "adjuntos")
    private String adjuntos;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "last_modified", nullable = false)
    private OffsetDateTime lastModified;

    @Column(name = "sync_token", nullable = false)
    private long syncToken;

    protected CuestionarioEntity() {
        // JPA only
    }

    public CuestionarioEntity(UUID cuestionarioId,
                               PacienteEntity paciente,
                               String plantillaCodigo,
                               String estado,
                               String respuestas,
                               String firmas,
                               String adjuntos,
                               OffsetDateTime createdAt,
                               OffsetDateTime updatedAt,
                               OffsetDateTime lastModified,
                               long syncToken) {
        this.cuestionarioId = cuestionarioId;
        this.paciente = paciente;
        this.plantillaCodigo = plantillaCodigo;
        this.estado = estado;
        this.respuestas = respuestas;
        this.firmas = firmas;
        this.adjuntos = adjuntos;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastModified = lastModified;
        this.syncToken = syncToken;
    }

    public UUID getCuestionarioId() {
        return cuestionarioId;
    }

    public void setCuestionarioId(UUID cuestionarioId) {
        this.cuestionarioId = cuestionarioId;
    }

    public PacienteEntity getPaciente() {
        return paciente;
    }

    public void setPaciente(PacienteEntity paciente) {
        this.paciente = paciente;
    }

    public String getPlantillaCodigo() {
        return plantillaCodigo;
    }

    public void setPlantillaCodigo(String plantillaCodigo) {
        this.plantillaCodigo = plantillaCodigo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getRespuestas() {
        return respuestas;
    }

    public void setRespuestas(String respuestas) {
        this.respuestas = respuestas;
    }

    public String getFirmas() {
        return firmas;
    }

    public void setFirmas(String firmas) {
        this.firmas = firmas;
    }

    public String getAdjuntos() {
        return adjuntos;
    }

    public void setAdjuntos(String adjuntos) {
        this.adjuntos = adjuntos;
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
        if (!(o instanceof CuestionarioEntity that)) {
            return false;
        }
        return Objects.equals(cuestionarioId, that.cuestionarioId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cuestionarioId);
    }

    @Override
    public String toString() {
        return "CuestionarioEntity{" +
                "cuestionarioId=" + cuestionarioId +
                ", paciente=" + (paciente != null ? paciente.getPacienteId() : null) +
                ", plantillaCodigo='" + plantillaCodigo + '\'' +
                ", estado='" + estado + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", lastModified=" + lastModified +
                ", syncToken=" + syncToken +
                '}';
    }
}
