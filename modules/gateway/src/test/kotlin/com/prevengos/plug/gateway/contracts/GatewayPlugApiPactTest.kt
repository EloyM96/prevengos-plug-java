package com.prevengos.plug.gateway.contracts

import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.consumer.junit5.Pact
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt
import au.com.dius.pact.consumer.junit5.PactTestFor
import au.com.dius.pact.core.model.RequestResponsePact
import au.com.dius.pact.core.support.json.JsonValue
import au.com.dius.pact.core.support.json.JsonValue.Companion.`object`
import au.com.dius.pact.core.support.json.JsonValue.Companion.stringValue
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.swagger.v3.parser.OpenAPIV3Parser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.net.HttpURLConnection
import java.net.URL

@ExtendWith(PactConsumerTestExt::class)
class GatewayPlugApiPactTest {

    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    @Pact(provider = "plug-api", consumer = "prevengos-gateway")
    fun patientLookupPact(builder: PactDslWithProvider): RequestResponsePact = builder
        .given("paciente 7b1f existe")
        .uponReceiving("recupera un paciente existente")
        .path("/api/v1/patients/7b1f")
        .method("GET")
        .headers(mapOf("Accept" to "application/json"))
        .willRespondWith()
        .status(200)
        .headers(mapOf("Content-Type" to "application/json; charset=utf-8"))
        .body(samplePatientPayload())
        .toPact()

    @Test
    @PactTestFor(pactMethod = "patientLookupPact")
    fun `gateway client can deserialize patient information`(mockServer: au.com.dius.pact.consumer.MockServer) {
        val connection = URL(mockServer.getUrl() + "/api/v1/patients/7b1f").openConnection() as HttpURLConnection
        connection.setRequestProperty("Accept", "application/json")

        val response = connection.inputStream.reader().use { it.readText() }
        val payload = objectMapper.readTree(response)

        assertEquals(200, connection.responseCode)
        assertEquals("7b1f", payload["id"].asText())
        assertTrue(payload["email"].asText().contains("@"))
    }

    @Test
    fun `openapi contract declares patient lookup endpoint`() {
        val openApi = OpenAPIV3Parser().readLocation("../../contracts/http/v1/plug-api.yaml", null, null)
        val getPatient = openApi.openAPI.paths["/api/v1/patients/{patientId}"]?.get
        requireNotNull(getPatient) { "El contrato HTTP debe publicar GET /api/v1/patients/{patientId}" }
        assertTrue(getPatient.responses.containsKey("200"))
    }

    private fun samplePatientPayload(): JsonValue.Object = `object`(
        "id" to stringValue("7b1f"),
        "nif" to stringValue("12345678Z"),
        "nombre" to stringValue("María"),
        "apellidos" to stringValue("García Pérez"),
        "email" to stringValue("maria.garcia@example.com"),
        "telefono" to stringValue("+34 600 000 001")
    )
}
