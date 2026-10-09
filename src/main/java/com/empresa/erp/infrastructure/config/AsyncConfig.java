package com.empresa.erp.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * - @EnableAsync: listeners de eventos entre módulos (@ListenerDeModulo) rodam fora da thread da requisição.
 *   O executor é o auto-configurado pelo Spring Boot (virtual threads, ver spring.threads.virtual.enabled).
 * - @EnableScheduling: execução periódica do OutboxRelay.
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {
}
