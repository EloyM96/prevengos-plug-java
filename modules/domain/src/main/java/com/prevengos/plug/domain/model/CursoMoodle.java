package com.prevengos.plug.domain.model;

import java.util.Objects;

public record CursoMoodle(
        CursoId id,
        String nombre,
        String codigo,
        String url
) {
    public CursoMoodle {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(nombre, "nombre");
        Objects.requireNonNull(codigo, "codigo");
        Objects.requireNonNull(url, "url");
    }
}
