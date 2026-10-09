package com.empresa.erp.infrastructure.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Grava eventos externalizados no outbox, NA MESMA transação da operação de negócio. */
@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxMessageJpaRepository repository;
    private final ObjectMapper objectMapper;

    /** MANDATORY: o evento só existe se a operação de negócio for confirmada. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void registrar(String destino, Object evento) {
        try {
            String payload = objectMapper.writeValueAsString(evento);
            repository.save(OutboxMessage.nova(destino, evento.getClass().getName(), payload));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Falha ao serializar evento para o outbox: " + evento, e);
        }
    }
}
