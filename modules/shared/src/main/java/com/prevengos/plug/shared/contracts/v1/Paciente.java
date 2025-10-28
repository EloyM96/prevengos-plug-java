package com.prevengos.plug.shared.contracts.v1;

import com.prevengos.plug.shared.csv.CsvRecord;
import com.prevengos.plug.shared.time.ContractDateFormats;
import com.prevengos.plug.shared.validation.ContractValidator;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Shared representation of the {@code paciente.schema.json} contract.
 */
public final class Paciente {

    private static final Pattern NIF_PATTERN = Pattern.compile("^[0-9A-Za-z]{5,16}$");
    public static final List<String> CSV_HEADERS = List.of(
            "paciente_id",
            "nif",
            "nombre",
            "apellidos",
            "fecha_nacimiento",
            "sexo",
            "telefono",
            "email",
            "empresa_id",
            "centro_id",
            "externo_ref",
            "created_at",
            "updated_at"
    );

    private final UUID pacienteId;
    private final String nif;
    private final String nombre;
    private final String apellidos;
    private final LocalDate fechaNacimiento;
    private final Sexo sexo;
    private final String telefono;
    private final String email;
    private final UUID empresaId;
    private final UUID centroId;
    private final String externoRef;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;

    private Paciente(Builder builder) {
        this.pacienteId = ContractValidator.requireNonNull(builder.pacienteId, "paciente_id");
        this.nif = ContractValidator.requirePattern(ContractValidator.normalize(builder.nif), NIF_PATTERN, "nif", "nif must match pattern " + NIF_PATTERN);
        this.nombre = ContractValidator.requireNonBlank(ContractValidator.normalize(builder.nombre), "nombre");
        this.apellidos = ContractValidator.requireNonBlank(ContractValidator.normalize(builder.apellidos), "apellidos");
        this.fechaNacimiento = ContractValidator.requireNonNull(builder.fechaNacimiento, "fecha_nacimiento");
        this.sexo = ContractValidator.requireNonNull(builder.sexo, "sexo");
        this.telefono = ContractValidator.normalize(builder.telefono);
        this.email = ContractValidator.requireEmail(ContractValidator.normalize(builder.email), "email");
        this.empresaId = builder.empresaId;
        this.centroId = builder.centroId;
        this.externoRef = ContractValidator.normalize(builder.externoRef);
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public UUID pacienteId() {
        return pacienteId;
    }

    public String nif() {
        return nif;
    }

    public String nombre() {
        return nombre;
    }

    public String apellidos() {
        return apellidos;
    }

    public LocalDate fechaNacimiento() {
        return fechaNacimiento;
    }

    public Sexo sexo() {
        return sexo;
    }

    public Optional<String> telefono() {
        return Optional.ofNullable(telefono);
    }

    public Optional<String> email() {
        return Optional.ofNullable(email);
    }

    public Optional<UUID> empresaId() {
        return Optional.ofNullable(empresaId);
    }

    public Optional<UUID> centroId() {
        return Optional.ofNullable(centroId);
    }

    public Optional<String> externoRef() {
        return Optional.ofNullable(externoRef);
    }

    public Optional<OffsetDateTime> createdAt() {
        return Optional.ofNullable(createdAt);
    }

    public Optional<OffsetDateTime> updatedAt() {
        return Optional.ofNullable(updatedAt);
    }

    public CsvRecord toCsvRecord() {
        Map<String, String> ordered = new LinkedHashMap<>();
        ordered.put("paciente_id", pacienteId.toString());
        ordered.put("nif", nif);
        ordered.put("nombre", nombre);
        ordered.put("apellidos", apellidos);
        ordered.put("fecha_nacimiento", ContractDateFormats.formatDate(fechaNacimiento));
        ordered.put("sexo", sexo.code);
        ordered.put("telefono", telefono);
        ordered.put("email", email);
        ordered.put("empresa_id", empresaId != null ? empresaId.toString() : null);
        ordered.put("centro_id", centroId != null ? centroId.toString() : null);
        ordered.put("externo_ref", externoRef);
        ordered.put("created_at", ContractDateFormats.formatDateTime(createdAt));
        ordered.put("updated_at", ContractDateFormats.formatDateTime(updatedAt));
        return CsvRecord.of(ordered);
    }

    public static Paciente fromCsvRecord(CsvRecord record) {
        Builder builder = builder()
                .pacienteId(UUID.fromString(record.require("paciente_id")))
                .nif(record.require("nif"))
                .nombre(record.require("nombre"))
                .apellidos(record.require("apellidos"))
                .fechaNacimiento(ContractDateFormats.parseDate(record.require("fecha_nacimiento"), "fecha_nacimiento"))
                .sexo(Sexo.fromCode(record.require("sexo")));

        record.optional("telefono").ifPresent(builder::telefono);
        record.optional("email").ifPresent(builder::email);
        record.optional("empresa_id").ifPresent(value -> builder.empresaId(UUID.fromString(value)));
        record.optional("centro_id").ifPresent(value -> builder.centroId(UUID.fromString(value)));
        record.optional("externo_ref").ifPresent(builder::externoRef);
        record.optional("created_at").ifPresent(value -> builder.createdAt(ContractDateFormats.parseDateTime(value, "created_at")));
        record.optional("updated_at").ifPresent(value -> builder.updatedAt(ContractDateFormats.parseDateTime(value, "updated_at")));
        return builder.build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public enum Sexo {
        M("M"),
        F("F"),
        X("X");

        private final String code;

        Sexo(String code) {
            this.code = code;
        }

        public static Sexo fromCode(String code) {
            for (Sexo sexo : values()) {
                if (sexo.code.equals(code)) {
                    return sexo;
                }
            }
            throw new IllegalArgumentException("Unknown sexo code: " + code);
        }

        public String code() {
            return code;
        }
    }

    public static final class Builder {
        private UUID pacienteId;
        private String nif;
        private String nombre;
        private String apellidos;
        private LocalDate fechaNacimiento;
        private Sexo sexo;
        private String telefono;
        private String email;
        private UUID empresaId;
        private UUID centroId;
        private String externoRef;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;

        private Builder() {
        }

        public Builder pacienteId(UUID pacienteId) {
            this.pacienteId = pacienteId;
            return this;
        }

        public Builder nif(String nif) {
            this.nif = nif;
            return this;
        }

        public Builder nombre(String nombre) {
            this.nombre = nombre;
            return this;
        }

        public Builder apellidos(String apellidos) {
            this.apellidos = apellidos;
            return this;
        }

        public Builder fechaNacimiento(LocalDate fechaNacimiento) {
            this.fechaNacimiento = fechaNacimiento;
            return this;
        }

        public Builder sexo(Sexo sexo) {
            this.sexo = sexo;
            return this;
        }

        public Builder telefono(String telefono) {
            this.telefono = telefono;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder empresaId(UUID empresaId) {
            this.empresaId = empresaId;
            return this;
        }

        public Builder centroId(UUID centroId) {
            this.centroId = centroId;
            return this;
        }

        public Builder externoRef(String externoRef) {
            this.externoRef = externoRef;
            return this;
        }

        public Builder createdAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(OffsetDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Paciente build() {
            return new Paciente(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Paciente paciente)) {
            return false;
        }
        return Objects.equals(pacienteId, paciente.pacienteId) && Objects.equals(nif, paciente.nif) && Objects.equals(nombre, paciente.nombre) && Objects.equals(apellidos, paciente.apellidos) && Objects.equals(fechaNacimiento, paciente.fechaNacimiento) && sexo == paciente.sexo && Objects.equals(telefono, paciente.telefono) && Objects.equals(email, paciente.email) && Objects.equals(empresaId, paciente.empresaId) && Objects.equals(centroId, paciente.centroId) && Objects.equals(externoRef, paciente.externoRef) && Objects.equals(createdAt, paciente.createdAt) && Objects.equals(updatedAt, paciente.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pacienteId, nif, nombre, apellidos, fechaNacimiento, sexo, telefono, email, empresaId, centroId, externoRef, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "Paciente{" +
                "pacienteId=" + pacienteId +
                ", nif='" + nif + '\'' +
                ", nombre='" + nombre + '\'' +
                ", apellidos='" + apellidos + '\'' +
                ", fechaNacimiento=" + fechaNacimiento +
                ", sexo=" + sexo +
                ", telefono='" + telefono + '\'' +
                ", email='" + email + '\'' +
                ", empresaId=" + empresaId +
                ", centroId=" + centroId +
                ", externoRef='" + externoRef + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
