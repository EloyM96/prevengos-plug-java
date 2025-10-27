package com.prevengos.plug.app.usecase

import com.prevengos.plug.app.dto.RegistrarResultadoCommand
import com.prevengos.plug.domain.events.DomainEventPublisher
import com.prevengos.plug.domain.events.ResultadoRegistradoEvent
import com.prevengos.plug.domain.model.ResultadoAnalitico
import com.prevengos.plug.domain.ports.ResultadosPort

class RegistrarResultadoUseCase(
    private val resultadosPort: ResultadosPort,
    private val eventPublisher: DomainEventPublisher
) {
    fun execute(command: RegistrarResultadoCommand): ResultadoAnalitico {
        val resultado = resultadosPort.registrar(command.toResultado())
        eventPublisher.publish(
            ResultadoRegistradoEvent(
                source = command.fuente,
                resultado = resultado
            )
        )
        return resultado
    }
}
