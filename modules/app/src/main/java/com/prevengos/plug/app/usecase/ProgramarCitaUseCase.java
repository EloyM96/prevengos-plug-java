package com.prevengos.plug.app.usecase;

import com.prevengos.plug.app.dto.ProgramarCitaCommand;
import com.prevengos.plug.domain.events.CitaProgramadaEvent;
import com.prevengos.plug.domain.events.DomainEventPublisher;
import com.prevengos.plug.domain.model.Cita;
import com.prevengos.plug.domain.ports.CitasPort;

import java.util.Objects;

public class ProgramarCitaUseCase {
    private final CitasPort citasPort;
    private final DomainEventPublisher eventPublisher;

    public ProgramarCitaUseCase(CitasPort citasPort, DomainEventPublisher eventPublisher) {
        this.citasPort = Objects.requireNonNull(citasPort, "citasPort");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher");
    }

    public Cita execute(ProgramarCitaCommand command) {
        Cita cita = citasPort.programar(command.toCita());
        eventPublisher.publish(new CitaProgramadaEvent(command.fuente(), cita));
        return cita;
    }
}
