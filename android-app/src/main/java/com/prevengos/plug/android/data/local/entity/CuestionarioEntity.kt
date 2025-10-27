package com.prevengos.plug.android.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.prevengos.plug.android.data.local.room.JsonConverters

@Entity(tableName = "cuestionarios")
@TypeConverters(JsonConverters::class)
data class CuestionarioEntity(
    @PrimaryKey
    @ColumnInfo(name = "cuestionario_id")
    val cuestionarioId: String,
    @ColumnInfo(name = "paciente_id")
    val pacienteId: String,
    @ColumnInfo(name = "plantilla_codigo")
    val plantillaCodigo: String,
    val estado: String,
    val respuestas: List<RespuestaLocal>,
    val firmas: List<String>,
    val adjuntos: List<String>,
    @ColumnInfo(name = "created_at")
    val createdAt: String?,
    @ColumnInfo(name = "updated_at")
    val updatedAt: String?,
    @ColumnInfo(name = "last_modified")
    val lastModified: Long,
    @ColumnInfo(name = "sync_token")
    val syncToken: String?,
    @ColumnInfo(name = "is_dirty")
    val isDirty: Boolean
)
