package com.prevengos.plug.domain.model;

import java.util.Objects;
import java.util.UUID;

public record CuestionarioId(UUID value) {
    public CuestionarioId {
        Objects.requireNonNull(value, "value");
    }
}
