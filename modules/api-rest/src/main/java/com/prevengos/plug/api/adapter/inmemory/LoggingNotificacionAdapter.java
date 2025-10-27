package com.prevengos.plug.api.adapter.inmemory;

import com.prevengos.plug.domain.model.Notificacion;
import com.prevengos.plug.domain.ports.NotificacionPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingNotificacionAdapter implements NotificacionPort {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingNotificacionAdapter.class);

    @Override
    public void programar(Notificacion notificacion) {
        LOGGER.info("[Notificacion] Programando {} hacia {}", notificacion.canal(), notificacion.destino());
    }
}
