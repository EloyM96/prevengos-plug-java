package com.prevengos.plug.shared.contracts.v1;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.prevengos.plug.shared.csv.CsvRecord;
import com.prevengos.plug.shared.json.ContractJsonMapper;
import com.prevengos.plug.shared.time.ContractDateFormats;
import com.prevengos.plug.shared.validation.ContractValidationException;
import com.prevengos.plug.shared.validation.ContractValidator;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Shared representation of the {@code event-envelope.schema.json} contract.
 */
public final class EventEnvelope {

    private static final Pattern EVENT_TYPE_PATTERN = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");
    public static final List<String> CSV_HEADERS = List.of(
            "event_id",
            "event_type",
            "version",
            "occurred_at",
            "source",
            "correlation_id",
            "causation_id",
            "payload",
            "metadata"
    );

    private final UUID eventId;
    private final String eventType;
    private final int version;
    private final OffsetDateTime occurredAt;
    private final String source;
    private final UUID correlationId;
    private final UUID causationId;
    private final JsonNode payload;
    private final ObjectNode metadata;

    private EventEnvelope(Builder builder) {
        this.eventId = ContractValidator.requireNonNull(builder.eventId, "event_id");
        String type = ContractValidator.requireNonBlank(ContractValidator.normalize(builder.eventType), "event_type");
        if (!EVENT_TYPE_PATTERN.matcher(type).matches()) {
            throw new ContractValidationException("event_type must match pattern " + EVENT_TYPE_PATTERN);
        }
        this.eventType = type;
        if (builder.version < 1) {
            throw new ContractValidationException("version must be greater or equal to 1");
        }
        this.version = builder.version;
        this.occurredAt = ContractValidator.requireNonNull(builder.occurredAt, "occurred_at");
        this.source = ContractValidator.requireNonBlank(ContractValidator.normalize(builder.source), "source");
        this.correlationId = builder.correlationId;
        this.causationId = builder.causationId;
        this.payload = ContractValidator.requireNonNull(builder.payload, "payload");
        if (builder.metadata != null && !builder.metadata.isObject()) {
            throw new ContractValidationException("metadata must be a JSON object");
        }
        this.metadata = builder.metadata;
    }

    public UUID eventId() {
        return eventId;
    }

    public String eventType() {
        return eventType;
    }

    public int version() {
        return version;
    }

    public OffsetDateTime occurredAt() {
        return occurredAt;
    }

    public String source() {
        return source;
    }

    public Optional<UUID> correlationId() {
        return Optional.ofNullable(correlationId);
    }

    public Optional<UUID> causationId() {
        return Optional.ofNullable(causationId);
    }

    public JsonNode payload() {
        return payload;
    }

    public Optional<ObjectNode> metadata() {
        return Optional.ofNullable(metadata);
    }

    public CsvRecord toCsvRecord() {
        Map<String, String> ordered = new LinkedHashMap<>();
        ordered.put("event_id", eventId.toString());
        ordered.put("event_type", eventType);
        ordered.put("version", Integer.toString(version));
        ordered.put("occurred_at", ContractDateFormats.formatDateTime(occurredAt));
        ordered.put("source", source);
        ordered.put("correlation_id", correlationId != null ? correlationId.toString() : null);
        ordered.put("causation_id", causationId != null ? causationId.toString() : null);
        ordered.put("payload", ContractJsonMapper.writeValue(payload));
        ordered.put("metadata", metadata == null ? null : ContractJsonMapper.writeValue(metadata));
        return CsvRecord.of(ordered);
    }

    public static EventEnvelope fromCsvRecord(CsvRecord record) {
        Builder builder = builder()
                .eventId(UUID.fromString(record.require("event_id")))
                .eventType(record.require("event_type"))
                .version(parseVersion(record.require("version")))
                .occurredAt(ContractDateFormats.parseDateTime(record.require("occurred_at"), "occurred_at"))
                .source(record.require("source"))
                .payload(parseJsonNode(record.require("payload")));

        record.optional("correlation_id").ifPresent(value -> builder.correlationId(UUID.fromString(value)));
        record.optional("causation_id").ifPresent(value -> builder.causationId(UUID.fromString(value)));
        record.optional("metadata").ifPresent(json -> builder.metadata(parseObjectNode(json)));
        return builder.build();
    }

    private static int parseVersion(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new ContractValidationException("version must be an integer", ex);
        }
    }

    private static JsonNode parseJsonNode(String json) {
        JsonNode node = ContractJsonMapper.parseNode(json);
        if (node == null) {
            throw new ContractValidationException("payload must not be empty");
        }
        return node;
    }

    private static ObjectNode parseObjectNode(String json) {
        JsonNode node = ContractJsonMapper.parseNode(json);
        if (node == null) {
            return null;
        }
        if (!node.isObject()) {
            throw new ContractValidationException("metadata must be a JSON object");
        }
        return (ObjectNode) node;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private UUID eventId;
        private String eventType;
        private int version = 1;
        private OffsetDateTime occurredAt;
        private String source;
        private UUID correlationId;
        private UUID causationId;
        private JsonNode payload;
        private ObjectNode metadata;

        private Builder() {
        }

        public Builder eventId(UUID eventId) {
            this.eventId = eventId;
            return this;
        }

        public Builder eventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder version(int version) {
            this.version = version;
            return this;
        }

        public Builder occurredAt(OffsetDateTime occurredAt) {
            this.occurredAt = occurredAt;
            return this;
        }

        public Builder source(String source) {
            this.source = source;
            return this;
        }

        public Builder correlationId(UUID correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public Builder causationId(UUID causationId) {
            this.causationId = causationId;
            return this;
        }

        public Builder payload(JsonNode payload) {
            this.payload = payload;
            return this;
        }

        public Builder payloadFrom(Object value) {
            this.payload = ContractJsonMapper.mapper().valueToTree(value);
            return this;
        }

        public Builder metadata(ObjectNode metadata) {
            this.metadata = metadata;
            return this;
        }

        public EventEnvelope build() {
            return new EventEnvelope(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EventEnvelope that)) {
            return false;
        }
        return version == that.version && Objects.equals(eventId, that.eventId) && Objects.equals(eventType, that.eventType) && Objects.equals(occurredAt, that.occurredAt) && Objects.equals(source, that.source) && Objects.equals(correlationId, that.correlationId) && Objects.equals(causationId, that.causationId) && Objects.equals(payload, that.payload) && Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, eventType, version, occurredAt, source, correlationId, causationId, payload, metadata);
    }

    @Override
    public String toString() {
        return "EventEnvelope{" +
                "eventId=" + eventId +
                ", eventType='" + eventType + '\'' +
                ", version=" + version +
                ", occurredAt=" + occurredAt +
                ", source='" + source + '\'' +
                ", correlationId=" + correlationId +
                ", causationId=" + causationId +
                ", payload=" + payload +
                ", metadata=" + metadata +
                '}';
    }
}
