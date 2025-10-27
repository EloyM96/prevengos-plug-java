package com.prevengos.plug.app.usecase

import com.prevengos.plug.app.dto.RegistrarPacienteCommand
import com.prevengos.plug.domain.events.DomainEventPublisher
import com.prevengos.plug.domain.events.PacienteRegistradoEvent
import com.prevengos.plug.domain.model.Paciente
import com.prevengos.plug.domain.ports.PacientesPort

class RegistrarPacienteUseCase(
    private val pacientesPort: PacientesPort,
    private val eventPublisher: DomainEventPublisher
) {
    fun execute(command: RegistrarPacienteCommand): Paciente {
        val paciente = pacientesPort.registrar(command.toPaciente())
        eventPublisher.publish(
            PacienteRegistradoEvent(
                source = command.fuente,
                paciente = paciente
            )
        )
        return paciente
    }
}
