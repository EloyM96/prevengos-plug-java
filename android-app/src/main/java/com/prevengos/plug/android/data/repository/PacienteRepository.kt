package com.prevengos.plug.android.data.repository

import com.prevengos.plug.android.data.local.dao.PacienteDao
import com.prevengos.plug.android.data.local.entity.PacienteEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class PacienteRepository(private val pacienteDao: PacienteDao) {
    fun observePacientes(): Flow<List<PacienteEntity>> = pacienteDao.observePacientes()

    suspend fun createPaciente(
        nif: String,
        nombre: String,
        apellidos: String,
        telefono: String?,
        email: String?
    ): PacienteEntity {
        val now = System.currentTimeMillis()
        val paciente = PacienteEntity(
            pacienteId = UUID.randomUUID().toString(),
            nif = nif,
            nombre = nombre,
            apellidos = apellidos,
            fechaNacimiento = null,
            sexo = null,
            telefono = telefono,
            email = email,
            empresaId = null,
            centroId = null,
            externoRef = null,
            createdAt = null,
            updatedAt = null,
            lastModified = now,
            syncToken = null,
            isDirty = true
        )
        pacienteDao.upsert(paciente)
        return paciente
    }
}
