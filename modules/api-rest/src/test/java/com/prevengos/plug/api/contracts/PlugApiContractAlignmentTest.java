package com.prevengos.plug.api.contracts;

import au.com.dius.pact.core.model.Pact;
import au.com.dius.pact.core.model.PactReader;
import au.com.dius.pact.core.model.RequestResponseInteraction;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlugApiContractAlignmentTest {

    @Test
    void consumerPactIsConsistentWithPublishedOpenApiContract() {
        Path pactPath = Path.of("../../contracts/pacts/gateway-plug-api.json").normalize().toAbsolutePath();
        ParseOptions options = new ParseOptions();
        options.setResolve(true);
        SwaggerParseResult parseResult = new OpenAPIV3Parser().readLocation("../../contracts/http/v1/plug-api.yaml", null, options);
        assertNotNull(parseResult);
        assertNotNull(parseResult.getOpenAPI(), "El contrato OpenAPI no puede ser nulo");
        Pact pact = PactReader.loadPact(pactPath.toFile());

        List<RequestResponseInteraction> interactions = pact.getInteractions().stream()
                .filter(RequestResponseInteraction.class::isInstance)
                .map(RequestResponseInteraction.class::cast)
                .collect(Collectors.toList());
        assertTrue(!interactions.isEmpty(), "El pacto debe contener al menos una interacción request/response");

        interactions.forEach(interaction -> {
            String requestPath = interaction.getRequest().getPath();
            String matchingPath = parseResult.getOpenAPI().getPaths().keySet().stream()
                    .filter(candidate -> {
                        String regex = candidate.replaceAll("\\\\{[^/]+?\\\\}", "[^/]+");
                        Pattern pattern = Pattern.compile("^" + regex + "$", Pattern.CASE_INSENSITIVE);
                        return pattern.matcher(requestPath).matches();
                    })
                    .findFirst()
                    .orElse(null);

            assertNotNull(matchingPath, String.format("La ruta %s del pacto debe existir en el contrato OpenAPI", requestPath));

            String methodKey = interaction.getRequest().getMethod().toLowerCase();
            var operationsMap = Objects.requireNonNull(parseResult.getOpenAPI().getPaths().get(matchingPath)).readOperationsMap();
            var operation = operationsMap.get(methodKey);
            assertNotNull(operation, String.format("El método %s debe estar documentado para %s", interaction.getRequest().getMethod(), matchingPath));

            String responseStatus = Integer.toString(interaction.getResponse().getStatus());
            assertTrue(operation.getResponses().containsKey(responseStatus),
                    String.format("La respuesta %s debe estar documentada en el contrato OpenAPI", responseStatus));
        });

        assertEquals("plug-api", pact.getProvider().getName());
    }
}
