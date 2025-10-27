package com.prevengos.plug.android.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

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

        viewModel = new ViewModelProvider(this,
                new CaptureViewModelFactory((PrevengosApplication) getApplication()))
                .get(CaptureViewModel.class);

        binding.saveButton.setOnClickListener(this::onSaveClicked);

        viewModel.getUiState().observe(this, state -> {
            if (state == null) {
                return;
            }
            binding.saveButton.setEnabled(!state.isSaving());
            if (!TextUtils.isEmpty(state.getMessage())) {
                Snackbar.make(binding.getRoot(), state.getMessage(), Snackbar.LENGTH_SHORT).show();
                clearInputs();
                viewModel.acknowledgeFeedback();
            } else if (!TextUtils.isEmpty(state.getError())) {
                Snackbar.make(binding.getRoot(), state.getError(), Snackbar.LENGTH_SHORT).show();
                viewModel.acknowledgeFeedback();
            }
        });

        viewModel.getPacientesResumen().observe(this, resumen -> {
            if (TextUtils.isEmpty(resumen)) {
                binding.pacientesRegistrados.setText(getString(R.string.sin_pacientes));
            } else {
                binding.pacientesRegistrados.setText(resumen);
            }
        });
    }

    private void onSaveClicked(View view) {
        viewModel.guardarCaptura(
                readText(binding.nifInput.getEditText() != null ? binding.nifInput.getEditText().getText() : null),
                readText(binding.nombreInput.getEditText() != null ? binding.nombreInput.getEditText().getText() : null),
                readText(binding.apellidosInput.getEditText() != null ? binding.apellidosInput.getEditText().getText() : null),
                readOptional(binding.telefonoInput.getEditText() != null ? binding.telefonoInput.getEditText().getText() : null),
                readOptional(binding.emailInput.getEditText() != null ? binding.emailInput.getEditText().getText() : null),
                readText(binding.notaInput.getEditText() != null ? binding.notaInput.getEditText().getText() : null));
    }

    private String readText(@Nullable CharSequence value) {
        return value == null ? "" : value.toString();
    }

    @Nullable
    private String readOptional(@Nullable CharSequence value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.toString().trim();
        return trimmed.isEmpty() ? null : trimmed;
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
