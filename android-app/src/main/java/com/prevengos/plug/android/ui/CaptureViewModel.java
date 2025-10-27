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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CaptureViewModel extends ViewModel {
    private static final String DEFAULT_PLANTILLA = "anamnesis_inicial";

    private final PacienteRepository pacienteRepository;
    private final CuestionarioRepository cuestionarioRepository;
    private final PrevengosApplication application;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<CaptureUiState> uiState = new MutableLiveData<>(new CaptureUiState());
    private final LiveData<String> pacientes;

    public CaptureViewModel(
            PacienteRepository pacienteRepository,
            CuestionarioRepository cuestionarioRepository,
            PrevengosApplication application
    ) {
        this.pacienteRepository = pacienteRepository;
        this.cuestionarioRepository = cuestionarioRepository;
        this.application = application;
        this.pacientes = Transformations.map(
                pacienteRepository.observePacientes(),
                this::buildResumen
        );
    }

    public LiveData<CaptureUiState> getUiState() {
        return uiState;
    }

    public LiveData<String> getPacientes() {
        return pacientes;
    }

    public void guardarCaptura(String nif, String nombre, String apellidos, String telefono, String email, String nota) {
        if (nif == null || nif.trim().isEmpty() || nombre == null || nombre.trim().isEmpty() || apellidos == null || apellidos.trim().isEmpty()) {
            uiState.postValue(new CaptureUiState(false, null, "Los campos NIF, nombre y apellidos son obligatorios"));
            return;
        }
        uiState.postValue(new CaptureUiState(true, null, null));
        executor.execute(() -> {
            try {
                String telefonoValue = telefono != null && !telefono.trim().isEmpty() ? telefono : null;
                String emailValue = email != null && !email.trim().isEmpty() ? email : null;
                String notaValue = nota != null && !nota.trim().isEmpty() ? nota : null;
                PacienteEntity paciente = pacienteRepository.createPaciente(nif.trim(), nombre.trim(), apellidos.trim(), telefonoValue, emailValue);
                RespuestaLocal respuesta = new RespuestaLocal("nota_inicial", notaValue, null, null);
                cuestionarioRepository.createDraft(
                        paciente.getPacienteId(),
                        DEFAULT_PLANTILLA,
                        Collections.singletonList(respuesta)
                );
                application.triggerOneTimeSync();
                uiState.postValue(new CaptureUiState(false, "Captura guardada para " + paciente.getNombre(), null));
            } catch (Exception exception) {
                String errorMessage = exception.getMessage() != null ? exception.getMessage() : "Error desconocido";
                uiState.postValue(new CaptureUiState(false, null, errorMessage));
            }
        });
    }

    public void acknowledgeFeedback() {
        uiState.postValue(new CaptureUiState());
    }

    private String buildResumen(List<PacienteEntity> pacientes) {
        if (pacientes == null || pacientes.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (PacienteEntity paciente : pacientes) {
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(paciente.getNombre()).append(' ').append(paciente.getApellidos());
        }
        return builder.toString();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdownNow();
    }
}
