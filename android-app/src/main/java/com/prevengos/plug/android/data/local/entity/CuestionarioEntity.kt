package com.prevengos.plug.android.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cuestionarios")
data class CuestionarioEntity(
    @PrimaryKey val cuestionarioId: String,
    val pacienteId: String,
    val plantillaCodigo: String,
    val estado: String,
    val respuestasJson: String,
    val completadoEn: Long?,
    val actualizadoEn: Long
)
