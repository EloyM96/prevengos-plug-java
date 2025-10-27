package com.prevengos.plug.android.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pacientes")
data class PacienteEntity(
    @PrimaryKey
    @ColumnInfo(name = "paciente_id")
    val pacienteId: String,
    val nif: String,
    val nombre: String,
    val apellidos: String,
    @ColumnInfo(name = "fecha_nacimiento")
    val fechaNacimiento: String?,
    val sexo: String?,
    val telefono: String?,
    val email: String?,
    @ColumnInfo(name = "empresa_id")
    val empresaId: String?,
    @ColumnInfo(name = "centro_id")
    val centroId: String?,
    @ColumnInfo(name = "externo_ref")
    val externoRef: String?,
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
