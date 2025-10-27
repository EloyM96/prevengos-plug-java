package com.prevengos.plug.shared.contracts

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersionDetector
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

class EventEnvelopeSchemaTest {

    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    @Test
    fun `event envelope sample payload matches JSON schema`() {
        val schemaPath: Path = Path.of("../../contracts/json/v1/event-envelope.schema.json").normalize().toAbsolutePath()
        val schemaNode = objectMapper.readTree(Files.newBufferedReader(schemaPath))
        val schemaFactory = JsonSchemaFactory.getInstance(SpecVersionDetector.detect(schemaNode))
        val jsonSchema = schemaFactory.getSchema(schemaNode)

        val samplePayload = objectMapper.readTree(
            """
            {
              "version": "1.0.0",
              "event_type": "questionnaire-submitted",
              "id": "3d7d8f25-3b7b-4c4f-a5d0-ffaf756d7fba",
              "occurred_at": "2024-04-09T10:15:30Z",
              "payload": {
                "questionnaire_id": "screening-ergonomia",
                "patient_id": "7b1f",
                "score": 85
              }
            }
            """.trimIndent()
        )

        val validationMessages = jsonSchema.validate(samplePayload)
        assertTrue(validationMessages.isEmpty(), "El payload de ejemplo debe cumplir el contrato JSON: $validationMessages")
    }
}
