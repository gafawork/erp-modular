package com.empresa.erp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Monolito modular: os módulos de negócio ficam em {@code com.empresa.erp.modules.*}.
 * As fronteiras entre eles são verificadas pelo teste ArquiteturaModularTest (ArchUnit).
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class ErpApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErpApplication.class, args);
    }
}
