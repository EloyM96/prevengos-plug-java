package com.prevengos.plug.android.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.prevengos.plug.android.PrevengosApplication
import com.prevengos.plug.android.R
import com.prevengos.plug.android.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val viewModel: CaptureViewModel by viewModels {
        CaptureViewModelFactory(application as PrevengosApplication)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.saveButton.setOnClickListener {
            viewModel.guardarCaptura(
                nif = binding.nifInput.editText?.text?.toString().orEmpty(),
                nombre = binding.nombreInput.editText?.text?.toString().orEmpty(),
                apellidos = binding.apellidosInput.editText?.text?.toString().orEmpty(),
                telefono = binding.telefonoInput.editText?.text?.toString(),
                email = binding.emailInput.editText?.text?.toString(),
                nota = binding.notaInput.editText?.text?.toString().orEmpty()
            )
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        binding.saveButton.isEnabled = !state.isSaving
                        when {
                            state.message != null -> {
                                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                                clearInputs()
                                viewModel.acknowledgeFeedback()
                            }
                            state.error != null -> {
                                Snackbar.make(binding.root, state.error, Snackbar.LENGTH_SHORT).show()
                                viewModel.acknowledgeFeedback()
                            }
                        }
                    }
                }
                launch {
                    viewModel.pacientes.collect { resumen ->
                        binding.pacientesRegistrados.text =
                            if (resumen.isBlank()) getString(R.string.sin_pacientes)
                            else resumen
                    }
                }
            }
        }
    }

    private fun clearInputs() {
        with(binding) {
            nifInput.editText?.text = null
            nombreInput.editText?.text = null
            apellidosInput.editText?.text = null
            telefonoInput.editText?.text = null
            emailInput.editText?.text = null
            notaInput.editText?.text = null
        }
    }
}
