package com.prevengos.plug.app.usecase;

import com.prevengos.plug.app.dto.GuardarCuestionarioCommand;
import com.prevengos.plug.domain.events.CuestionarioCompletadoEvent;
import com.prevengos.plug.domain.events.DomainEventPublisher;
import com.prevengos.plug.domain.model.Cuestionario;
import com.prevengos.plug.domain.ports.CuestionariosPort;

import java.util.Objects;

public class GuardarCuestionarioUseCase {
    private final CuestionariosPort cuestionariosPort;
    private final DomainEventPublisher eventPublisher;

    public GuardarCuestionarioUseCase(CuestionariosPort cuestionariosPort, DomainEventPublisher eventPublisher) {
        this.cuestionariosPort = Objects.requireNonNull(cuestionariosPort, "cuestionariosPort");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher");
    }

    public Cuestionario execute(GuardarCuestionarioCommand command) {
        Cuestionario cuestionario = cuestionariosPort.guardar(command.toCuestionario());
        if (cuestionario.completadoEn() != null) {
            eventPublisher.publish(new CuestionarioCompletadoEvent(command.fuente(), cuestionario));
        }
        return cuestionario;
    }
}
