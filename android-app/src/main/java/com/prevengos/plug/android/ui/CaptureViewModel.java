package com.prevengos.plug.android.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.prevengos.plug.android.PrevengosApplication;
import com.prevengos.plug.android.data.local.entity.PacienteEntity;
import com.prevengos.plug.android.data.local.entity.RespuestaLocal;
import com.prevengos.plug.android.data.repository.CuestionarioRepository;
import com.prevengos.plug.android.data.repository.PacienteRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

public class CaptureViewModel extends ViewModel {
    private static final String DEFAULT_PLANTILLA = "anamnesis_inicial";

    private final PacienteRepository pacienteRepository;
    private final CuestionarioRepository cuestionarioRepository;
    private final PrevengosApplication application;
    private final ExecutorService executorService;

    private final MutableLiveData<CaptureUiState> uiState = new MutableLiveData<>(new CaptureUiState());
    private final LiveData<String> pacientesResumen;

    public CaptureViewModel(PacienteRepository pacienteRepository,
                            CuestionarioRepository cuestionarioRepository,
                            PrevengosApplication application,
                            ExecutorService executorService) {
        this.pacienteRepository = pacienteRepository;
        this.cuestionarioRepository = cuestionarioRepository;
        this.application = application;
        this.executorService = executorService;
        pacientesResumen = Transformations.map(
                pacienteRepository.observePacientes(),
                pacientes -> {
                    if (pacientes == null || pacientes.isEmpty()) {
                        return "";
                    }
                    List<String> nombres = new ArrayList<>();
                    for (PacienteEntity paciente : pacientes) {
                        nombres.add(String.format(Locale.getDefault(), "%s %s",
                                paciente.getNombre(), paciente.getApellidos()));
                    }
                    return String.join("\n", nombres);
                }
        );
    }

    public LiveData<CaptureUiState> getUiState() {
        return uiState;
    }

    public LiveData<String> getPacientesResumen() {
        return pacientesResumen;
    }

    public void guardarCaptura(String nif,
                               String nombre,
                               String apellidos,
                               String telefono,
                               String email,
                               String nota) {
        if (isBlank(nif) || isBlank(nombre) || isBlank(apellidos)) {
            uiState.setValue(CaptureUiState.failure("Los campos NIF, nombre y apellidos son obligatorios"));
            return;
        }
        uiState.setValue(CaptureUiState.saving());
        executorService.execute(() -> {
            try {
                PacienteEntity paciente = pacienteRepository.createPaciente(
                        nif.trim(),
                        nombre.trim(),
                        apellidos.trim(),
                        toNullable(telefono),
                        toNullable(email));
                List<RespuestaLocal> respuestas = new ArrayList<>();
                respuestas.add(new RespuestaLocal("nota_inicial", nota == null || nota.trim().isEmpty() ? null : nota.trim(), null, null));
                cuestionarioRepository.createDraft(
                        paciente.getPacienteId(),
                        DEFAULT_PLANTILLA,
                        respuestas);
                application.triggerOneTimeSync();
                uiState.postValue(CaptureUiState.success(
                        "Captura guardada para " + paciente.getNombre()));
            } catch (Exception exception) {
                uiState.postValue(CaptureUiState.failure(
                        exception.getMessage() != null ? exception.getMessage() : "Error desconocido"));
            }
        });
    }

    public void acknowledgeFeedback() {
        uiState.setValue(new CaptureUiState());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String toNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
