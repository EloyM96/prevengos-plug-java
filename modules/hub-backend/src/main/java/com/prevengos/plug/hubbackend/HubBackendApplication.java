package com.prevengos.plug.hubbackend;

import com.prevengos.plug.hubbackend.config.RrhhExportProperties;
import com.prevengos.plug.hubbackend.config.SqlServerProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({SqlServerProperties.class, RrhhExportProperties.class})
public class HubBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(HubBackendApplication.class, args);
    }
}
