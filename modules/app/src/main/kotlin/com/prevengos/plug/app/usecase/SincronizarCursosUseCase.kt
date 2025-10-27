package com.prevengos.plug.app.usecase

import com.prevengos.plug.app.dto.SincronizarCursosCommand
import com.prevengos.plug.domain.ports.MoodlePort

class SincronizarCursosUseCase(
    private val moodlePort: MoodlePort
) {
    fun execute(command: SincronizarCursosCommand) {
        moodlePort.sincronizarCursos(command.toCursos())
    }
}
