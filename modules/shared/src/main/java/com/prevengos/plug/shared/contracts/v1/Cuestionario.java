package com.prevengos.plug.shared.contracts.v1;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.prevengos.plug.shared.csv.CsvRecord;
import com.prevengos.plug.shared.json.ContractJsonMapper;
import com.prevengos.plug.shared.time.ContractDateFormats;
import com.prevengos.plug.shared.validation.ContractValidationException;
import com.prevengos.plug.shared.validation.ContractValidator;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Shared representation of the {@code cuestionario.schema.json} contract.
 */
public final class Cuestionario {

    public static final List<String> CSV_HEADERS = List.of(
            "cuestionario_id",
            "paciente_id",
            "plantilla_codigo",
            "estado",
            "respuestas",
            "firmas",
            "adjuntos",
            "created_at",
            "updated_at"
    );

    private final UUID cuestionarioId;
    private final UUID pacienteId;
    private final String plantillaCodigo;
    private final Estado estado;
    private final List<Respuesta> respuestas;
    private final List<String> firmas;
    private final List<String> adjuntos;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;

    private Cuestionario(Builder builder) {
        this.cuestionarioId = ContractValidator.requireNonNull(builder.cuestionarioId, "cuestionario_id");
        this.pacienteId = ContractValidator.requireNonNull(builder.pacienteId, "paciente_id");
        this.plantillaCodigo = ContractValidator.requireNonBlank(ContractValidator.normalize(builder.plantillaCodigo), "plantilla_codigo");
        this.estado = builder.estado == null ? Estado.BORRADOR : builder.estado;
        List<Respuesta> respuestas = builder.respuestas;
        if (respuestas == null) {
            throw new ContractValidationException("respuestas must not be null");
        }
        this.respuestas = List.copyOf(respuestas);
        this.firmas = builder.firmas == null ? List.of() : List.copyOf(builder.firmas);
        this.adjuntos = builder.adjuntos == null ? List.of() : List.copyOf(builder.adjuntos);
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public UUID cuestionarioId() {
        return cuestionarioId;
    }

    public UUID pacienteId() {
        return pacienteId;
    }

    public String plantillaCodigo() {
        return plantillaCodigo;
    }

    public Estado estado() {
        return estado;
    }

    public List<Respuesta> respuestas() {
        return respuestas;
    }

    public List<String> firmas() {
        return firmas;
    }

    public List<String> adjuntos() {
        return adjuntos;
    }

    public Optional<OffsetDateTime> createdAt() {
        return Optional.ofNullable(createdAt);
    }

    public Optional<OffsetDateTime> updatedAt() {
        return Optional.ofNullable(updatedAt);
    }

    public CsvRecord toCsvRecord() {
        Map<String, String> ordered = new LinkedHashMap<>();
        ordered.put("cuestionario_id", cuestionarioId.toString());
        ordered.put("paciente_id", pacienteId.toString());
        ordered.put("plantilla_codigo", plantillaCodigo);
        ordered.put("estado", estado.code);
        ordered.put("respuestas", ContractJsonMapper.writeValue(toRespuestasArray()));
        ordered.put("firmas", firmas.isEmpty() ? null : ContractJsonMapper.writeValue(firmas));
        ordered.put("adjuntos", adjuntos.isEmpty() ? null : ContractJsonMapper.writeValue(adjuntos));
        ordered.put("created_at", ContractDateFormats.formatDateTime(createdAt));
        ordered.put("updated_at", ContractDateFormats.formatDateTime(updatedAt));
        return CsvRecord.of(ordered);
    }

    private ArrayNode toRespuestasArray() {
        ArrayNode arrayNode = ContractJsonMapper.mapper().createArrayNode();
        for (Respuesta respuesta : respuestas) {
            arrayNode.add(respuesta.toJson());
        }
        return arrayNode;
    }

    public static Cuestionario fromCsvRecord(CsvRecord record) {
        Builder builder = builder()
                .cuestionarioId(UUID.fromString(record.require("cuestionario_id")))
                .pacienteId(UUID.fromString(record.require("paciente_id")))
                .plantillaCodigo(record.require("plantilla_codigo"))
                .estado(Estado.fromCode(record.require("estado")));

        JsonNode respuestasNode = ContractJsonMapper.parseNode(record.require("respuestas"));
        if (respuestasNode == null) {
            respuestasNode = ContractJsonMapper.mapper().createArrayNode();
        }
        if (!respuestasNode.isArray()) {
            throw new ContractValidationException("respuestas must be a JSON array");
        }
        List<Respuesta> respuestas = new ArrayList<>();
        for (JsonNode node : respuestasNode) {
            respuestas.add(Respuesta.fromJson(node));
        }
        builder.respuestas(respuestas);

        record.optional("firmas").ifPresent(json -> builder.firmas(parseStringArray(json)));
        record.optional("adjuntos").ifPresent(json -> builder.adjuntos(parseStringArray(json)));
        record.optional("created_at").ifPresent(value -> builder.createdAt(ContractDateFormats.parseDateTime(value, "created_at")));
        record.optional("updated_at").ifPresent(value -> builder.updatedAt(ContractDateFormats.parseDateTime(value, "updated_at")));
        return builder.build();
    }

    private static List<String> parseStringArray(String json) {
        JsonNode node = ContractJsonMapper.parseNode(json);
        if (node == null) {
            return List.of();
        }
        if (!node.isArray()) {
            throw new ContractValidationException("Expected JSON array");
        }
        List<String> result = new ArrayList<>();
        for (JsonNode element : node) {
            if (!element.isTextual()) {
                throw new ContractValidationException("Array elements must be strings");
            }
            result.add(element.asText());
        }
        return List.copyOf(result);
    }

    public static Builder builder() {
        return new Builder();
    }

    public enum Estado {
        BORRADOR("borrador"),
        COMPLETADO("completado"),
        VALIDADO("validado");

        private final String code;

        Estado(String code) {
            this.code = code;
        }

        public static Estado fromCode(String code) {
            for (Estado estado : values()) {
                if (estado.code.equals(code)) {
                    return estado;
                }
            }
            throw new IllegalArgumentException("Unknown estado code: " + code);
        }

        public String code() {
            return code;
        }
    }

    public static final class Respuesta {
        private final String preguntaCodigo;
        private final JsonNode valor;
        private final String unidad;
        private final ObjectNode metadata;

        private Respuesta(Builder builder) {
            this.preguntaCodigo = ContractValidator.requireNonBlank(ContractValidator.normalize(builder.preguntaCodigo), "pregunta_codigo");
            this.valor = ContractValidator.requireNonNull(builder.valor, "valor");
            this.unidad = ContractValidator.normalize(builder.unidad);
            if (builder.metadata != null && !builder.metadata.isObject()) {
                throw new ContractValidationException("metadata must be a JSON object");
            }
            this.metadata = builder.metadata;
        }

        public String preguntaCodigo() {
            return preguntaCodigo;
        }

        public JsonNode valor() {
            return valor;
        }

        public Optional<String> unidad() {
            return Optional.ofNullable(unidad);
        }

        public Optional<ObjectNode> metadata() {
            return Optional.ofNullable(metadata);
        }

        private ObjectNode toJson() {
            ObjectNode node = ContractJsonMapper.mapper().createObjectNode();
            node.put("pregunta_codigo", preguntaCodigo);
            node.set("valor", valor);
            if (unidad != null) {
                node.put("unidad", unidad);
            }
            if (metadata != null) {
                node.set("metadata", metadata);
            }
            return node;
        }

        public static Respuesta fromJson(JsonNode node) {
            if (node == null || !node.isObject()) {
                throw new ContractValidationException("Each respuesta must be a JSON object");
            }
            JsonNode preguntaCodigo = node.get("pregunta_codigo");
            JsonNode valor = node.get("valor");
            Builder builder = builder()
                    .preguntaCodigo(preguntaCodigo != null ? preguntaCodigo.asText(null) : null)
                    .valor(valor);
            JsonNode unidadNode = node.get("unidad");
            if (unidadNode != null && unidadNode.isTextual()) {
                builder.unidad(unidadNode.asText());
            }
            JsonNode metadataNode = node.get("metadata");
            if (metadataNode != null) {
                if (!metadataNode.isObject()) {
                    throw new ContractValidationException("metadata must be a JSON object");
                }
                builder.metadata((ObjectNode) metadataNode);
            }
            return builder.build();
        }

        public static Builder builder() {
            return new Builder();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Respuesta respuesta)) {
                return false;
            }
            return Objects.equals(preguntaCodigo, respuesta.preguntaCodigo) && Objects.equals(valor, respuesta.valor) && Objects.equals(unidad, respuesta.unidad) && Objects.equals(metadata, respuesta.metadata);
        }

