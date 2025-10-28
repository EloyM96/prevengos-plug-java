package com.prevengos.plug.android.ui;

import android.os.Handler;
import android.os.Looper;
import android.util.Patterns;

import androidx.annotation.MainThread;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.prevengos.plug.android.PrevengosApplication;
import com.prevengos.plug.android.R;
import com.prevengos.plug.android.data.local.entity.CuestionarioEntity;
import com.prevengos.plug.android.data.local.entity.PacienteEntity;
import com.prevengos.plug.android.data.local.entity.RespuestaLocal;
import com.prevengos.plug.android.data.repository.CuestionarioRepository;
import com.prevengos.plug.android.data.repository.PacienteRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class MainViewModel extends ViewModel {
    private static final String NOTA_PREGUNTA_CODIGO = "nota_inicial";

    private final PacienteRepository pacienteRepository;
    private final CuestionarioRepository cuestionarioRepository;
    private final PrevengosApplication application;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    private final LiveData<List<PacienteEntity>> pacientes;
    private final MediatorLiveData<List<CuestionarioEntity>> cuestionarios = new MediatorLiveData<>();
    private final MutableLiveData<String> selectedPacienteId = new MutableLiveData<>();
    private final MutableLiveData<String> selectedPacienteNombre = new MutableLiveData<>();
    private final MutableLiveData<MainUiState> uiState = new MutableLiveData<>(MainUiState.idle());

    @Nullable
    private LiveData<List<CuestionarioEntity>> cuestionarioSource;

    public MainViewModel(PacienteRepository pacienteRepository,
                         CuestionarioRepository cuestionarioRepository,
                         PrevengosApplication application,
                         ExecutorService executorService) {
        this.pacienteRepository = pacienteRepository;
        this.cuestionarioRepository = cuestionarioRepository;
        this.application = application;
        this.executorService = executorService;
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.pacientes = pacienteRepository.observePacientes();
        cuestionarios.setValue(Collections.emptyList());
    }

    public LiveData<List<PacienteEntity>> getPacientes() {
        return pacientes;
    }

    public LiveData<List<CuestionarioEntity>> getCuestionarios() {
        return cuestionarios;
    }

    public LiveData<String> getSelectedPacienteId() {
        return selectedPacienteId;
    }

    public LiveData<String> getSelectedPacienteNombre() {
        return selectedPacienteNombre;
    }

    public LiveData<MainUiState> getUiState() {
        return uiState;
    }

    @MainThread
    public void selectPaciente(@Nullable String pacienteId) {
        selectedPacienteId.setValue(pacienteId);
        if (pacienteId == null) {
            detachCuestionariosSource();
            cuestionarios.setValue(Collections.emptyList());
            selectedPacienteNombre.setValue(null);
            return;
        }
        attachCuestionariosSource(pacienteId);
        executorService.execute(() -> {
            PacienteEntity entity = pacienteRepository.findById(pacienteId);
            if (entity != null) {
                String nombre = entity.getNombre() + " " + entity.getApellidos();
                selectedPacienteNombre.postValue(nombre.trim());
            }
        });
    }

    public void createPaciente(String nif,
                                String nombre,
                                String apellidos,
                                @Nullable String telefono,
                                @Nullable String email) {
        String nifValue = sanitize(nif);
        String nombreValue = sanitize(nombre);
        String apellidosValue = sanitize(apellidos);
        if (isBlank(nifValue) || isBlank(nombreValue) || isBlank(apellidosValue)) {
            uiState.setValue(MainUiState.failure(application.getString(R.string.error_campos_obligatorios)));
            return;
        }
        String telefonoValue = toNullable(telefono);
        String emailValue = toNullable(email);
        if (emailValue != null && !Patterns.EMAIL_ADDRESS.matcher(emailValue).matches()) {
            uiState.setValue(MainUiState.failure(application.getString(R.string.error_email_invalido)));
            return;
        }
        uiState.setValue(MainUiState.saving());
        executorService.execute(() -> {
            try {
                PacienteEntity entity = pacienteRepository.createPaciente(
                        nifValue,
                        nombreValue,
                        apellidosValue,
                        telefonoValue,
                        emailValue);
                mainHandler.post(() -> selectPaciente(entity.getPacienteId()));
                application.triggerOneTimeSync();
                uiState.postValue(MainUiState.success(application.getString(R.string.mensaje_paciente_guardado)));
            } catch (Exception exception) {
                uiState.postValue(MainUiState.failure(resolveError(exception)));
            }
        });
    }

    public void updatePaciente(String pacienteId,
                               String nif,
                               String nombre,
                               String apellidos,
                               @Nullable String telefono,
                               @Nullable String email) {
        if (pacienteId == null) {
            uiState.setValue(MainUiState.failure(application.getString(R.string.error_paciente_no_seleccionado)));
            return;
        }
        String nifValue = sanitize(nif);
        String nombreValue = sanitize(nombre);
        String apellidosValue = sanitize(apellidos);
        if (isBlank(nifValue) || isBlank(nombreValue) || isBlank(apellidosValue)) {
            uiState.setValue(MainUiState.failure(application.getString(R.string.error_campos_obligatorios)));
            return;
        }
        String telefonoValue = toNullable(telefono);
        String emailValue = toNullable(email);
        if (emailValue != null && !Patterns.EMAIL_ADDRESS.matcher(emailValue).matches()) {
            uiState.setValue(MainUiState.failure(application.getString(R.string.error_email_invalido)));
            return;
        }
        uiState.setValue(MainUiState.saving());
        executorService.execute(() -> {
            try {
                PacienteEntity actualizado = pacienteRepository.updatePaciente(
                        pacienteId,
                        nifValue,
                        nombreValue,
                        apellidosValue,
                        telefonoValue,
                        emailValue);
                mainHandler.post(() -> selectPaciente(actualizado.getPacienteId()));
                application.triggerOneTimeSync();
                uiState.postValue(MainUiState.success(application.getString(R.string.mensaje_paciente_actualizado)));
            } catch (Exception exception) {
                uiState.postValue(MainUiState.failure(resolveError(exception)));
            }
        });
    }

    public void createCuestionario(String pacienteId,
                                   String plantillaCodigo,
                                   String estado,
                                   @Nullable String nota) {
        if (pacienteId == null) {
            uiState.setValue(MainUiState.failure(application.getString(R.string.error_paciente_no_seleccionado)));
            return;
        }
        String plantillaValue = sanitize(plantillaCodigo);
        String estadoValue = sanitize(estado);
        if (isBlank(plantillaValue) || isBlank(estadoValue)) {
            uiState.setValue(MainUiState.failure(application.getString(R.string.error_cuestionario_obligatorio)));
            return;
        }
        List<RespuestaLocal> respuestas = buildRespuestas(nota);
        uiState.setValue(MainUiState.saving());
        executorService.execute(() -> {
            try {
                cuestionarioRepository.createCuestionario(pacienteId, plantillaValue, estadoValue, respuestas);
                application.triggerOneTimeSync();
                uiState.postValue(MainUiState.success(application.getString(R.string.mensaje_cuestionario_guardado)));
            } catch (Exception exception) {
                uiState.postValue(MainUiState.failure(resolveError(exception)));
            }
        });
    }

    public void updateCuestionario(String cuestionarioId,
                                   String estado,
                                   @Nullable String nota) {
        if (cuestionarioId == null) {
            uiState.setValue(MainUiState.failure(application.getString(R.string.error_cuestionario_no_encontrado)));
            return;
        }
        String estadoValue = sanitize(estado);
        if (isBlank(estadoValue)) {
            uiState.setValue(MainUiState.failure(application.getString(R.string.error_cuestionario_obligatorio)));
            return;
        }
        List<RespuestaLocal> respuestas = buildRespuestas(nota);
        uiState.setValue(MainUiState.saving());
        executorService.execute(() -> {
            try {
                cuestionarioRepository.updateCuestionario(cuestionarioId, estadoValue, respuestas);
                application.triggerOneTimeSync();
                uiState.postValue(MainUiState.success(application.getString(R.string.mensaje_cuestionario_actualizado)));
            } catch (Exception exception) {
                uiState.postValue(MainUiState.failure(resolveError(exception)));
            }
        });
    }

    public void acknowledgeFeedback() {
        uiState.setValue(MainUiState.idle());
    }

    private void attachCuestionariosSource(String pacienteId) {
        detachCuestionariosSource();
        cuestionarioSource = cuestionarioRepository.observeForPaciente(pacienteId);
        cuestionarios.addSource(cuestionarioSource, cuestionarios::setValue);
    }

    private void detachCuestionariosSource() {
        if (cuestionarioSource != null) {
            cuestionarios.removeSource(cuestionarioSource);
            cuestionarioSource = null;
        }
    }

    private boolean isBlank(@Nullable String value) {
        return value == null || value.trim().isEmpty();
    }

    private String sanitize(@Nullable String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    @Nullable
    private String toNullable(@Nullable String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private List<RespuestaLocal> buildRespuestas(@Nullable String nota) {
        List<RespuestaLocal> respuestas = new ArrayList<>();
        String valor = toNullable(nota);
        respuestas.add(new RespuestaLocal(NOTA_PREGUNTA_CODIGO, valor, null, null));
        return respuestas;
    }

    private String resolveError(Exception exception) {
        if (exception.getMessage() != null && !exception.getMessage().isEmpty()) {
            return exception.getMessage();
        }
        return application.getString(R.string.error_operacion_generica);
    }
}
