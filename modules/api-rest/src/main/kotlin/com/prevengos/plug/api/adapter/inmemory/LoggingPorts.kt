package com.prevengos.plug.api.adapter.inmemory

import com.prevengos.plug.domain.model.CursoMoodle
import com.prevengos.plug.domain.model.Notificacion
import com.prevengos.plug.domain.ports.MoodlePort
import com.prevengos.plug.domain.ports.NotificacionPort
import com.prevengos.plug.domain.ports.PrevengosPort
import com.prevengos.plug.domain.ports.RRHHPort
import org.slf4j.LoggerFactory
import java.time.Instant

class LoggingRRHHAdapter : RRHHPort {
    private val logger = LoggerFactory.getLogger(LoggingRRHHAdapter::class.java)

    override fun exportarPlantillaTrabajadores(desde: Instant) {
        logger.info("[RRHH] Exportando plantilla desde {}", desde)
    }
}

class LoggingNotificacionAdapter : NotificacionPort {
    private val logger = LoggerFactory.getLogger(LoggingNotificacionAdapter::class.java)

    override fun programar(notificacion: Notificacion) {
        logger.info("[Notificacion] Programando {} hacia {}", notificacion.canal, notificacion.destino)
    }
}

class LoggingMoodleAdapter : MoodlePort {
    private val logger = LoggerFactory.getLogger(LoggingMoodleAdapter::class.java)

    override fun sincronizarCursos(cursos: List<CursoMoodle>) {
        logger.info("[Moodle] Sincronizando {} cursos", cursos.size)
    }
}

class LoggingPrevengosAdapter : PrevengosPort {
    private val logger = LoggerFactory.getLogger(LoggingPrevengosAdapter::class.java)

    override fun publicarEventoDominio(payload: Any) {
        logger.info("[Prevengos] Publicando evento: {}", payload)
    }
}
