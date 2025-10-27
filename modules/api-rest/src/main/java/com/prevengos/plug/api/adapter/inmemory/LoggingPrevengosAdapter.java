package com.prevengos.plug.api.adapter.inmemory;

import com.prevengos.plug.domain.ports.PrevengosPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingPrevengosAdapter implements PrevengosPort {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingPrevengosAdapter.class);

    @Override
    public void publicarEventoDominio(Object payload) {
        LOGGER.info("[Prevengos] Publicando evento: {}", payload);
    }
}
