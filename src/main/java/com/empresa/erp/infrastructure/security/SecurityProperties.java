package com.empresa.erp.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(List<Usuario> usuarios) {

    public SecurityProperties {
        usuarios = usuarios == null ? List.of() : List.copyOf(usuarios);
    }

    public record Usuario(String username, String senha, List<String> perfis) {
    }
}
