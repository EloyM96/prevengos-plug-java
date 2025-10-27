package com.prevengos.plug.android.data.repository

import com.prevengos.plug.android.data.local.dao.CuestionarioDao
import com.prevengos.plug.android.data.local.entity.CuestionarioEntity
import com.prevengos.plug.android.data.local.entity.RespuestaLocal
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class CuestionarioRepository(private val cuestionarioDao: CuestionarioDao) {
    fun observeForPaciente(pacienteId: String): Flow<List<CuestionarioEntity>> =
        cuestionarioDao.observeByPaciente(pacienteId)

    suspend fun createDraft(pacienteId: String, plantillaCodigo: String, respuestas: List<RespuestaLocal>): CuestionarioEntity {
        val now = System.currentTimeMillis()
        val entity = CuestionarioEntity(
            cuestionarioId = UUID.randomUUID().toString(),
            pacienteId = pacienteId,
            plantillaCodigo = plantillaCodigo,
            estado = "borrador",
            respuestas = respuestas,
            firmas = emptyList(),
            adjuntos = emptyList(),
            createdAt = null,
            updatedAt = null,
            lastModified = now,
            syncToken = null,
            isDirty = true
        )
        cuestionarioDao.upsert(entity)
        return entity
    }
}
