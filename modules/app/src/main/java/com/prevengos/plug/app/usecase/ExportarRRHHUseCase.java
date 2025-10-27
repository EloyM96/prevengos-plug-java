package com.prevengos.plug.app.usecase;

import com.prevengos.plug.domain.ports.RRHHPort;

import java.time.Instant;
import java.util.Objects;

public class ExportarRRHHUseCase {
    private static final long ONE_DAY_SECONDS = 86_400L;

    private final RRHHPort rrhhPort;

    public ExportarRRHHUseCase(RRHHPort rrhhPort) {
        this.rrhhPort = Objects.requireNonNull(rrhhPort, "rrhhPort");
    }

    public void execute() {
        execute(Instant.now().minusSeconds(ONE_DAY_SECONDS));
    }

    public void execute(Instant desde) {
        rrhhPort.exportarPlantillaTrabajadores(Objects.requireNonNull(desde, "desde"));
    }
}
