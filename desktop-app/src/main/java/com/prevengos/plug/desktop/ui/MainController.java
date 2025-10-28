package com.prevengos.plug.desktop.ui;

import com.prevengos.plug.desktop.AppContainer;
import com.prevengos.plug.desktop.config.DesktopConfiguration;
import com.prevengos.plug.desktop.model.Cuestionario;
import com.prevengos.plug.desktop.model.Paciente;
import com.prevengos.plug.desktop.model.SyncMetadata;
import com.prevengos.plug.desktop.repository.RepositoryException;
import com.prevengos.plug.desktop.sync.SyncService;
import com.prevengos.plug.desktop.sync.dto.SyncBatchResponse;
import com.prevengos.plug.desktop.sync.dto.SyncPullResponse;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

public class MainController {

    private final DesktopConfiguration configuration;
    private final AppContainer container;
    private final ObservableList<Paciente> pacientes = FXCollections.observableArrayList();
    private final ObservableList<Cuestionario> cuestionarios = FXCollections.observableArrayList();

    @FXML
    private TableView<Paciente> pacienteTable;
    @FXML
    private TableColumn<Paciente, String> nifColumn;
    @FXML
    private TableColumn<Paciente, String> nombreColumn;
    @FXML
    private TableColumn<Paciente, String> telefonoColumn;
    @FXML
    private TableView<Cuestionario> cuestionarioTable;
    @FXML
    private TableColumn<Cuestionario, String> cuestionarioCodigoColumn;
    @FXML
    private TableColumn<Cuestionario, String> cuestionarioEstadoColumn;
    @FXML
    private TableColumn<Cuestionario, String> cuestionarioActualizadoColumn;
    @FXML
    private Label nombreCompletoLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label lastModifiedLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label metadataLabel;

    public MainController(DesktopConfiguration configuration) {
        this.configuration = configuration;
        this.container = new AppContainer(configuration);
    }

