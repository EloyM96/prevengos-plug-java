package com.prevengos.plug.android.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CuestionarioPayload(
    @Json(name = "cuestionario_id")
    val cuestionarioId: String,
    @Json(name = "paciente_id")
    val pacienteId: String,
    @Json(name = "plantilla_codigo")
    val plantillaCodigo: String,
    val estado: String,
    val respuestas: List<RespuestaPayload>,
    val firmas: List<String>,
    val adjuntos: List<String>,
    @Json(name = "created_at")
    val createdAt: String?,
    @Json(name = "updated_at")
    val updatedAt: String?,
    @Json(name = "last_modified")
    val lastModified: Long,
    @Json(name = "sync_token")
    val syncToken: String?
)
