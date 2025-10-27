package com.prevengos.plug.app.dto;

import com.prevengos.plug.domain.model.CanalNotificacion;
import com.prevengos.plug.domain.model.Notificacion;
import com.prevengos.plug.domain.model.NotificacionId;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record ProgramarNotificacionCommand(
        CanalNotificacion canal,
        String destino,
        String plantilla,
        Map<String, Object> datos
) {
    public ProgramarNotificacionCommand {
        Objects.requireNonNull(canal, "canal");
        Objects.requireNonNull(destino, "destino");
        Objects.requireNonNull(plantilla, "plantilla");
        Objects.requireNonNull(datos, "datos");
    }

    public Notificacion toNotificacion() {
        return new Notificacion(
                new NotificacionId(UUID.randomUUID()),
                canal,
                destino,
                plantilla,
                datos
        );
    }
}
