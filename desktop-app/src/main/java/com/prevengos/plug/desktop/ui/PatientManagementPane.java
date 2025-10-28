package com.prevengos.plug.desktop.ui;

import com.prevengos.plug.desktop.model.Patient;
import com.prevengos.plug.desktop.service.LocalStorageService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Panel CRUD básico para pacientes.
 */
public class PatientManagementPane {

    private final BorderPane root;
    private final ObservableList<Patient> patients;

    public PatientManagementPane(LocalStorageService localStorageService) {
        this.patients = FXCollections.observableArrayList(localStorageService.listPatients());

        TableView<Patient> tableView = new TableView<>(patients);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.getColumns().add(column("Nombre", "firstName"));
        tableView.getColumns().add(column("Apellidos", "lastName"));
        tableView.getColumns().add(column("Documento", "documentNumber"));

        TextField nameField = new TextField();
        TextField lastNameField = new TextField();
        TextField documentField = new TextField();
        DatePicker birthDatePicker = new DatePicker();

        GridPane form = new GridPane();
        form.setPadding(new Insets(10));
        form.setHgap(10);
        form.setVgap(10);
        form.addRow(0, new Label("Nombre"), nameField);
        form.addRow(1, new Label("Apellidos"), lastNameField);
        form.addRow(2, new Label("Documento"), documentField);
        form.addRow(3, new Label("Fecha nacimiento"), birthDatePicker);

        Button addButton = new Button("Crear/Actualizar");
        addButton.setOnAction(event -> {
            String firstName = nameField.getText();
            String lastName = lastNameField.getText();
            if (firstName.isBlank() || lastName.isBlank()) {
                return;
            }
            LocalDate birthDate = birthDatePicker.getValue();
            Patient selected = tableView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                Patient patient = new Patient(
                        UUID.randomUUID(),
                        firstName,
                        lastName,
                        documentField.getText(),
                        birthDate,
                        Instant.now(),
                        Instant.now()
                );
                localStorageService.savePatient(patient);
                patients.add(patient);
            } else {
                Patient updated = new Patient(
                        selected.getId(),
                        firstName,
                        lastName,
                        documentField.getText(),
                        birthDate,
                        selected.getCreatedAt(),
                        Instant.now()
                );
                localStorageService.savePatient(updated);
                int index = patients.indexOf(selected);
                patients.set(index, updated);
            }
            clearForm(nameField, lastNameField, documentField, birthDatePicker, tableView);
        });

        Button deleteButton = new Button("Eliminar");
        deleteButton.setOnAction(event -> {
            Patient selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                localStorageService.deletePatient(selected.getId());
                patients.remove(selected);
            }
        });

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                nameField.setText(newValue.getFirstName());
                lastNameField.setText(newValue.getLastName());
                documentField.setText(newValue.getDocumentNumber());
                birthDatePicker.setValue(newValue.getBirthDate());
            }
        });

        HBox buttons = new HBox(10, addButton, deleteButton);
        buttons.setPadding(new Insets(10));

        root = new BorderPane();
        root.setCenter(tableView);
        root.setBottom(buttons);
        root.setRight(form);
    }

    private TableColumn<Patient, String> column(String title, String property) {
        TableColumn<Patient, String> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        return column;
    }

    private void clearForm(TextField nameField,
                           TextField lastNameField,
                           TextField documentField,
                           DatePicker birthDatePicker,
                           TableView<Patient> tableView) {
        nameField.clear();
        lastNameField.clear();
        documentField.clear();
        birthDatePicker.setValue(null);
        tableView.getSelectionModel().clearSelection();
    }

    public Node getRoot() {
        return root;
    }
}
