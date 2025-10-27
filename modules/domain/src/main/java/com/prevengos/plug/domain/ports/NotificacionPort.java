package com.prevengos.plug.domain.ports;

import com.prevengos.plug.domain.model.Notificacion;

public interface NotificacionPort {
    void programar(Notificacion notificacion);
}
