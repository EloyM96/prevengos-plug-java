package com.prevengos.plug.android.data.remote.model;

import com.squareup.moshi.Json;

import java.util.List;

public class CuestionarioPayload {
    @Json(name = "cuestionario_id")
    private final String cuestionarioId;

    @Json(name = "paciente_id")
    private final String pacienteId;

    @Json(name = "plantilla_codigo")
    private final String plantillaCodigo;

    private final String estado;

    private final List<RespuestaPayload> respuestas;

    private final List<String> firmas;

    private final List<String> adjuntos;

    @Json(name = "created_at")
    private final String createdAt;

    @Json(name = "updated_at")
    private final String updatedAt;

    @Json(name = "last_modified")
    private final long lastModified;

    @Json(name = "sync_token")
    private final String syncToken;

    public CuestionarioPayload(
            String cuestionarioId,
            String pacienteId,
            String plantillaCodigo,
            String estado,
            List<RespuestaPayload> respuestas,
            List<String> firmas,
            List<String> adjuntos,
            String createdAt,
            String updatedAt,
            long lastModified,
            String syncToken
    ) {
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
    }

    public String getCuestionarioId() {
        return cuestionarioId;
    }

    public String getPacienteId() {
        return pacienteId;
    }

    public String getPlantillaCodigo() {
        return plantillaCodigo;
    }

    public String getEstado() {
        return estado;
    }

    public List<RespuestaPayload> getRespuestas() {
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
}
