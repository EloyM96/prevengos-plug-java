package com.prevengos.plug.android.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RespuestaPayload(
    @Json(name = "pregunta_codigo")
    val preguntaCodigo: String,
    val valor: String?,
    val unidad: String? = null,
    val metadata: Map<String, String>? = null
)
