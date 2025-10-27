package com.prevengos.plug.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prevengos.plug.android.PrevengosApplication
import com.prevengos.plug.android.data.local.entity.RespuestaLocal
import com.prevengos.plug.android.data.repository.CuestionarioRepository
import com.prevengos.plug.android.data.repository.PacienteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CaptureViewModel(
    private val pacienteRepository: PacienteRepository,
    private val cuestionarioRepository: CuestionarioRepository,
    private val application: PrevengosApplication
) : ViewModel() {

    private val _uiState = MutableStateFlow(CaptureUiState())
    val uiState: StateFlow<CaptureUiState> = _uiState

    val pacientes = pacienteRepository.observePacientes()
        .map { pacientes -> pacientes.joinToString(separator = "\n") { "${it.nombre} ${it.apellidos}" } }
        .catch { emit("") }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun guardarCaptura(
        nif: String,
        nombre: String,
        apellidos: String,
        telefono: String?,
        email: String?,
        nota: String
    ) {
        viewModelScope.launch {
            if (nif.isBlank() || nombre.isBlank() || apellidos.isBlank()) {
                _uiState.value = CaptureUiState(error = "Los campos NIF, nombre y apellidos son obligatorios")
                return@launch
            }
            _uiState.value = CaptureUiState(isSaving = true)
            try {
                val paciente = pacienteRepository.createPaciente(
                    nif = nif,
                    nombre = nombre,
                    apellidos = apellidos,
                    telefono = telefono?.takeIf { it.isNotBlank() },
                    email = email?.takeIf { it.isNotBlank() }
                )
                cuestionarioRepository.createDraft(
                    pacienteId = paciente.pacienteId,
                    plantillaCodigo = DEFAULT_PLANTILLA,
                    respuestas = listOf(
                        RespuestaLocal(
                            preguntaCodigo = "nota_inicial",
                            valor = nota.ifBlank { null }
                        )
                    )
                )
                application.triggerOneTimeSync()
                _uiState.value = CaptureUiState(message = "Captura guardada para ${paciente.nombre}")
            } catch (exception: Exception) {
                _uiState.value = CaptureUiState(error = exception.message ?: "Error desconocido")
            }
        }
    }

    fun acknowledgeFeedback() {
        _uiState.value = CaptureUiState()
    }

    companion object {
        private const val DEFAULT_PLANTILLA = "anamnesis_inicial"
    }
}
