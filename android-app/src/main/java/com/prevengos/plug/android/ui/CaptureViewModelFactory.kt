package com.prevengos.plug.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.prevengos.plug.android.PrevengosApplication

class CaptureViewModelFactory(private val application: PrevengosApplication) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CaptureViewModel::class.java)) {
            val container = application.container
            return CaptureViewModel(
                pacienteRepository = container.pacienteRepository,
                cuestionarioRepository = container.cuestionarioRepository,
                application = application
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
