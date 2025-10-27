package com.prevengos.plug.hubbackend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "cuestionarios")
public class Cuestionario {

    @Id
    @Column(name = "cuestionario_id", nullable = false, updatable = false)
    private UUID cuestionarioId;

    @Column(name = "paciente_id", nullable = false)
    private UUID pacienteId;

    @Column(name = "plantilla_codigo", nullable = false)
    private String plantillaCodigo;

    @Column(name = "estado", nullable = false)
    private String estado;

    @Lob
    @Column(name = "respuestas", nullable = false, columnDefinition = "jsonb")
    private String respuestas;

    @Lob
    @Column(name = "firmas", columnDefinition = "jsonb")
    private String firmas;

    @Lob
    @Column(name = "adjuntos", columnDefinition = "jsonb")
    private String adjuntos;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "last_modified", nullable = false)
    private OffsetDateTime lastModified;

    @Column(name = "sync_token", nullable = false)
    private long syncToken;

    protected Cuestionario() {
        // JPA
    }

    public Cuestionario(UUID cuestionarioId) {
        this.cuestionarioId = cuestionarioId;
    }

    public UUID getCuestionarioId() {
        return cuestionarioId;
    }

    public UUID getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(UUID pacienteId) {
        this.pacienteId = pacienteId;
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
}
