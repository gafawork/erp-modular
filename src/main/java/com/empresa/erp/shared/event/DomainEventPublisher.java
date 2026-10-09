package com.empresa.erp.shared.event;

/**
 * Porta de publicação de eventos usada pelos módulos.
 * Implementação: infrastructure.events.SpringDomainEventPublisher
 * (eventos em memória + outbox para eventos anotados com {@link Externalizado}).
 */
public interface DomainEventPublisher {

    void publicar(Object evento);
}
