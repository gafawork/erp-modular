package com.empresa.erp.infrastructure.events;

import com.empresa.erp.infrastructure.outbox.OutboxService;
import com.empresa.erp.shared.event.DomainEventPublisher;
import com.empresa.erp.shared.event.Externalizado;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Implementação da porta DomainEventPublisher:
 * 1. se o evento for @Externalizado, grava no outbox (na transação corrente);
 * 2. publica em memória para os listeners dos outros módulos (@ListenerDeModulo).
 */
@Component
@RequiredArgsConstructor
public class SpringDomainEventPublisher implements DomainEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final OutboxService outboxService;

    @Override
    public void publicar(Object evento) {
        Externalizado externalizado = evento.getClass().getAnnotation(Externalizado.class);
        if (externalizado != null) {
            outboxService.registrar(externalizado.value(), evento);
        }
        applicationEventPublisher.publishEvent(evento);
    }
}
