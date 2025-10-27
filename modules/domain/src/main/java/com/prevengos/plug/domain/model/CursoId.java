package com.prevengos.plug.domain.model;

import java.util.Objects;
import java.util.UUID;

public record CursoId(UUID value) {
    public CursoId {
        Objects.requireNonNull(value, "value");
    }
}
