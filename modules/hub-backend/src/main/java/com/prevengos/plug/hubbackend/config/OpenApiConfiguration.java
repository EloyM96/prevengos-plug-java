package com.prevengos.plug.hubbackend.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI hubBackendOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Prevengos Hub Backend API")
                        .version("v1")
                        .description("API para sincronización de pacientes y cuestionarios con Prevengos")
                        .contact(new Contact().name("Prevengos").url("https://prevengos.com")))
                .externalDocs(new ExternalDocumentation()
                        .description("Contratos JSON")
                        .url("https://github.com/Prevengos/prevengos-plug-java/tree/main/contracts"));
    }
}