    @FXML
    public void initialize() {
        pacienteTable.setItems(pacientes);
        cuestionarioTable.setItems(cuestionarios);

        nifColumn.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(cellData.getValue()::nif));
        nombreColumn.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(cellData.getValue()::nombreCompleto));
        telefonoColumn.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(() -> {
            String telefono = cellData.getValue().telefono();
            return telefono == null ? "" : telefono;
        }));

        cuestionarioCodigoColumn.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(cellData.getValue()::plantillaCodigo));
        cuestionarioEstadoColumn.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(cellData.getValue()::estado));
        cuestionarioActualizadoColumn.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(() -> {
            if (cellData.getValue().updatedAt() == null) {
                return "";
            }
            return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(cellData.getValue().updatedAt());
        }));

        pacienteTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                loadCuestionarios(newSelection.pacienteId());
                updatePacienteDetails(newSelection);
            } else {
                cuestionarios.clear();
                clearPacienteDetails();
            }
        });

        refreshPacientes();
        updateMetadataLabel();
    }

    @FXML
    public void onNuevoPaciente() {
        PacienteDialogController.PacienteDialogResult result = showPacienteDialog(null);
        if (result != null) {
            try {
                Paciente paciente = container.pacienteRepository().create(
                        result.nif(),
                        result.nombre(),
                        result.apellidos(),
                        result.fechaNacimiento(),
                        result.sexo(),
                        result.telefono(),
                        result.email(),
                        result.empresaId(),
                        result.centroId(),
                        result.externoRef()
                );
                pacientes.add(paciente);
                pacienteTable.getSelectionModel().select(paciente);
                setStatus("Paciente creado correctamente");
            } catch (RepositoryException e) {
                showError("No se pudo crear el paciente", e);
            }
        }
    }

    @FXML
    public void onEditarPaciente() {
        Paciente seleccionado = pacienteTable.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            showInfo("Seleccione un paciente para editar");
            return;
        }
        PacienteDialogController.PacienteDialogResult result = showPacienteDialog(seleccionado);
        if (result != null) {
            try {
                Paciente actualizado = container.pacienteRepository().update(
                        seleccionado.pacienteId(),
                        result.nif(),
                        result.nombre(),
                        result.apellidos(),
                        result.fechaNacimiento(),
                        result.sexo(),
                        result.telefono(),
                        result.email(),
                        result.empresaId(),
                        result.centroId(),
                        result.externoRef()
                );
                int index = pacientes.indexOf(seleccionado);
                pacientes.set(index, actualizado);
                pacienteTable.getSelectionModel().select(actualizado);
                setStatus("Paciente actualizado");
            } catch (RepositoryException e) {
                showError("No se pudo actualizar el paciente", e);
            }
        }
    }

    @FXML
    public void onEliminarPaciente() {
        Paciente seleccionado = pacienteTable.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            showInfo("Seleccione un paciente para eliminar");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar paciente");
        confirm.setHeaderText("¿Desea eliminar al paciente seleccionado?");
        confirm.setContentText(seleccionado.nombreCompleto());
        Optional<ButtonType> action = confirm.showAndWait();
        if (action.isPresent() && action.get() == ButtonType.OK) {
            try {
                container.pacienteRepository().delete(seleccionado.pacienteId());
                pacientes.remove(seleccionado);
                cuestionarios.clear();
                clearPacienteDetails();
                setStatus("Paciente eliminado");
            } catch (RepositoryException e) {
                showError("No se pudo eliminar el paciente", e);
            }
        }
    }

    @FXML
    public void onNuevoCuestionario() {
        Paciente paciente = pacienteTable.getSelectionModel().getSelectedItem();
        if (paciente == null) {
            showInfo("Seleccione un paciente antes de crear un cuestionario");
            return;
        }
        CuestionarioDialogController.CuestionarioDialogResult result = showCuestionarioDialog(null, paciente);
        if (result != null) {
            try {
                Cuestionario cuestionario = container.cuestionarioRepository().create(
                        paciente.pacienteId(),
                        result.plantillaCodigo(),
                        result.estado(),
                        result.respuestas(),
                        result.firmas(),
                        result.adjuntos(),
                        result.createdAt(),
                        result.updatedAt()
                );
                cuestionarios.add(0, cuestionario);
                cuestionarioTable.getSelectionModel().select(cuestionario);
                setStatus("Cuestionario creado");
            } catch (RepositoryException e) {
                showError("No se pudo crear el cuestionario", e);
            }
        }
    }

    @FXML
    public void onEditarCuestionario() {
        Cuestionario cuestionario = cuestionarioTable.getSelectionModel().getSelectedItem();
        Paciente paciente = pacienteTable.getSelectionModel().getSelectedItem();
        if (cuestionario == null || paciente == null) {
            showInfo("Seleccione un cuestionario");
            return;
        }
        CuestionarioDialogController.CuestionarioDialogResult result = showCuestionarioDialog(cuestionario, paciente);
        if (result != null) {
            try {
                Cuestionario actualizado = container.cuestionarioRepository().update(
                        cuestionario.cuestionarioId(),
                        result.estado(),
                        result.respuestas(),
                        result.firmas(),
                        result.adjuntos()
                );
                int index = cuestionarios.indexOf(cuestionario);
                cuestionarios.set(index, actualizado);
                cuestionarioTable.getSelectionModel().select(actualizado);
                setStatus("Cuestionario actualizado");
            } catch (RepositoryException e) {
                showError("No se pudo actualizar el cuestionario", e);
            }
        }
    }

    @FXML
    public void onEliminarCuestionario() {
        Cuestionario cuestionario = cuestionarioTable.getSelectionModel().getSelectedItem();
        if (cuestionario == null) {
            showInfo("Seleccione un cuestionario para eliminar");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar cuestionario");
        confirm.setHeaderText("¿Desea eliminar el cuestionario seleccionado?");
        Optional<ButtonType> action = confirm.showAndWait();
        if (action.isPresent() && action.get() == ButtonType.OK) {
            try {
                container.cuestionarioRepository().delete(cuestionario.cuestionarioId());
                cuestionarios.remove(cuestionario);
                setStatus("Cuestionario eliminado");
            } catch (RepositoryException e) {
                showError("No se pudo eliminar el cuestionario", e);
            }
        }
    }

    @FXML
    public void onPush(ActionEvent event) {
        runAsync("Sincronizando cambios locales", () -> {
            SyncService syncService = container.syncService();
            SyncBatchResponse response = syncService.pushDirtyEntities();
            Platform.runLater(() -> {
                refreshPacientes();
                setStatus("Cambios enviados. Pacientes: " + response.pacientesProcesados() + ", cuestionarios: " + response.cuestionariosProcesados());
                updateMetadataLabel();
            });
        });
    }

    @FXML
    public void onPull(ActionEvent event) {
        runAsync("Recuperando cambios del Hub", () -> {
            SyncService syncService = container.syncService();
            SyncPullResponse response = syncService.pullUpdates();
            Platform.runLater(() -> {
                refreshPacientes();
                setStatus("Pull completado. Próximo token: " + response.nextToken());
                updateMetadataLabel();
            });
        });
    }

    @FXML
    public void onExportJson() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exportar datos");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
        chooser.setInitialFileName("prevengos-desktop-export.json");
        Window window = statusLabel.getScene().getWindow();
        Path selected = Optional.ofNullable(chooser.showSaveDialog(window)).map(java.io.File::toPath).orElse(null);
        if (selected != null) {
            try {
                container.jsonExportService().exportTo(selected);
                setStatus("Datos exportados a " + selected);
            } catch (IOException e) {
                showError("No se pudo exportar el archivo", e);
            }
        }
    }

    @FXML
    public void onImportJson() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Importar datos");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
        Window window = statusLabel.getScene().getWindow();
        Path selected = Optional.ofNullable(chooser.showOpenDialog(window)).map(java.io.File::toPath).orElse(null);
        if (selected != null) {
            try {
                container.jsonExportService().importFrom(selected);
                refreshPacientes();
                setStatus("Importación completada desde " + selected);
                updateMetadataLabel();
            } catch (IOException e) {
                showError("No se pudo importar el archivo", e);
            }
        }
    }

    @FXML
    public void onExit(ActionEvent event) {
        Platform.exit();
    }

    private void refreshPacientes() {
        pacientes.setAll(container.pacienteRepository().findAll());
        if (!pacientes.isEmpty()) {
            pacienteTable.getSelectionModel().selectFirst();
        } else {
            cuestionarios.clear();
            clearPacienteDetails();
        }
    }

    private void loadCuestionarios(UUID pacienteId) {
        cuestionarios.setAll(container.cuestionarioRepository().findByPaciente(pacienteId));
    }

    private void updatePacienteDetails(Paciente paciente) {
        nombreCompletoLabel.setText(paciente.nombreCompleto());
        emailLabel.setText(paciente.email() == null ? "" : paciente.email());
        if (paciente.lastModified() > 0) {
            String formatted = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    .withZone(ZoneId.systemDefault())
                    .format(Instant.ofEpochMilli(paciente.lastModified()));
            lastModifiedLabel.setText(formatted);
        } else {
            lastModifiedLabel.setText("");
        }
    }

    private void clearPacienteDetails() {
        nombreCompletoLabel.setText("");
        emailLabel.setText("");
        lastModifiedLabel.setText("");
    }

    private void updateMetadataLabel() {
        SyncMetadata metadata = container.metadataRepository().readMetadata();
        StringBuilder builder = new StringBuilder();
        if (metadata.lastSyncToken() != null) {
            builder.append("Token: ").append(metadata.lastSyncToken());
        }
        if (metadata.lastPullAt() != null) {
            if (builder.length() > 0) {
                builder.append(" | ");
            }
            builder.append("Pull: ").append(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(metadata.lastPullAt()));
        }
        if (metadata.lastPushAt() != null) {
            if (builder.length() > 0) {
                builder.append(" | ");
            }
            builder.append("Push: ").append(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(metadata.lastPushAt()));
        }
        metadataLabel.setText(builder.toString());
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
    }

    private void runAsync(String pendingMessage, Runnable runnable) {
        setStatus(pendingMessage + "...");
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                runnable.run();
                return null;
            }

            @Override
            protected void failed() {
                Throwable ex = getException();
                Platform.runLater(() -> showError("La operación falló", ex));
            }
        };
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private PacienteDialogController.PacienteDialogResult showPacienteDialog(Paciente paciente) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/paciente-dialog.fxml"));
            DialogPane pane = loader.load();
            PacienteDialogController controller = loader.getController();
            controller.configure(paciente);
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(pane);
            dialog.setTitle(paciente == null ? "Nuevo paciente" : "Editar paciente");
            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                try {
                    return controller.buildResult();
                } catch (IllegalArgumentException ex) {
                    showError("Datos de paciente inválidos", ex);
                    return showPacienteDialog(paciente);
                }
            }
        } catch (IOException e) {
            showError("No se pudo mostrar el diálogo de paciente", e);
        }
        return null;
    }

    private CuestionarioDialogController.CuestionarioDialogResult showCuestionarioDialog(Cuestionario cuestionario, Paciente paciente) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/cuestionario-dialog.fxml"));
            DialogPane pane = loader.load();
            CuestionarioDialogController controller = loader.getController();
            controller.configure(cuestionario, paciente);
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(pane);
            dialog.setTitle(cuestionario == null ? "Nuevo cuestionario" : "Editar cuestionario");
            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                try {
                    return controller.buildResult();
                } catch (IllegalArgumentException ex) {
                    showError("Datos de cuestionario inválidos", ex);
                    return showCuestionarioDialog(cuestionario, paciente);
                }
            }
        } catch (IOException e) {
            showError("No se pudo mostrar el diálogo de cuestionario", e);
        }
        return null;
    }

    private void showError(String message, Throwable e) {
        if (!(e instanceof IllegalArgumentException)) {
            e.printStackTrace();
        }
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(message);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
        setStatus(message);
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setTitle("Información");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
