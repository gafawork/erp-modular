package com.empresa.erp.infrastructure.jms;

import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;

/**
 * JMS é usado apenas para integração com sistemas EXTERNOS (via outbox).
 * Entre os módulos do monolito, os eventos trafegam em memória (ApplicationEventPublisher).
 * Conexão: Artemis embarcado (spring.artemis.*); em produção, aponte para um broker externo.
 */
@EnableJms
@Configuration
public class JmsConfig {

    public static final String FILA_VENDAS_CONFIRMADAS = "erp.vendas.confirmadas";
    public static final String FILA_COMPRAS_RECEBIDAS = "erp.compras.recebidas";
}
