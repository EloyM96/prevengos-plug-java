package com.prevengos.plug.api.adapter.inmemory;

import com.prevengos.plug.domain.ports.RRHHPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class LoggingRRHHAdapter implements RRHHPort {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingRRHHAdapter.class);

    @Override
    public void exportarPlantillaTrabajadores(Instant desde) {
        LOGGER.info("[RRHH] Exportando plantilla desde {}", desde);
    }
}
