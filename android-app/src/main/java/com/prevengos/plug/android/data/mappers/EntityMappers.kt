package com.prevengos.plug.android.data.mappers

import com.prevengos.plug.android.data.local.entity.CuestionarioEntity
import com.prevengos.plug.android.data.local.entity.PacienteEntity
import com.prevengos.plug.android.data.local.entity.RespuestaLocal
import com.prevengos.plug.android.data.remote.model.CuestionarioPayload
import com.prevengos.plug.android.data.remote.model.PacientePayload
import com.prevengos.plug.android.data.remote.model.RespuestaPayload

fun PacienteEntity.toPayload(): PacientePayload = PacientePayload(
    pacienteId = pacienteId,
    nif = nif,
    nombre = nombre,
    apellidos = apellidos,
    fechaNacimiento = fechaNacimiento,
    sexo = sexo,
    telefono = telefono,
    email = email,
    empresaId = empresaId,
    centroId = centroId,
    externoRef = externoRef,
    createdAt = createdAt,
    updatedAt = updatedAt,
    lastModified = lastModified,
    syncToken = syncToken
)

fun PacientePayload.toEntity(isDirty: Boolean = false): PacienteEntity = PacienteEntity(
    pacienteId = pacienteId,
    nif = nif,
    nombre = nombre,
    apellidos = apellidos,
    fechaNacimiento = fechaNacimiento,
    sexo = sexo,
    telefono = telefono,
    email = email,
    empresaId = empresaId,
    centroId = centroId,
    externoRef = externoRef,
    createdAt = createdAt,
    updatedAt = updatedAt,
    lastModified = lastModified,
    syncToken = syncToken,
    isDirty = isDirty
)

fun CuestionarioEntity.toPayload(): CuestionarioPayload = CuestionarioPayload(
    cuestionarioId = cuestionarioId,
    pacienteId = pacienteId,
    plantillaCodigo = plantillaCodigo,
    estado = estado,
    respuestas = respuestas.map { it.toPayload() },
    firmas = firmas,
    adjuntos = adjuntos,
    createdAt = createdAt,
    updatedAt = updatedAt,
    lastModified = lastModified,
    syncToken = syncToken
)

fun CuestionarioPayload.toEntity(isDirty: Boolean = false): CuestionarioEntity = CuestionarioEntity(
    cuestionarioId = cuestionarioId,
    pacienteId = pacienteId,
    plantillaCodigo = plantillaCodigo,
    estado = estado,
    respuestas = respuestas.map { it.toLocal() },
    firmas = firmas,
    adjuntos = adjuntos,
    createdAt = createdAt,
    updatedAt = updatedAt,
    lastModified = lastModified,
    syncToken = syncToken,
    isDirty = isDirty
)

fun RespuestaLocal.toPayload(): RespuestaPayload = RespuestaPayload(
    preguntaCodigo = preguntaCodigo,
    valor = valor,
    unidad = unidad,
    metadata = metadata
)

fun RespuestaPayload.toLocal(): RespuestaLocal = RespuestaLocal(
    preguntaCodigo = preguntaCodigo,
    valor = valor,
    unidad = unidad,
    metadata = metadata
)
