package com.prevengos.plug.android.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pacientes")
data class PacienteEntity(
    @PrimaryKey val pacienteId: String,
    val nombre: String,
    val apellidos: String,
    val documentoIdentidad: String,
    val empresa: String?,
    val centroTrabajo: String?,
    val actualizadoEn: Long
)
