package com.prevengos.plug.domain.model;

import java.util.Objects;
import java.util.UUID;

public record NotificacionId(UUID value) {
    public NotificacionId {
        Objects.requireNonNull(value, "value");
    }
}
