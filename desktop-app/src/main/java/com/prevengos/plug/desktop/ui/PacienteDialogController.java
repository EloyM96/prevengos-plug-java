package com.prevengos.plug.desktop.ui;

import com.prevengos.plug.desktop.model.Paciente;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.util.UUID;

public class PacienteDialogController {

    @FXML
    private TextField nifField;
    @FXML
    private TextField nombreField;
    @FXML
    private TextField apellidosField;
    @FXML
    private DatePicker fechaNacimientoPicker;
    @FXML
    private TextField sexoField;
    @FXML
    private TextField telefonoField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField empresaField;
    @FXML
    private TextField centroField;
    @FXML
    private TextField externoField;

    public void configure(Paciente paciente) {
        if (paciente != null) {
            nifField.setText(paciente.nif());
            nombreField.setText(paciente.nombre());
            apellidosField.setText(paciente.apellidos());
            if (paciente.fechaNacimiento() != null) {
                fechaNacimientoPicker.setValue(paciente.fechaNacimiento());
            }
            sexoField.setText(paciente.sexo());
            telefonoField.setText(paciente.telefono());
            emailField.setText(paciente.email());
            empresaField.setText(paciente.empresaId() != null ? paciente.empresaId().toString() : "");
            centroField.setText(paciente.centroId() != null ? paciente.centroId().toString() : "");
            externoField.setText(paciente.externoRef());
        }
    }

    public PacienteDialogResult buildResult() {
        String nif = trimToNull(nifField.getText());
        String nombre = trimToNull(nombreField.getText());
        String apellidos = trimToNull(apellidosField.getText());
        if (nif == null || nombre == null || apellidos == null) {
            throw new IllegalArgumentException("NIF, nombre y apellidos son obligatorios");
        }
        LocalDate fechaNacimiento = fechaNacimientoPicker.getValue();
        String sexo = trimToNull(sexoField.getText());
        String telefono = trimToNull(telefonoField.getText());
        String email = trimToNull(emailField.getText());
        UUID empresaId = parseUuid(trimToNull(empresaField.getText()));
        UUID centroId = parseUuid(trimToNull(centroField.getText()));
        String externo = trimToNull(externoField.getText());
        return new PacienteDialogResult(nif, nombre, apellidos, fechaNacimiento, sexo, telefono, email, empresaId, centroId, externo);
    }

    private UUID parseUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return UUID.fromString(value);
    }

    private String trimToNull(String text) {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public record PacienteDialogResult(
            String nif,
            String nombre,
            String apellidos,
            LocalDate fechaNacimiento,
            String sexo,
            String telefono,
            String email,
            UUID empresaId,
            UUID centroId,
            String externoRef
    ) {
    }
}
