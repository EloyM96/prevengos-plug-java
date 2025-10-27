package com.prevengos.plug.app.usecase;

import com.prevengos.plug.app.dto.ProgramarNotificacionCommand;
import com.prevengos.plug.domain.events.DomainEventPublisher;
import com.prevengos.plug.domain.events.NotificacionProgramadaEvent;
import com.prevengos.plug.domain.model.Notificacion;
import com.prevengos.plug.domain.ports.NotificacionPort;

import java.util.Objects;

public class ProgramarNotificacionUseCase {
    private static final String SOURCE = "app-service";

    private final NotificacionPort notificacionPort;
    private final DomainEventPublisher eventPublisher;

    public ProgramarNotificacionUseCase(NotificacionPort notificacionPort, DomainEventPublisher eventPublisher) {
        this.notificacionPort = Objects.requireNonNull(notificacionPort, "notificacionPort");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher");
    }

    public Notificacion execute(ProgramarNotificacionCommand command) {
        Notificacion notificacion = command.toNotificacion();
        notificacionPort.programar(notificacion);
        eventPublisher.publish(new NotificacionProgramadaEvent(SOURCE, notificacion));
        return notificacion;
    }
}
