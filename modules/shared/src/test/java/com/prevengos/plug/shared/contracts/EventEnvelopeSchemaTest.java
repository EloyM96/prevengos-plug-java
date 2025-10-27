package com.prevengos.plug.shared.contracts;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersionDetector;
import com.networknt.schema.ValidationMessage;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EventEnvelopeSchemaTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void eventEnvelopeSamplePayloadMatchesJsonSchema() throws IOException {
        Path schemaPath = Path.of("../../contracts/json/v1/event-envelope.schema.json").normalize().toAbsolutePath();
        JsonNode schemaNode = objectMapper.readTree(Files.newBufferedReader(schemaPath));
        JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersionDetector.detect(schemaNode));
        JsonSchema jsonSchema = schemaFactory.getSchema(schemaNode);

        String sample = "{" +
                "\"version\": \"1.0.0\"," +
                "\"event_type\": \"questionnaire-submitted\"," +
                "\"id\": \"3d7d8f25-3b7b-4c4f-a5d0-ffaf756d7fba\"," +
                "\"occurred_at\": \"2024-04-09T10:15:30Z\"," +
                "\"payload\": {" +
                "\"questionnaire_id\": \"screening-ergonomia\"," +
                "\"patient_id\": \"7b1f\"," +
                "\"score\": 85" +
                "}" +
                "}";
        JsonNode samplePayload = objectMapper.readTree(sample);

        Set<ValidationMessage> validationMessages = jsonSchema.validate(samplePayload);
        assertTrue(validationMessages.isEmpty(),
                "El payload de ejemplo debe cumplir el contrato JSON: " + validationMessages);
    }
}
