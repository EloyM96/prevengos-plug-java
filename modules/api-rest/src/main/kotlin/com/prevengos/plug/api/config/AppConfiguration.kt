package com.prevengos.plug.api.config

import com.prevengos.plug.api.adapter.inmemory.InMemoryCitasAdapter
import com.prevengos.plug.api.adapter.inmemory.InMemoryCuestionariosAdapter
import com.prevengos.plug.api.adapter.inmemory.InMemoryPacientesAdapter
import com.prevengos.plug.api.adapter.inmemory.InMemoryResultadosAdapter
import com.prevengos.plug.api.adapter.inmemory.LoggingMoodleAdapter
import com.prevengos.plug.api.adapter.inmemory.LoggingNotificacionAdapter
import com.prevengos.plug.api.adapter.inmemory.LoggingPrevengosAdapter
import com.prevengos.plug.api.adapter.inmemory.LoggingRRHHAdapter
import com.prevengos.plug.app.usecase.ExportarRRHHUseCase
import com.prevengos.plug.app.usecase.GuardarCuestionarioUseCase
import com.prevengos.plug.app.usecase.ProgramarCitaUseCase
import com.prevengos.plug.app.usecase.ProgramarNotificacionUseCase
import com.prevengos.plug.app.usecase.RegistrarPacienteUseCase
import com.prevengos.plug.app.usecase.RegistrarResultadoUseCase
import com.prevengos.plug.app.usecase.SincronizarCursosUseCase
import com.prevengos.plug.domain.events.DomainEvent
import com.prevengos.plug.domain.events.DomainEventPublisher
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfiguration {
    private val logger = LoggerFactory.getLogger(AppConfiguration::class.java)

    @Bean
    fun domainEventPublisher(): DomainEventPublisher = DomainEventPublisher { event: DomainEvent ->
        logger.info("Evento de dominio publicado: {}", event)
    }

    @Bean
    fun pacientesPort() = InMemoryPacientesAdapter()

    @Bean
    fun cuestionariosPort() = InMemoryCuestionariosAdapter()

    @Bean
    fun citasPort() = InMemoryCitasAdapter()

    @Bean
    fun resultadosPort() = InMemoryResultadosAdapter()

    @Bean
    fun rrhhPort() = LoggingRRHHAdapter()

    @Bean
    fun notificacionPort() = LoggingNotificacionAdapter()

    @Bean
    fun moodlePort() = LoggingMoodleAdapter()

    @Bean
    fun prevengosPort() = LoggingPrevengosAdapter()

    @Bean
    fun registrarPacienteUseCase() = RegistrarPacienteUseCase(pacientesPort(), domainEventPublisher())

    @Bean
    fun guardarCuestionarioUseCase() = GuardarCuestionarioUseCase(cuestionariosPort(), domainEventPublisher())

    @Bean
    fun programarCitaUseCase() = ProgramarCitaUseCase(citasPort(), domainEventPublisher())

    @Bean
    fun registrarResultadoUseCase() = RegistrarResultadoUseCase(resultadosPort(), domainEventPublisher())

    @Bean
    fun programarNotificacionUseCase() = ProgramarNotificacionUseCase(notificacionPort(), domainEventPublisher())

    @Bean
    fun sincronizarCursosUseCase() = SincronizarCursosUseCase(moodlePort())

    @Bean
    fun exportarRrhhUseCase() = ExportarRRHHUseCase(rrhhPort())
}
