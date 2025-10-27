package com.prevengos.plug.domain.model;

import java.util.Objects;
import java.util.UUID;

public record PacienteId(UUID value) {
    public PacienteId {
        Objects.requireNonNull(value, "value");
    }
}
