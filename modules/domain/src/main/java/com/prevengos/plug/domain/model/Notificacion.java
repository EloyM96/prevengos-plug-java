package com.prevengos.plug.domain.model;

import java.util.Map;
import java.util.Objects;

public record Notificacion(
        NotificacionId id,
        CanalNotificacion canal,
        String destino,
        String plantilla,
        Map<String, Object> datos
) {
    public Notificacion {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(canal, "canal");
        Objects.requireNonNull(destino, "destino");
        Objects.requireNonNull(plantilla, "plantilla");
        Objects.requireNonNull(datos, "datos");
    }
}
