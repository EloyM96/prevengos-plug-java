package com.prevengos.plug.gateway.contracts;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.Pact;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(PactConsumerTestExt.class)
class GatewayPlugApiPactTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Pact(provider = "plug-api", consumer = "prevengos-gateway")
    public RequestResponsePact patientLookupPact(PactDslWithProvider builder) {
        String body = "{" +
                "\"id\":\"7b1f\"," +
                "\"nif\":\"12345678Z\"," +
                "\"nombre\":\"María\"," +
                "\"apellidos\":\"García Pérez\"," +
                "\"email\":\"maria.garcia@example.com\"," +
                "\"telefono\":\"+34 600 000 001\"" +
                "}";
        return builder
                .given("paciente 7b1f existe")
                .uponReceiving("recupera un paciente existente")
                .path("/api/v1/patients/7b1f")
                .method("GET")
                .headers("Accept", "application/json")
                .willRespondWith()
                .status(200)
                .headers("Content-Type", "application/json; charset=utf-8")
                .body(body)
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "patientLookupPact")
    void gatewayClientCanDeserializePatientInformation(MockServer mockServer) throws Exception {
        URL url = new URL(mockServer.getUrl() + "/api/v1/patients/7b1f");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Accept", "application/json");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            JsonNode payload = objectMapper.readTree(builder.toString());

            assertEquals(200, connection.getResponseCode());
            assertEquals("7b1f", payload.get("id").asText());
            assertTrue(payload.get("email").asText().contains("@"));
        } finally {
            connection.disconnect();
        }
    }

    @Test
    void openapiContractDeclaresPatientLookupEndpoint() {
        SwaggerParseResult result = new OpenAPIV3Parser().readLocation("../../contracts/http/v1/plug-api.yaml", null, null);
        assertNotNull(result);
        assertNotNull(result.getOpenAPI(), "El contrato HTTP debe estar disponible");
        PathItem pathItem = result.getOpenAPI().getPaths().get("/api/v1/patients/{patientId}");
        assertNotNull(pathItem, "El contrato HTTP debe publicar GET /api/v1/patients/{patientId}");
        var getPatient = pathItem.getGet();
        assertNotNull(getPatient, "El contrato HTTP debe publicar GET /api/v1/patients/{patientId}");
        assertTrue(getPatient.getResponses().containsKey("200"));
    }
}
