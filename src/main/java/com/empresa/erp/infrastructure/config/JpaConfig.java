package com.empresa.erp.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Auditoria JPA (criadoPor/atualizadoPor). Entidades e repositórios Spring Data são descobertos
 * a partir do pacote raiz, o que inclui todos os módulos e o outbox da plataforma.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaConfig {
}
