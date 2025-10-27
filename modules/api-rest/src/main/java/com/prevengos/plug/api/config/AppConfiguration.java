package com.prevengos.plug.api.config;

import com.prevengos.plug.api.adapter.inmemory.InMemoryCitasAdapter;
import com.prevengos.plug.api.adapter.inmemory.InMemoryCuestionariosAdapter;
import com.prevengos.plug.api.adapter.inmemory.InMemoryPacientesAdapter;
import com.prevengos.plug.api.adapter.inmemory.InMemoryResultadosAdapter;
import com.prevengos.plug.api.adapter.inmemory.LoggingMoodleAdapter;
import com.prevengos.plug.api.adapter.inmemory.LoggingNotificacionAdapter;
import com.prevengos.plug.api.adapter.inmemory.LoggingPrevengosAdapter;
import com.prevengos.plug.api.adapter.inmemory.LoggingRRHHAdapter;
import com.prevengos.plug.app.usecase.ExportarRRHHUseCase;
import com.prevengos.plug.app.usecase.GuardarCuestionarioUseCase;
import com.prevengos.plug.app.usecase.ProgramarCitaUseCase;
import com.prevengos.plug.app.usecase.ProgramarNotificacionUseCase;
import com.prevengos.plug.app.usecase.RegistrarPacienteUseCase;
import com.prevengos.plug.app.usecase.RegistrarResultadoUseCase;
import com.prevengos.plug.app.usecase.SincronizarCursosUseCase;
import com.prevengos.plug.domain.events.DomainEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfiguration.class);

    @Bean
    public DomainEventPublisher domainEventPublisher() {
        return event -> LOGGER.info("Evento de dominio publicado: {}", event);
    }

    @Bean
    public InMemoryPacientesAdapter pacientesPort() {
        return new InMemoryPacientesAdapter();
    }

    @Bean
    public InMemoryCuestionariosAdapter cuestionariosPort() {
        return new InMemoryCuestionariosAdapter();
    }

    @Bean
    public InMemoryCitasAdapter citasPort() {
        return new InMemoryCitasAdapter();
    }

    @Bean
    public InMemoryResultadosAdapter resultadosPort() {
        return new InMemoryResultadosAdapter();
    }

    @Bean
    public LoggingRRHHAdapter rrhhPort() {
        return new LoggingRRHHAdapter();
    }

    @Bean
    public LoggingNotificacionAdapter notificacionPort() {
        return new LoggingNotificacionAdapter();
    }

    @Bean
    public LoggingMoodleAdapter moodlePort() {
        return new LoggingMoodleAdapter();
    }

    @Bean
    public LoggingPrevengosAdapter prevengosPort() {
        return new LoggingPrevengosAdapter();
    }

    @Bean
    public RegistrarPacienteUseCase registrarPacienteUseCase() {
        return new RegistrarPacienteUseCase(pacientesPort(), domainEventPublisher());
    }

    @Bean
    public GuardarCuestionarioUseCase guardarCuestionarioUseCase() {
        return new GuardarCuestionarioUseCase(cuestionariosPort(), domainEventPublisher());
    }

    @Bean
    public ProgramarCitaUseCase programarCitaUseCase() {
        return new ProgramarCitaUseCase(citasPort(), domainEventPublisher());
    }

    @Bean
    public RegistrarResultadoUseCase registrarResultadoUseCase() {
        return new RegistrarResultadoUseCase(resultadosPort(), domainEventPublisher());
    }

    @Bean
    public ProgramarNotificacionUseCase programarNotificacionUseCase() {
        return new ProgramarNotificacionUseCase(notificacionPort(), domainEventPublisher());
    }

    @Bean
    public SincronizarCursosUseCase sincronizarCursosUseCase() {
        return new SincronizarCursosUseCase(moodlePort());
    }

    @Bean
    public ExportarRRHHUseCase exportarRrhhUseCase() {
        return new ExportarRRHHUseCase(rrhhPort());
    }
}
