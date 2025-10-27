package com.prevengos.plug.domain.model;

import java.util.Objects;
import java.util.UUID;

public record CitaId(UUID value) {
    public CitaId {
        Objects.requireNonNull(value, "value");
    }
}
