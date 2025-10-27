package com.prevengos.plug.app.usecase

import com.prevengos.plug.domain.ports.RRHHPort
import java.time.Instant

class ExportarRRHHUseCase(
    private val rrhhPort: RRHHPort
) {
    fun execute(desde: Instant = Instant.now().minusSeconds(86_400)) {
        rrhhPort.exportarPlantillaTrabajadores(desde)
    }
}
