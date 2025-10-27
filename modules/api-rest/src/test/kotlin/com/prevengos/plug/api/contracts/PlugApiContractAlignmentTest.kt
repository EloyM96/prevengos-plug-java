package com.prevengos.plug.api.contracts

import au.com.dius.pact.core.model.PactReader
import au.com.dius.pact.core.model.RequestResponseInteraction
import io.swagger.v3.parser.OpenAPIV3Parser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Path

class PlugApiContractAlignmentTest {

    @Test
    fun `consumer pact is consistent with published OpenAPI contract`() {
        val pactPath = Path.of("../../contracts/pacts/gateway-plug-api.json").normalize().toAbsolutePath()
        val openApi = OpenAPIV3Parser().readLocation("../../contracts/http/v1/plug-api.yaml", null, null).openAPI
        val pact = PactReader.loadPact(pactPath.toFile())

        val interactions = pact.interactions.filterIsInstance<RequestResponseInteraction>()
        assertTrue(interactions.isNotEmpty(), "El pacto debe contener al menos una interacción request/response")

        interactions.forEach { interaction ->
            val requestPath = interaction.request.path
            val matchingPath = openApi.paths.keys.firstOrNull { candidate ->
                val regex = candidate.replace(Regex("\\{[^/]+?}"), "[^/]+")
                requestPath.matches(Regex("^$regex$"))
            }

            assertNotNull(
                matchingPath,
                "La ruta $requestPath del pacto debe existir en el contrato OpenAPI"
            )

            val methodKey = interaction.request.method.lowercase()
            val operation = openApi.paths[matchingPath]?.readOperationsMap()?.get(methodKey)
            assertNotNull(
                operation,
                "El método ${interaction.request.method} debe estar documentado para $matchingPath"
            )

            val responseStatus = interaction.response.status.toString()
            assertTrue(
                operation!!.responses.containsKey(responseStatus),
                "La respuesta $responseStatus debe estar documentada en el contrato OpenAPI"
            )
        }

        assertEquals("plug-api", pact.provider.name)
    }
}
