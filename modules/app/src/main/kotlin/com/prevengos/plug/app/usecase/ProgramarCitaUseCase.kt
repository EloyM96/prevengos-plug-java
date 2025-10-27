package com.prevengos.plug.app.usecase

import com.prevengos.plug.app.dto.ProgramarCitaCommand
import com.prevengos.plug.domain.events.CitaProgramadaEvent
import com.prevengos.plug.domain.events.DomainEventPublisher
import com.prevengos.plug.domain.model.Cita
import com.prevengos.plug.domain.ports.CitasPort

class ProgramarCitaUseCase(
    private val citasPort: CitasPort,
    private val eventPublisher: DomainEventPublisher
) {
    fun execute(command: ProgramarCitaCommand): Cita {
        val cita = citasPort.programar(command.toCita())
        eventPublisher.publish(
            CitaProgramadaEvent(
                source = command.fuente,
                cita = cita
            )
        )
        return cita
    }
}
