package com.prevengos.plug.desktop.pacientes;

import com.prevengos.plug.shared.contracts.v1.Paciente;
import com.prevengos.plug.shared.contracts.v1.Paciente.Sexo;
import com.prevengos.plug.shared.time.ContractDateFormats;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.SplitPane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public final class PacientesPane extends BorderPane {

    private final PacienteRepository repository;
    private final ExecutorService executor;

    private final ObservableList<Paciente> pacientes = FXCollections.observableArrayList();
    private final TableView<Paciente> tableView = new TableView<>();
    private final TextField searchField = new TextField();
    private final ReadOnlyStringWrapper statusText = new ReadOnlyStringWrapper("Listo");
    private final PacienteForm form;

    public PacientesPane(PacienteRepository repository, ExecutorService executor) {
        this.repository = repository;
        this.executor = executor;
        this.form = new PacienteForm();
        buildLayout();
    }

    private void buildLayout() {
        getStyleClass().add("pacientes-pane");
        setPadding(new Insets(24));

        Label title = new Label("Gestión de pacientes");
        title.getStyleClass().add("view-title");

        searchField.setPromptText("Buscar por NIF, nombre o apellidos");
        searchField.setPrefWidth(280);
        searchField.setOnAction(event -> refresh());
        searchField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                searchField.clear();
                refresh();
            }
        });

        Button searchButton = new Button("Buscar");
        searchButton.getStyleClass().add("ghost-button");
        searchButton.setOnAction(event -> refresh());

        Button clearButton = new Button("Limpiar");
        clearButton.getStyleClass().add("ghost-button");
        clearButton.setOnAction(event -> {
            searchField.clear();
            refresh();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button newButton = new Button("Nuevo paciente");
        newButton.getStyleClass().add("primary-button");
        newButton.setOnAction(event -> {
            tableView.getSelectionModel().clearSelection();
            form.beginNewRecord();
        });

        HBox header = new HBox(12, title, spacer, searchField, searchButton, clearButton, newButton);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("view-header");
        header.setPadding(new Insets(0, 0, 18, 0));

        configureTable();

        form.saveButton().setOnAction(event -> handleSave());
        form.deleteButton().setOnAction(event -> handleDelete());
        form.resetButton().setOnAction(event -> form.reset());

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
            if (selected != null) {
                form.showPaciente(selected);
            }
        });

        SplitPane splitPane = new SplitPane(tableView, form);
        splitPane.setDividerPositions(0.48);
        splitPane.getStyleClass().add("content-split");

        setTop(header);
        setCenter(splitPane);
    }

    private void configureTable() {
        tableView.setItems(pacientes);
        tableView.getStyleClass().add("pacientes-table");
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tableView.setPlaceholder(new Label("No hay pacientes registrados."));

        TableColumn<Paciente, String> nifColumn = new TableColumn<>("NIF");
        nifColumn.setMinWidth(110);
        nifColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().nif()));

        TableColumn<Paciente, String> nombreColumn = new TableColumn<>("Nombre");
        nombreColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().nombre()));

        TableColumn<Paciente, String> apellidosColumn = new TableColumn<>("Apellidos");
        apellidosColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().apellidos()));

        TableColumn<Paciente, String> fechaColumn = new TableColumn<>("Nacimiento");
        fechaColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(ContractDateFormats.formatDate(cell.getValue().fechaNacimiento())));

        TableColumn<Paciente, String> sexoColumn = new TableColumn<>("Sexo");
        sexoColumn.setMaxWidth(70);
        sexoColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().sexo().code()));

        TableColumn<Paciente, String> actualizadoColumn = new TableColumn<>("Actualizado");
        actualizadoColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                cell.getValue().updatedAt().map(value -> ContractDateFormats.formatDateTime(value)).orElse("—")));

        tableView.getColumns().setAll(nifColumn, nombreColumn, apellidosColumn, fechaColumn, sexoColumn, actualizadoColumn);
        tableView.getSortOrder().add(apellidosColumn);
    }

    public void refresh() {
        String filter = searchField.getText();
        setStatus(filter == null || filter.isBlank() ? "Cargando pacientes…" : "Buscando pacientes…");

        CompletableFuture
                .supplyAsync(() -> repository.search(filter), executor)
                .whenComplete((result, error) -> Platform.runLater(() -> {
                    if (error != null) {
                        showError("No se pudieron cargar los pacientes", error);
                        setStatus("Error al cargar pacientes");
                    } else {
                        pacientes.setAll(result);
                        pacientes.sort(Comparator
                                .comparing(Paciente::apellidos, String.CASE_INSENSITIVE_ORDER)
                                .thenComparing(Paciente::nombre, String.CASE_INSENSITIVE_ORDER));
                        if (!result.isEmpty()) {
                            tableView.getSelectionModel().select(0);
                        }
                        setStatus(result.isEmpty() ? "Sin registros" : result.size() + " pacientes cargados");
                    }
                }));
    }

    private void handleSave() {
        Paciente draft;
        try {
            draft = form.buildPaciente();
        } catch (IllegalArgumentException ex) {
            showValidationError(ex.getMessage());
            return;
        } catch (RuntimeException ex) {
            showValidationError(ex.getMessage());
            return;
        }

        setStatus("Guardando paciente…");
        CompletableFuture
                .supplyAsync(() -> repository.save(draft), executor)
                .whenComplete((saved, error) -> Platform.runLater(() -> {
                    if (error != null) {
                        showError("No se pudo guardar el paciente", error);
                        setStatus("Error al guardar");
                    } else {
                        upsertPaciente(saved);
                        tableView.getSelectionModel().select(saved);
                        form.updateAfterSave(saved);
                        setStatus("Paciente guardado correctamente");
                    }
                }));
    }

    private void handleDelete() {
        Optional<UUID> currentId = form.currentPacienteId();
        if (currentId.isEmpty()) {
            return;
        }

        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar paciente");
        confirm.setHeaderText("¿Quieres eliminar este registro?");
        confirm.setContentText("La eliminación se sincronizará con el hub en la próxima subida.");
        ButtonType deleteAction = new ButtonType("Eliminar", ButtonData.OK_DONE);
        confirm.getButtonTypes().setAll(ButtonType.CANCEL, deleteAction);
        confirm.showAndWait().filter(response -> response == deleteAction).ifPresent(response -> {
            setStatus("Eliminando paciente…");
            CompletableFuture
                    .runAsync(() -> repository.delete(currentId.get()), executor)
                    .whenComplete((ignored, error) -> Platform.runLater(() -> {
                        if (error != null) {
                            showError("No se pudo eliminar el paciente", error);
                            setStatus("Error al eliminar");
                        } else {
                            pacientes.removeIf(p -> p.pacienteId().equals(currentId.get()));
                            form.beginNewRecord();
                            setStatus("Paciente eliminado");
                        }
                    }));
        });
    }

    private void upsertPaciente(Paciente paciente) {
        for (int i = 0; i < pacientes.size(); i++) {
            if (pacientes.get(i).pacienteId().equals(paciente.pacienteId())) {
                pacientes.set(i, paciente);
                return;
            }
        }
        pacientes.add(paciente);
    }

    private void setStatus(String message) {
        statusText.set(message);
    }

    public ReadOnlyStringProperty statusProperty() {
        return statusText.getReadOnlyProperty();
    }

    private void showError(String message, Throwable error) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(message);
        alert.setContentText(Optional.ofNullable(error.getMessage()).orElse("Revisa los registros de la aplicación."));
        alert.showAndWait();
    }

    private void showValidationError(String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("Datos inválidos");
        alert.setHeaderText("Por favor corrige los campos del formulario");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private final class PacienteForm extends VBox {

        private final Label title = new Label("Detalle del paciente");
        private final Label updatedAtLabel = new Label("—");

        private final TextField nifField = new TextField();
        private final TextField nombreField = new TextField();
        private final TextField apellidosField = new TextField();
        private final DatePicker fechaNacimientoPicker = new DatePicker();
        private final ComboBox<Sexo> sexoCombo = new ComboBox<>();
        private final TextField telefonoField = new TextField();
        private final TextField emailField = new TextField();
        private final TextField empresaIdField = new TextField();
        private final TextField centroIdField = new TextField();
        private final TextArea externoRefField = new TextArea();

        private final Button saveButton = new Button("Guardar cambios");
        private final Button deleteButton = new Button("Eliminar");
        private final Button resetButton = new Button("Restablecer");

        private UUID currentId;
        private Paciente loadedPaciente;

        private PacienteForm() {
            getStyleClass().add("paciente-form");
            setPadding(new Insets(24));
            setSpacing(18);

            title.getStyleClass().add("form-title");

            VBox header = new VBox(4, title, buildUpdatedAtLine());

            sexoCombo.setItems(FXCollections.observableArrayList(Sexo.values()));
            sexoCombo.setPromptText("Sexo");

            telefonoField.setPromptText("Teléfono");
            emailField.setPromptText("Email corporativo");
            empresaIdField.setPromptText("UUID empresa");
            centroIdField.setPromptText("UUID centro");
            externoRefField.setPromptText("Referencia externa / notas");
            externoRefField.setPrefRowCount(3);

            GridPane formGrid = buildFormGrid();

            HBox actions = new HBox(12, saveButton, deleteButton, resetButton);
            actions.getStyleClass().add("form-actions");

            saveButton.getStyleClass().add("primary-button");
            deleteButton.getStyleClass().add("danger-button");
            resetButton.getStyleClass().add("ghost-button");

            getChildren().addAll(header, formGrid, actions);

            beginNewRecord();
        }

        private VBox buildUpdatedAtLine() {
            Label subtitle = new Label("Última actualización");
            subtitle.getStyleClass().add("subtitle");

            updatedAtLabel.getStyleClass().add("updated-at");

            VBox box = new VBox(2, subtitle, updatedAtLabel);
            box.getStyleClass().add("updated-box");
            return box;
        }

        private GridPane buildFormGrid() {
            GridPane grid = new GridPane();
            grid.setHgap(12);
            grid.setVgap(12);
            grid.getStyleClass().add("form-grid");

            ColumnConstraints col1 = new ColumnConstraints();
            col1.setPercentWidth(50);
            ColumnConstraints col2 = new ColumnConstraints();
            col2.setPercentWidth(50);
            grid.getColumnConstraints().addAll(col1, col2);

            addLabeledField(grid, "NIF", nifField, 0, 0);
            addLabeledField(grid, "Nombre", nombreField, 1, 0);
            addLabeledField(grid, "Apellidos", apellidosField, 0, 1);
            addLabeledField(grid, "Fecha de nacimiento", fechaNacimientoPicker, 1, 1);
            addLabeledField(grid, "Sexo", sexoCombo, 0, 2);
            addLabeledField(grid, "Teléfono", telefonoField, 1, 2);
            addLabeledField(grid, "Email", emailField, 0, 3);
            addLabeledField(grid, "Empresa ID", empresaIdField, 1, 3);
            addLabeledField(grid, "Centro ID", centroIdField, 0, 4);
            addLabeledField(grid, "Notas", externoRefField, 1, 4);

            return grid;
        }

        private void addLabeledField(GridPane grid, String label, Region field, int column, int row) {
            Label fieldLabel = new Label(label);
            fieldLabel.getStyleClass().add("field-label");
            VBox box = new VBox(4, fieldLabel, field);
            box.getStyleClass().add("field-wrapper");
            GridPane.setColumnIndex(box, column);
            GridPane.setRowIndex(box, row);
            grid.getChildren().add(box);
            if (field instanceof TextField textField) {
                textField.textProperty().addListener((obs, oldValue, newValue) -> {
                    if (textField == emailField) {
                        textField.setTooltip(newValue != null && newValue.length() > 3 ? new Tooltip(newValue) : null);
                    }
                });
            }
        }

        void showPaciente(Paciente paciente) {
            this.loadedPaciente = paciente;
            this.currentId = paciente.pacienteId();

            title.setText("Detalle del paciente");
            deleteButton.setDisable(false);

            nifField.setText(paciente.nif());
            nombreField.setText(paciente.nombre());
            apellidosField.setText(paciente.apellidos());
            fechaNacimientoPicker.setValue(paciente.fechaNacimiento());
            sexoCombo.setValue(paciente.sexo());
            telefonoField.setText(paciente.telefono().orElse(""));
            emailField.setText(paciente.email().orElse(""));
            empresaIdField.setText(paciente.empresaId().map(UUID::toString).orElse(""));
            centroIdField.setText(paciente.centroId().map(UUID::toString).orElse(""));
            externoRefField.setText(paciente.externoRef().orElse(""));
            updatedAtLabel.setText(paciente.updatedAt().map(ContractDateFormats::formatDateTime).orElse("—"));
        }

        void beginNewRecord() {
            this.loadedPaciente = null;
            this.currentId = UUID.randomUUID();
            title.setText("Nuevo paciente");
            deleteButton.setDisable(true);
            nifField.clear();
            nombreField.clear();
            apellidosField.clear();
            fechaNacimientoPicker.setValue(LocalDate.now());
            sexoCombo.setValue(Sexo.M);
            telefonoField.clear();
            emailField.clear();
            empresaIdField.clear();
            centroIdField.clear();
            externoRefField.clear();
            updatedAtLabel.setText("Pendiente de guardar");
        }

        void reset() {
            if (loadedPaciente != null) {
                showPaciente(loadedPaciente);
            } else {
                beginNewRecord();
            }
        }

        Paciente buildPaciente() {
            UUID id = currentId != null ? currentId : UUID.randomUUID();
            String nif = valueOrThrow(nifField.getText(), "El NIF es obligatorio");
            String nombre = valueOrThrow(nombreField.getText(), "El nombre es obligatorio");
            String apellidos = valueOrThrow(apellidosField.getText(), "Los apellidos son obligatorios");
            LocalDate fechaNacimiento = fechaNacimientoPicker.getValue();
            if (fechaNacimiento == null) {
                throw new IllegalArgumentException("Selecciona una fecha de nacimiento válida.");
            }
            Sexo sexo = sexoCombo.getValue();
            if (sexo == null) {
                throw new IllegalArgumentException("Selecciona el sexo del paciente.");
            }

            Paciente.Builder builder = Paciente.builder()
                    .pacienteId(id)
                    .nif(nif)
                    .nombre(nombre)
                    .apellidos(apellidos)
                    .fechaNacimiento(fechaNacimiento)
                    .sexo(sexo);

            applyOptionalField(telefonoField.getText()).ifPresent(builder::telefono);
            applyOptionalField(emailField.getText()).ifPresent(builder::email);
            parseUuid(empresaIdField.getText(), "empresa_id").ifPresent(builder::empresaId);
            parseUuid(centroIdField.getText(), "centro_id").ifPresent(builder::centroId);
            applyOptionalField(externoRefField.getText()).ifPresent(builder::externoRef);

            if (loadedPaciente != null) {
                loadedPaciente.createdAt().ifPresent(builder::createdAt);
                loadedPaciente.updatedAt().ifPresent(builder::updatedAt);
            }

            return builder.build();
        }

        private Optional<UUID> parseUuid(String value, String field) {
            String trimmed = applyOptionalField(value).orElse(null);
            if (trimmed == null) {
                return Optional.empty();
            }
            try {
                return Optional.of(UUID.fromString(trimmed));
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("El campo " + field + " debe ser un UUID válido.");
            }
        }

        private Optional<String> applyOptionalField(String value) {
            if (value == null) {
                return Optional.empty();
            }
            String trimmed = value.trim();
            return trimmed.isEmpty() ? Optional.empty() : Optional.of(trimmed);
        }

        private String valueOrThrow(String value, String message) {
            if (value == null || value.trim().isEmpty()) {
                throw new IllegalArgumentException(message);
            }
            return value.trim();
        }

        void updateAfterSave(Paciente saved) {
            this.loadedPaciente = saved;
            this.currentId = saved.pacienteId();
            showPaciente(saved);
        }

        Optional<UUID> currentPacienteId() {
            return Optional.ofNullable(loadedPaciente != null ? loadedPaciente.pacienteId() : currentId);
        }

        Button saveButton() {
            return saveButton;
        }

        Button deleteButton() {
            return deleteButton;
        }

        Button resetButton() {
            return resetButton;
        }
    }
}
