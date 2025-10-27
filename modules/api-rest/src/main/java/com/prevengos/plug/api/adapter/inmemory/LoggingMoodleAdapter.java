package com.prevengos.plug.api.adapter.inmemory;

import com.prevengos.plug.domain.model.CursoMoodle;
import com.prevengos.plug.domain.ports.MoodlePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class LoggingMoodleAdapter implements MoodlePort {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingMoodleAdapter.class);

    @Override
    public void sincronizarCursos(List<CursoMoodle> cursos) {
        LOGGER.info("[Moodle] Sincronizando {} cursos", cursos.size());
    }
}
