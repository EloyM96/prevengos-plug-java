package com.prevengos.plug.shared.contracts.v1;

import com.prevengos.plug.shared.csv.CsvRecord;
import com.prevengos.plug.shared.time.ContractDateFormats;
import com.prevengos.plug.shared.validation.ContractValidator;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Shared representation of the {@code cita.schema.json} contract.
 */
public final class CitaReconocimiento {

    public static final List<String> CSV_HEADERS = List.of(
            "cita_id",
            "paciente_id",
            "fecha",
            "tipo",
            "estado",
            "aptitud",
            "externo_ref",
            "observaciones",
            "created_at",
            "updated_at"
    );

    private final UUID citaId;
    private final UUID pacienteId;
    private final OffsetDateTime fecha;
    private final Tipo tipo;
    private final Estado estado;
    private final Aptitud aptitud;
    private final String externoRef;
    private final String observaciones;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;

    private CitaReconocimiento(Builder builder) {
        this.citaId = ContractValidator.requireNonNull(builder.citaId, "cita_id");
        this.pacienteId = ContractValidator.requireNonNull(builder.pacienteId, "paciente_id");
        this.fecha = ContractValidator.requireNonNull(builder.fecha, "fecha");
        this.tipo = ContractValidator.requireNonNull(builder.tipo, "tipo");
        this.estado = ContractValidator.requireNonNull(builder.estado, "estado");
        this.aptitud = builder.aptitud;
        this.externoRef = ContractValidator.normalize(builder.externoRef);
        this.observaciones = ContractValidator.normalize(builder.observaciones);
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public UUID citaId() {
        return citaId;
    }

    public UUID pacienteId() {
        return pacienteId;
    }

    public OffsetDateTime fecha() {
        return fecha;
    }

    public Tipo tipo() {
        return tipo;
    }

    public Estado estado() {
        return estado;
    }

    public Optional<Aptitud> aptitud() {
        return Optional.ofNullable(aptitud);
    }

    public Optional<String> externoRef() {
        return Optional.ofNullable(externoRef);
    }

    public Optional<String> observaciones() {
        return Optional.ofNullable(observaciones);
    }

    public Optional<OffsetDateTime> createdAt() {
        return Optional.ofNullable(createdAt);
    }

    public Optional<OffsetDateTime> updatedAt() {
        return Optional.ofNullable(updatedAt);
    }

    public CsvRecord toCsvRecord() {
        Map<String, String> ordered = new LinkedHashMap<>();
        ordered.put("cita_id", citaId.toString());
        ordered.put("paciente_id", pacienteId.toString());
        ordered.put("fecha", ContractDateFormats.formatDateTime(fecha));
        ordered.put("tipo", tipo.code);
        ordered.put("estado", estado.code);
        ordered.put("aptitud", aptitud != null ? aptitud.code : null);
        ordered.put("externo_ref", externoRef);
        ordered.put("observaciones", observaciones);
        ordered.put("created_at", ContractDateFormats.formatDateTime(createdAt));
        ordered.put("updated_at", ContractDateFormats.formatDateTime(updatedAt));
        return CsvRecord.of(ordered);
    }

    public static CitaReconocimiento fromCsvRecord(CsvRecord record) {
        Builder builder = builder()
                .citaId(UUID.fromString(record.require("cita_id")))
                .pacienteId(UUID.fromString(record.require("paciente_id")))
                .fecha(ContractDateFormats.parseDateTime(record.require("fecha"), "fecha"))
                .tipo(Tipo.fromCode(record.require("tipo")))
                .estado(Estado.fromCode(record.require("estado")));

        record.optional("aptitud").ifPresent(value -> builder.aptitud(Aptitud.fromCode(value)));
        record.optional("externo_ref").ifPresent(builder::externoRef);
        record.optional("observaciones").ifPresent(builder::observaciones);
        record.optional("created_at").ifPresent(value -> builder.createdAt(ContractDateFormats.parseDateTime(value, "created_at")));
        record.optional("updated_at").ifPresent(value -> builder.updatedAt(ContractDateFormats.parseDateTime(value, "updated_at")));
        return builder.build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public enum Tipo {
        INICIAL("inicial"),
        PERIODICO("periodico"),
        EXTRAORDINARIO("extraordinario");

        private final String code;

        Tipo(String code) {
            this.code = code;
        }

        public static Tipo fromCode(String code) {
            for (Tipo value : values()) {
                if (value.code.equals(code)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unknown tipo code: " + code);
        }

        public String code() {
            return code;
        }
    }

    public enum Estado {
        PLANIFICADA("planificada"),
        EN_CURSO("en_curso"),
        FINALIZADA("finalizada"),
        CANCELADA("cancelada");

        private final String code;

        Estado(String code) {
            this.code = code;
        }

        public static Estado fromCode(String code) {
            for (Estado value : values()) {
                if (value.code.equals(code)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unknown estado code: " + code);
        }

        public String code() {
            return code;
        }
    }

    public enum Aptitud {
        APTO("apto"),
        APTO_CON_LIMITACIONES("apto_con_limitaciones"),
        NO_APTO("no_apto"),
        PENDIENTE("pendiente");

        private final String code;

        Aptitud(String code) {
            this.code = code;
        }

        public static Aptitud fromCode(String code) {
            for (Aptitud value : values()) {
                if (value.code.equals(code)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unknown aptitud code: " + code);
        }

        public String code() {
            return code;
        }
    }

    public static final class Builder {
        private UUID citaId;
        private UUID pacienteId;
        private OffsetDateTime fecha;
        private Tipo tipo;
        private Estado estado;
        private Aptitud aptitud;
        private String externoRef;
        private String observaciones;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;

        private Builder() {
        }

        public Builder citaId(UUID citaId) {
            this.citaId = citaId;
            return this;
        }

        public Builder pacienteId(UUID pacienteId) {
            this.pacienteId = pacienteId;
            return this;
        }

        public Builder fecha(OffsetDateTime fecha) {
            this.fecha = fecha;
            return this;
        }

        public Builder tipo(Tipo tipo) {
            this.tipo = tipo;
            return this;
        }

        public Builder estado(Estado estado) {
            this.estado = estado;
            return this;
        }

        public Builder aptitud(Aptitud aptitud) {
            this.aptitud = aptitud;
            return this;
        }

        public Builder externoRef(String externoRef) {
            this.externoRef = externoRef;
            return this;
        }

        public Builder observaciones(String observaciones) {
            this.observaciones = observaciones;
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

        public CitaReconocimiento build() {
            return new CitaReconocimiento(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CitaReconocimiento that)) {
            return false;
        }
        return Objects.equals(citaId, that.citaId) && Objects.equals(pacienteId, that.pacienteId) && Objects.equals(fecha, that.fecha) && tipo == that.tipo && estado == that.estado && aptitud == that.aptitud && Objects.equals(externoRef, that.externoRef) && Objects.equals(observaciones, that.observaciones) && Objects.equals(createdAt, that.createdAt) && Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(citaId, pacienteId, fecha, tipo, estado, aptitud, externoRef, observaciones, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "CitaReconocimiento{" +
                "citaId=" + citaId +
                ", pacienteId=" + pacienteId +
                ", fecha=" + fecha +
                ", tipo=" + tipo +
                ", estado=" + estado +
                ", aptitud=" + aptitud +
                ", externoRef='" + externoRef + '\'' +
                ", observaciones='" + observaciones + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
