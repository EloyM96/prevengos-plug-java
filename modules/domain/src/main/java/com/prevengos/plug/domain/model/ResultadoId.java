package com.prevengos.plug.domain.model;

import java.util.Objects;
import java.util.UUID;

public record ResultadoId(UUID value) {
    public ResultadoId {
        Objects.requireNonNull(value, "value");
    }
}
