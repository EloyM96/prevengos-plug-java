package com.prevengos.plug.app.usecase

import com.prevengos.plug.app.dto.GuardarCuestionarioCommand
import com.prevengos.plug.domain.events.CuestionarioCompletadoEvent
import com.prevengos.plug.domain.events.DomainEventPublisher
import com.prevengos.plug.domain.model.Cuestionario
import com.prevengos.plug.domain.ports.CuestionariosPort

class GuardarCuestionarioUseCase(
    private val cuestionariosPort: CuestionariosPort,
    private val eventPublisher: DomainEventPublisher
) {
    fun execute(command: GuardarCuestionarioCommand): Cuestionario {
        val cuestionario = cuestionariosPort.guardar(command.toCuestionario())
        if (cuestionario.completadoEn != null) {
            eventPublisher.publish(
                CuestionarioCompletadoEvent(
                    source = command.fuente,
                    cuestionario = cuestionario
                )
            )
        }
        return cuestionario
    }
}
