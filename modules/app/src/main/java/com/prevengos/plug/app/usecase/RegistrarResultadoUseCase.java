package com.prevengos.plug.app.usecase;

import com.prevengos.plug.app.dto.RegistrarResultadoCommand;
import com.prevengos.plug.domain.events.DomainEventPublisher;
import com.prevengos.plug.domain.events.ResultadoRegistradoEvent;
import com.prevengos.plug.domain.model.ResultadoAnalitico;
import com.prevengos.plug.domain.ports.ResultadosPort;

import java.util.Objects;

public class RegistrarResultadoUseCase {
    private final ResultadosPort resultadosPort;
    private final DomainEventPublisher eventPublisher;

    public RegistrarResultadoUseCase(ResultadosPort resultadosPort, DomainEventPublisher eventPublisher) {
        this.resultadosPort = Objects.requireNonNull(resultadosPort, "resultadosPort");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher");
    }

    public ResultadoAnalitico execute(RegistrarResultadoCommand command) {
        ResultadoAnalitico resultado = resultadosPort.registrar(command.toResultado());
        eventPublisher.publish(new ResultadoRegistradoEvent(command.fuente(), resultado));
        return resultado;
    }
}
