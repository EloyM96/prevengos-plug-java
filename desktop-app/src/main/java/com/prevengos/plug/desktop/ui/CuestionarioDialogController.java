package com.prevengos.plug.desktop.ui;

import com.prevengos.plug.desktop.model.Cuestionario;
import com.prevengos.plug.desktop.model.Paciente;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class CuestionarioDialogController {

    @FXML
    private TextField plantillaField;
    @FXML
    private TextField estadoField;
    @FXML
    private TextArea respuestasArea;
    @FXML
    private TextArea firmasArea;
    @FXML
    private TextArea adjuntosArea;

    private Cuestionario existing;
    public void configure(Cuestionario cuestionario, Paciente paciente) {
        this.existing = cuestionario;
        if (cuestionario != null) {
            plantillaField.setText(cuestionario.plantillaCodigo());
            estadoField.setText(cuestionario.estado());
            respuestasArea.setText(cuestionario.respuestas());
            firmasArea.setText(cuestionario.firmas());
            adjuntosArea.setText(cuestionario.adjuntos());
        }
    }

    public CuestionarioDialogResult buildResult() {
        String plantilla = trimToNull(plantillaField.getText());
        String estado = trimToNull(estadoField.getText());
        if (plantilla == null || estado == null) {
            throw new IllegalArgumentException("Plantilla y estado son obligatorios");
        }
        String respuestas = trimToNull(respuestasArea.getText());
        String firmas = trimToNull(firmasArea.getText());
        String adjuntos = trimToNull(adjuntosArea.getText());
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime createdAt = existing != null ? existing.createdAt() : now;
        OffsetDateTime updatedAt = existing != null ? now : now;
        return new CuestionarioDialogResult(plantilla, estado, respuestas, firmas, adjuntos, createdAt, updatedAt);
    }

    private String trimToNull(String text) {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public record CuestionarioDialogResult(
            String plantillaCodigo,
            String estado,
            String respuestas,
            String firmas,
            String adjuntos,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
    }
}
