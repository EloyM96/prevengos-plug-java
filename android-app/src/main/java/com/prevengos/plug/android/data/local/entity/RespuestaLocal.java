package com.prevengos.plug.android.data.local.entity;

import com.squareup.moshi.Json;

import java.util.Map;

public class RespuestaLocal {
    @Json(name = "pregunta_codigo")
    private final String preguntaCodigo;
    @Json(name = "valor")
    private final String valor;
    @Json(name = "unidad")
    private final String unidad;
    @Json(name = "metadata")
    private final Map<String, String> metadata;

    public RespuestaLocal(String preguntaCodigo, String valor, String unidad, Map<String, String> metadata) {
        this.preguntaCodigo = preguntaCodigo;
        this.valor = valor;
        this.unidad = unidad;
        this.metadata = metadata;
    }

    public String getPreguntaCodigo() {
        return preguntaCodigo;
    }

    public String getValor() {
        return valor;
    }

    public String getUnidad() {
        return unidad;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }
}