        @Override
        public int hashCode() {
            return Objects.hash(preguntaCodigo, valor, unidad, metadata);
        }

        @Override
        public String toString() {
            return "Respuesta{" +
                    "preguntaCodigo='" + preguntaCodigo + '\'' +
                    ", valor=" + valor +
                    ", unidad='" + unidad + '\'' +
                    ", metadata=" + metadata +
                    '}';
        }

        public static final class Builder {
            private String preguntaCodigo;
            private JsonNode valor;
            private String unidad;
            private ObjectNode metadata;

            private Builder() {
            }

            public Builder preguntaCodigo(String preguntaCodigo) {
                this.preguntaCodigo = preguntaCodigo;
                return this;
            }

            public Builder valor(JsonNode valor) {
                this.valor = valor;
                return this;
            }

            public Builder valorFrom(Object value) {
                this.valor = ContractJsonMapper.mapper().valueToTree(value);
                return this;
            }

            public Builder unidad(String unidad) {
                this.unidad = unidad;
                return this;
            }

            public Builder metadata(ObjectNode metadata) {
                this.metadata = metadata;
                return this;
            }

            public Respuesta build() {
                return new Respuesta(this);
            }
        }
    }

    public static final class Builder {
        private UUID cuestionarioId;
        private UUID pacienteId;
        private String plantillaCodigo;
        private Estado estado;
        private List<Respuesta> respuestas = new ArrayList<>();
        private List<String> firmas = new ArrayList<>();
        private List<String> adjuntos = new ArrayList<>();
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;

        private Builder() {
        }

        public Builder cuestionarioId(UUID cuestionarioId) {
            this.cuestionarioId = cuestionarioId;
            return this;
        }

        public Builder pacienteId(UUID pacienteId) {
            this.pacienteId = pacienteId;
            return this;
        }

        public Builder plantillaCodigo(String plantillaCodigo) {
            this.plantillaCodigo = plantillaCodigo;
            return this;
        }

        public Builder estado(Estado estado) {
            this.estado = estado;
            return this;
        }

        public Builder respuestas(List<Respuesta> respuestas) {
            this.respuestas = new ArrayList<>(respuestas);
            return this;
        }

        public Builder addRespuesta(Respuesta respuesta) {
            this.respuestas.add(respuesta);
            return this;
        }

        public Builder firmas(List<String> firmas) {
            this.firmas = new ArrayList<>(firmas);
            return this;
        }

        public Builder addFirma(String firmaBase64) {
            this.firmas.add(firmaBase64);
            return this;
        }

        public Builder adjuntos(List<String> adjuntos) {
            this.adjuntos = new ArrayList<>(adjuntos);
            return this;
        }

        public Builder addAdjunto(String adjuntoBase64) {
            this.adjuntos.add(adjuntoBase64);
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

        public Cuestionario build() {
            return new Cuestionario(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Cuestionario that)) {
            return false;
        }
        return Objects.equals(cuestionarioId, that.cuestionarioId) && Objects.equals(pacienteId, that.pacienteId) && Objects.equals(plantillaCodigo, that.plantillaCodigo) && estado == that.estado && Objects.equals(respuestas, that.respuestas) && Objects.equals(firmas, that.firmas) && Objects.equals(adjuntos, that.adjuntos) && Objects.equals(createdAt, that.createdAt) && Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cuestionarioId, pacienteId, plantillaCodigo, estado, respuestas, firmas, adjuntos, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "Cuestionario{" +
                "cuestionarioId=" + cuestionarioId +
                ", pacienteId=" + pacienteId +
                ", plantillaCodigo='" + plantillaCodigo + '\'' +
                ", estado=" + estado +
                ", respuestas=" + respuestas +
                ", firmas=" + firmas +
                ", adjuntos=" + adjuntos +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
