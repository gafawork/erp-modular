package com.empresa.erp.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Swagger com um grupo por módulo (seletor no canto superior direito da UI). */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI erpOpenApi() {
        return new OpenAPI()
                .info(new Info().title("ERP Modular").version("v1"))
                .components(new Components().addSecuritySchemes("basicAuth",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic")))
                .addSecurityItem(new SecurityRequirement().addList("basicAuth"));
    }

    @Bean
    public GroupedOpenApi moduloClientes() {
        return grupo("clientes", "/api/v1/clientes/**");
    }

    @Bean
    public GroupedOpenApi moduloEstoque() {
        return grupo("estoque", "/api/v1/produtos/**");
    }

    @Bean
    public GroupedOpenApi moduloCompras() {
        return grupo("compras", "/api/v1/fornecedores/**", "/api/v1/compras/**");
    }

    @Bean
    public GroupedOpenApi moduloVendas() {
        return grupo("vendas", "/api/v1/vendas/**");
    }

    private static GroupedOpenApi grupo(String nome, String... caminhos) {
        return GroupedOpenApi.builder().group(nome).pathsToMatch(caminhos).build();
    }
}
