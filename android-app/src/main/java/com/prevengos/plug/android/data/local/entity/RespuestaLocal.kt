package com.prevengos.plug.android.data.local.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RespuestaLocal(
    @Json(name = "pregunta_codigo")
    val preguntaCodigo: String,
    @Json(name = "valor")
    val valor: String?,
    @Json(name = "unidad")
    val unidad: String? = null,
    @Json(name = "metadata")
    val metadata: Map<String, String>? = null
)
