package com.prevengos.plug.android;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.prevengos.plug.android.data.local.entity.CuestionarioEntity;
import com.prevengos.plug.android.data.local.entity.PacienteEntity;
import com.prevengos.plug.android.databinding.ActivityMainBinding;
import com.prevengos.plug.android.databinding.DialogCuestionarioBinding;
import com.prevengos.plug.android.databinding.DialogPacienteBinding;
import com.prevengos.plug.android.ui.CuestionarioAdapter;
import com.prevengos.plug.android.ui.MainUiState;
import com.prevengos.plug.android.ui.MainViewModel;
import com.prevengos.plug.android.ui.MainViewModelFactory;
import com.prevengos.plug.android.ui.PacienteAdapter;

import java.util.List;

public class MainActivity extends AppCompatActivity implements PacienteAdapter.Callbacks, CuestionarioAdapter.Callbacks {
    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private PacienteAdapter pacienteAdapter;
    private CuestionarioAdapter cuestionarioAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.topAppBar);

        pacienteAdapter = new PacienteAdapter(this);
        cuestionarioAdapter = new CuestionarioAdapter(this);

        binding.pacientesList.setLayoutManager(new LinearLayoutManager(this));
        binding.pacientesList.setAdapter(pacienteAdapter);
        binding.cuestionariosList.setLayoutManager(new LinearLayoutManager(this));
        binding.cuestionariosList.setAdapter(cuestionarioAdapter);
        binding.addCuestionarioFab.setEnabled(false);

        viewModel = new ViewModelProvider(this,
                new MainViewModelFactory((PrevengosApplication) getApplication()))
                .get(MainViewModel.class);

        observeViewModel();
        setupActions();
    }

    private void observeViewModel() {
        viewModel.getPacientes().observe(this, pacientes -> {
            pacienteAdapter.submitList(pacientes);
            ensureSelection(pacientes);
        });
        viewModel.getSelectedPacienteId().observe(this, pacienteId -> {
            pacienteAdapter.setSelectedPacienteId(pacienteId);
            binding.addCuestionarioFab.setEnabled(pacienteId != null);
            updatePacienteSeleccionado(viewModel.getSelectedPacienteNombre().getValue());
        });
        viewModel.getSelectedPacienteNombre().observe(this, this::updatePacienteSeleccionado);
        viewModel.getCuestionarios().observe(this, cuestionarios -> {
            cuestionarioAdapter.submitList(cuestionarios);
            boolean empty = cuestionarios == null || cuestionarios.isEmpty();
            binding.cuestionariosEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        });
        viewModel.getUiState().observe(this, this::renderUiState);
    }

    private void setupActions() {
        binding.addPacienteFab.setOnClickListener(v -> showPacienteDialog(null));
        binding.addCuestionarioFab.setOnClickListener(v -> {
            String pacienteId = viewModel.getSelectedPacienteId().getValue();
            if (pacienteId == null) {
                Snackbar.make(binding.getRoot(), R.string.error_paciente_no_seleccionado, Snackbar.LENGTH_SHORT).show();
            } else {
                showCuestionarioDialog(null);
            }
        });
    }

    @Override
    public void onPacienteSelected(PacienteEntity paciente) {
        viewModel.selectPaciente(paciente.getPacienteId());
    }

    @Override
    public void onPacienteEdit(PacienteEntity paciente) {
        showPacienteDialog(paciente);
    }

    @Override
    public void onCuestionarioEdit(CuestionarioEntity cuestionario) {
        showCuestionarioDialog(cuestionario);
    }

    private void showPacienteDialog(@Nullable PacienteEntity existente) {
        DialogPacienteBinding dialogBinding = DialogPacienteBinding.inflate(getLayoutInflater());
        if (existente != null) {
            setText(dialogBinding.nifInput, existente.getNif());
            setText(dialogBinding.nombreInput, existente.getNombre());
            setText(dialogBinding.apellidosInput, existente.getApellidos());
            setText(dialogBinding.telefonoInput, existente.getTelefono());
            setText(dialogBinding.emailInput, existente.getEmail());
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle(existente == null
                        ? R.string.dialog_titulo_nuevo_paciente
                        : R.string.dialog_titulo_editar_paciente)
                .setView(dialogBinding.getRoot())
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(existente == null
                        ? R.string.action_guardar
                        : R.string.action_actualizar, null);
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    if (!validarFormularioPaciente(dialogBinding)) {
                        return;
                    }
                    String nif = readText(dialogBinding.nifInput);
                    String nombre = readText(dialogBinding.nombreInput);
                    String apellidos = readText(dialogBinding.apellidosInput);
                    String telefono = readOptional(dialogBinding.telefonoInput);
                    String email = readOptional(dialogBinding.emailInput);
                    if (existente == null) {
                        viewModel.createPaciente(nif, nombre, apellidos, telefono, email);
                    } else {
                        viewModel.updatePaciente(existente.getPacienteId(), nif, nombre, apellidos, telefono, email);
                    }
                    dialog.dismiss();
                }));
        dialog.show();
    }

    private void showCuestionarioDialog(@Nullable CuestionarioEntity existente) {
        DialogCuestionarioBinding dialogBinding = DialogCuestionarioBinding.inflate(getLayoutInflater());
        if (existente != null) {
            setText(dialogBinding.plantillaInput, existente.getPlantillaCodigo());
            setText(dialogBinding.estadoInput, existente.getEstado());
            if (existente.getRespuestas() != null && !existente.getRespuestas().isEmpty()) {
                setText(dialogBinding.notaInput, existente.getRespuestas().get(0).getValor());
            }
        }
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle(existente == null
                        ? R.string.dialog_titulo_nuevo_cuestionario
                        : R.string.dialog_titulo_editar_cuestionario)
                .setView(dialogBinding.getRoot())
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(existente == null
                        ? R.string.action_guardar
                        : R.string.action_actualizar, null);
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    if (!validarFormularioCuestionario(dialogBinding)) {
                        return;
                    }
                    String plantilla = readText(dialogBinding.plantillaInput);
                    String estado = readText(dialogBinding.estadoInput);
                    String nota = readOptional(dialogBinding.notaInput);
                    if (existente == null) {
                        String pacienteId = viewModel.getSelectedPacienteId().getValue();
                        viewModel.createCuestionario(pacienteId, plantilla, estado, nota);
                    } else {
                        viewModel.updateCuestionario(existente.getCuestionarioId(), estado, nota);
                    }
                    dialog.dismiss();
                }));
        dialog.show();
    }

    private boolean validarFormularioPaciente(DialogPacienteBinding binding) {
        clearError(binding.nifInput);
        clearError(binding.nombreInput);
        clearError(binding.apellidosInput);
        clearError(binding.emailInput);

        boolean valido = true;
        if (TextUtils.isEmpty(readText(binding.nifInput))) {
            binding.nifInput.setError(getString(R.string.error_campo_requerido));
            valido = false;
        }
        if (TextUtils.isEmpty(readText(binding.nombreInput))) {
            binding.nombreInput.setError(getString(R.string.error_campo_requerido));
            valido = false;
        }
        if (TextUtils.isEmpty(readText(binding.apellidosInput))) {
            binding.apellidosInput.setError(getString(R.string.error_campo_requerido));
            valido = false;
        }
        String email = readOptional(binding.emailInput);
        if (!TextUtils.isEmpty(email) && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInput.setError(getString(R.string.error_email_invalido));
            valido = false;
        }
        return valido;
    }

    private boolean validarFormularioCuestionario(DialogCuestionarioBinding binding) {
        clearError(binding.plantillaInput);
        clearError(binding.estadoInput);
        boolean valido = true;
        if (TextUtils.isEmpty(readText(binding.plantillaInput))) {
            binding.plantillaInput.setError(getString(R.string.error_campo_requerido));
            valido = false;
        }
        if (TextUtils.isEmpty(readText(binding.estadoInput))) {
            binding.estadoInput.setError(getString(R.string.error_campo_requerido));
            valido = false;
        }
        return valido;
    }

    private void renderUiState(MainUiState state) {
        binding.progressIndicator.setVisibility(state.isSaving() ? View.VISIBLE : View.GONE);
        if (!TextUtils.isEmpty(state.getMessage())) {
            Snackbar.make(binding.getRoot(), state.getMessage(), Snackbar.LENGTH_SHORT).show();
            viewModel.acknowledgeFeedback();
        } else if (!TextUtils.isEmpty(state.getError())) {
            Snackbar.make(binding.getRoot(), state.getError(), Snackbar.LENGTH_SHORT).show();
            viewModel.acknowledgeFeedback();
        }
    }

    private void ensureSelection(@Nullable List<PacienteEntity> pacientes) {
        if (pacientes == null || pacientes.isEmpty()) {
            viewModel.selectPaciente(null);
            return;
        }
        String actual = viewModel.getSelectedPacienteId().getValue();
        if (actual == null) {
            viewModel.selectPaciente(pacientes.get(0).getPacienteId());
            return;
        }
        boolean stillPresent = false;
        for (PacienteEntity paciente : pacientes) {
            if (paciente.getPacienteId().equals(actual)) {
                stillPresent = true;
                break;
            }
        }
        if (!stillPresent) {
            viewModel.selectPaciente(pacientes.get(0).getPacienteId());
        }
    }

    private void updatePacienteSeleccionado(@Nullable String nombre) {
        if (TextUtils.isEmpty(nombre)) {
            binding.pacienteSeleccionado.setText(R.string.label_paciente_seleccionado);
        } else {
            binding.pacienteSeleccionado.setText(getString(R.string.label_paciente_seleccionado_con_nombre, nombre));
        }
    }

    private void setText(com.google.android.material.textfield.TextInputLayout layout, @Nullable String valor) {
        if (layout.getEditText() != null) {
            layout.getEditText().setText(valor);
        }
    }

    private String readText(com.google.android.material.textfield.TextInputLayout layout) {
        if (layout.getEditText() == null || layout.getEditText().getText() == null) {
            return "";
        }
        return layout.getEditText().getText().toString().trim();
    }

    @Nullable
    private String readOptional(com.google.android.material.textfield.TextInputLayout layout) {
        String texto = readText(layout);
        return texto.isEmpty() ? null : texto;
    }

    private void clearError(com.google.android.material.textfield.TextInputLayout layout) {
        layout.setError(null);
    }
}
