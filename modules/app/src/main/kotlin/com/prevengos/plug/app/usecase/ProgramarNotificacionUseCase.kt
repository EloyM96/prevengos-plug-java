package com.prevengos.plug.app.usecase

import com.prevengos.plug.app.dto.ProgramarNotificacionCommand
import com.prevengos.plug.domain.events.DomainEventPublisher
import com.prevengos.plug.domain.events.NotificacionProgramadaEvent
import com.prevengos.plug.domain.model.Notificacion
import com.prevengos.plug.domain.ports.NotificacionPort

class ProgramarNotificacionUseCase(
    private val notificacionPort: NotificacionPort,
    private val eventPublisher: DomainEventPublisher
) {
    fun execute(command: ProgramarNotificacionCommand): Notificacion {
        val notificacion = command.toNotificacion()
        notificacionPort.programar(notificacion)
        eventPublisher.publish(
            NotificacionProgramadaEvent(
                source = "app-service",
                notificacion = notificacion
            )
        )
        return notificacion
    }
}
