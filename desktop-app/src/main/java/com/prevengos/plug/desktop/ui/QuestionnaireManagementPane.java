package com.prevengos.plug.desktop.ui;

import com.prevengos.plug.desktop.model.Patient;
import com.prevengos.plug.desktop.model.Questionnaire;
import com.prevengos.plug.desktop.service.LocalStorageService;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.time.Instant;
import java.util.UUID;

/**
 * Panel CRUD para la gestión de cuestionarios.
 */
public class QuestionnaireManagementPane {

    private final BorderPane root;
    private final ObservableList<Questionnaire> questionnaires;

    public QuestionnaireManagementPane(LocalStorageService localStorageService) {
        ObservableList<Patient> patients = FXCollections.observableArrayList(localStorageService.listPatients());
        this.questionnaires = FXCollections.observableArrayList(localStorageService.listQuestionnaires());

        TableView<Questionnaire> tableView = new TableView<>(questionnaires);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.getColumns().add(column("Título", "title"));

        TableColumn<Questionnaire, String> patientColumn = new TableColumn<>("Paciente");
        patientColumn.setCellValueFactory(cellData -> {
            UUID patientId = cellData.getValue().getPatientId();
            return new ReadOnlyStringWrapper(patients.stream()
                    .filter(patient -> patient.getId().equals(patientId))
                    .map(patient -> patient.getFirstName() + " " + patient.getLastName())
                    .findFirst()
                    .orElse(patientId.toString()));
        });
        tableView.getColumns().add(patientColumn);

        ComboBox<Patient> patientComboBox = new ComboBox<>(patients);
        patientComboBox.setConverter(new PatientStringConverter());
        TextField titleField = new TextField();
        TextArea responsesArea = new TextArea();
        responsesArea.setPrefRowCount(5);

        GridPane form = new GridPane();
        form.setPadding(new Insets(10));
        form.setHgap(10);
        form.setVgap(10);
        form.addRow(0, new Label("Paciente"), patientComboBox);
        form.addRow(1, new Label("Título"), titleField);
        form.addRow(2, new Label("Respuestas"), responsesArea);

        Button refreshPatients = new Button("Refrescar pacientes");
        refreshPatients.setOnAction(event -> patients.setAll(localStorageService.listPatients()));

        Button saveButton = new Button("Crear/Actualizar");
        saveButton.setOnAction(event -> {
            Patient patient = patientComboBox.getValue();
            if (patient == null) {
                return;
            }
            String title = titleField.getText();
            if (title.isBlank()) {
                return;
            }
            Questionnaire selected = tableView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                Questionnaire questionnaire = new Questionnaire(
                        UUID.randomUUID(),
                        patient.getId(),
                        title,
                        responsesArea.getText(),
                        Instant.now(),
                        Instant.now()
                );
                localStorageService.saveQuestionnaire(questionnaire);
                questionnaires.add(questionnaire);
            } else {
                Questionnaire updated = new Questionnaire(
                        selected.getId(),
                        patient.getId(),
                        title,
                        responsesArea.getText(),
                        selected.getCreatedAt(),
                        Instant.now()
                );
                localStorageService.saveQuestionnaire(updated);
                int index = questionnaires.indexOf(selected);
                questionnaires.set(index, updated);
            }
            clearForm(patientComboBox, titleField, responsesArea, tableView);
        });

        Button deleteButton = new Button("Eliminar");
        deleteButton.setOnAction(event -> {
            Questionnaire selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                localStorageService.deleteQuestionnaire(selected.getId());
                questionnaires.remove(selected);
            }
        });

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                patientComboBox.getSelectionModel().select(patients.stream()
                        .filter(patient -> patient.getId().equals(newValue.getPatientId()))
                        .findFirst()
                        .orElse(null));
                titleField.setText(newValue.getTitle());
                responsesArea.setText(newValue.getResponses());
            }
        });

        HBox buttons = new HBox(10, refreshPatients, saveButton, deleteButton);
        buttons.setPadding(new Insets(10));

        root = new BorderPane();
        root.setCenter(tableView);
        root.setRight(form);
        root.setBottom(buttons);
    }

    private TableColumn<Questionnaire, String> column(String title, String property) {
        TableColumn<Questionnaire, String> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        return column;
    }

    private void clearForm(ComboBox<Patient> patientComboBox,
                           TextField titleField,
                           TextArea responsesArea,
                           TableView<Questionnaire> tableView) {
        patientComboBox.getSelectionModel().clearSelection();
        titleField.clear();
        responsesArea.clear();
        tableView.getSelectionModel().clearSelection();
    }

    public Node getRoot() {
        return root;
    }
}
