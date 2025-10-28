package com.prevengos.plug.desktop.ui;

import com.prevengos.plug.desktop.model.Patient;
import javafx.util.StringConverter;

/**
 * Convierte pacientes a una representación amigable en controles JavaFX.
 */
public class PatientStringConverter extends StringConverter<Patient> {

    @Override
    public String toString(Patient patient) {
        if (patient == null) {
            return "";
        }
        return patient.getFirstName() + " " + patient.getLastName();
    }

    @Override
    public Patient fromString(String string) {
        throw new UnsupportedOperationException("Solo se soporta la conversión de Patient a String");
    }
}
