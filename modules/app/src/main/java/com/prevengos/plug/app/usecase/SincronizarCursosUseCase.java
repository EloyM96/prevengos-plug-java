package com.prevengos.plug.app.usecase;

import com.prevengos.plug.app.dto.SincronizarCursosCommand;
import com.prevengos.plug.domain.ports.MoodlePort;

import java.util.Objects;

public class SincronizarCursosUseCase {
    private final MoodlePort moodlePort;

    public SincronizarCursosUseCase(MoodlePort moodlePort) {
        this.moodlePort = Objects.requireNonNull(moodlePort, "moodlePort");
    }

    public void execute(SincronizarCursosCommand command) {
        moodlePort.sincronizarCursos(command.toCursos());
    }
}
