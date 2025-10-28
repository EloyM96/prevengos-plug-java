package com.prevengos.plug.hubbackend;

import com.prevengos.plug.hubbackend.config.PrlNotifierProperties;
import com.prevengos.plug.hubbackend.config.RrhhExportProperties;
import com.prevengos.plug.hubbackend.config.RrhhImportProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
        "com.prevengos.plug.hubbackend",
        "com.prevengos.plug.gateway"
})
@EnableScheduling
@EnableConfigurationProperties({RrhhExportProperties.class, RrhhImportProperties.class, PrlNotifierProperties.class})
public class HubBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(HubBackendApplication.class, args);
    }
}
