package com.prevengos.plug.shared.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.prevengos.plug.shared.validation.ContractValidationException;

/**
 * Centralised access to the Jackson {@link ObjectMapper} configuration used to
 * serialise and deserialise contract payloads.
 */
public final class ContractJsonMapper {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private ContractJsonMapper() {
    }

    public static ObjectMapper mapper() {
        return MAPPER;
    }

    public static JsonNode parseNode(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return MAPPER.readTree(json);
        } catch (JsonProcessingException e) {
            throw new ContractValidationException("Unable to parse JSON content", e);
        }
    }

    public static String writeJson(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw new ContractValidationException("Unable to serialise JSON content", e);
        }
    }

    public static String writeValue(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new ContractValidationException("Unable to serialise value", e);
        }
    }
}
