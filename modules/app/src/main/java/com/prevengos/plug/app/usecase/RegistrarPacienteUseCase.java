package com.prevengos.plug.app.usecase;

import com.prevengos.plug.app.dto.RegistrarPacienteCommand;
import com.prevengos.plug.domain.events.DomainEventPublisher;
import com.prevengos.plug.domain.events.PacienteRegistradoEvent;
import com.prevengos.plug.domain.model.Paciente;
import com.prevengos.plug.domain.ports.PacientesPort;

import java.util.Objects;

public class RegistrarPacienteUseCase {
    private final PacientesPort pacientesPort;
    private final DomainEventPublisher eventPublisher;

    public RegistrarPacienteUseCase(PacientesPort pacientesPort, DomainEventPublisher eventPublisher) {
        this.pacientesPort = Objects.requireNonNull(pacientesPort, "pacientesPort");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher");
    }

    public Paciente execute(RegistrarPacienteCommand command) {
        Paciente paciente = pacientesPort.registrar(command.toPaciente());
        eventPublisher.publish(new PacienteRegistradoEvent(command.fuente(), paciente));
        return paciente;
    }
}
