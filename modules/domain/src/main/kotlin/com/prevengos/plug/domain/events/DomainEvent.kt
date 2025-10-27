package com.prevengos.plug.domain.events

import com.prevengos.plug.domain.model.Cita
import com.prevengos.plug.domain.model.Cuestionario
import com.prevengos.plug.domain.model.Notificacion
import com.prevengos.plug.domain.model.Paciente
import com.prevengos.plug.domain.model.ResultadoAnalitico
import java.time.Instant
import java.util.UUID

interface DomainEvent {
    val eventId: UUID
    val occurredAt: Instant
    val version: Int
    val source: String
}

data class PacienteRegistradoEvent(
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now(),
    override val version: Int = 1,
    override val source: String,
    val paciente: Paciente
) : DomainEvent

data class CuestionarioCompletadoEvent(
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now(),
    override val version: Int = 1,
    override val source: String,
    val cuestionario: Cuestionario
) : DomainEvent

data class CitaProgramadaEvent(
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now(),
    override val version: Int = 1,
    override val source: String,
    val cita: Cita
) : DomainEvent

data class ResultadoRegistradoEvent(
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now(),
    override val version: Int = 1,
    override val source: String,
    val resultado: ResultadoAnalitico
) : DomainEvent

data class NotificacionProgramadaEvent(
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now(),
    override val version: Int = 1,
    override val source: String,
    val notificacion: Notificacion
) : DomainEvent

fun interface DomainEventPublisher {
    fun publish(event: DomainEvent)
}
