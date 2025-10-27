package com.prevengos.plug.android.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.prevengos.plug.android.PrevengosApplication;
import com.prevengos.plug.android.R;
import com.prevengos.plug.android.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private CaptureViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(
                this,
                new CaptureViewModelFactory((PrevengosApplication) getApplication())
        ).get(CaptureViewModel.class);

        binding.saveButton.setOnClickListener(view -> viewModel.guardarCaptura(
                getText(binding.nifInput),
                getText(binding.nombreInput),
                getText(binding.apellidosInput),
                getOptionalText(binding.telefonoInput),
                getOptionalText(binding.emailInput),
                getText(binding.notaInput)
        ));

        viewModel.getUiState().observe(this, state -> {
            if (state == null) {
                return;
            }
            binding.saveButton.setEnabled(!state.isSaving());
            if (state.getMessage() != null) {
                Snackbar.make(binding.getRoot(), state.getMessage(), Snackbar.LENGTH_SHORT).show();
                clearInputs();
                viewModel.acknowledgeFeedback();
            } else if (state.getError() != null) {
                Snackbar.make(binding.getRoot(), state.getError(), Snackbar.LENGTH_SHORT).show();
                viewModel.acknowledgeFeedback();
            }
        });

        viewModel.getPacientes().observe(this, resumen -> {
            if (resumen == null || resumen.trim().isEmpty()) {
                binding.pacientesRegistrados.setText(getString(R.string.sin_pacientes));
            } else {
                binding.pacientesRegistrados.setText(resumen);
            }
        });
    }

    private String getText(com.google.android.material.textfield.TextInputLayout layout) {
        if (layout.getEditText() == null) {
            return "";
        }
        CharSequence value = layout.getEditText().getText();
        return value != null ? value.toString() : "";
    }

    private String getOptionalText(com.google.android.material.textfield.TextInputLayout layout) {
        if (layout.getEditText() == null) {
            return null;
        }
        CharSequence value = layout.getEditText().getText();
        return value != null ? value.toString() : null;
    }

    private void clearInputs() {
        if (binding.nifInput.getEditText() != null) {
            binding.nifInput.getEditText().setText(null);
        }
        if (binding.nombreInput.getEditText() != null) {
            binding.nombreInput.getEditText().setText(null);
        }
        if (binding.apellidosInput.getEditText() != null) {
            binding.apellidosInput.getEditText().setText(null);
        }
        if (binding.telefonoInput.getEditText() != null) {
            binding.telefonoInput.getEditText().setText(null);
        }
        if (binding.emailInput.getEditText() != null) {
            binding.emailInput.getEditText().setText(null);
        }
        if (binding.notaInput.getEditText() != null) {
            binding.notaInput.getEditText().setText(null);
        }
    }
}
