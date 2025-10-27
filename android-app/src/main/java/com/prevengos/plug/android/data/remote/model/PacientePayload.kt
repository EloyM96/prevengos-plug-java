package com.prevengos.plug.android.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PacientePayload(
    @Json(name = "paciente_id")
    val pacienteId: String,
    val nif: String,
    val nombre: String,
    val apellidos: String,
    @Json(name = "fecha_nacimiento")
    val fechaNacimiento: String?,
    val sexo: String?,
    val telefono: String?,
    val email: String?,
    @Json(name = "empresa_id")
    val empresaId: String?,
    @Json(name = "centro_id")
    val centroId: String?,
    @Json(name = "externo_ref")
    val externoRef: String?,
    @Json(name = "created_at")
    val createdAt: String?,
    @Json(name = "updated_at")
    val updatedAt: String?,
    @Json(name = "last_modified")
    val lastModified: Long,
    @Json(name = "sync_token")
    val syncToken: String?
)
