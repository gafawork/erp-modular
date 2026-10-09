package com.empresa.erp.infrastructure.outbox;

import jakarta.jms.TextMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Envia periodicamente as mensagens pendentes do outbox para as filas JMS.
 * Entrega "pelo menos uma vez": se a aplicação cair entre o envio e o commit, a mensagem
 * será reenviada — os consumidores externos devem ser idempotentes.
 * Com várias instâncias da aplicação, use um lock distribuído (ex.: ShedLock) neste método.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRelay {

    private final OutboxMessageJpaRepository repository;
    private final JmsTemplate jmsTemplate;

    @Value("${app.outbox.lote:100}")
    private int lote;

    @Scheduled(fixedDelayString = "${app.outbox.intervalo-ms:2000}")
    @Transactional
    public void enviarPendentes() {
        for (OutboxMessage mensagem : repository.findByEnviadoEmIsNullOrderByIdAsc(PageRequest.of(0, lote))) {
            try {
                jmsTemplate.send(mensagem.getDestino(), session -> {
                    TextMessage texto = session.createTextMessage(mensagem.getPayload());
                    texto.setStringProperty("_type", mensagem.getTipo());
                    texto.setLongProperty("outboxId", mensagem.getId());
                    return texto;
                });
                mensagem.marcarEnviada();
                log.debug("Outbox {} enviado para {}", mensagem.getId(), mensagem.getDestino());
            } catch (Exception e) {
                mensagem.registrarFalha(e.getMessage());
                log.warn("Falha ao enviar outbox {} para {}: {}", mensagem.getId(), mensagem.getDestino(), e.getMessage());
                break; // preserva a ordem: tenta de novo na próxima execução
            }
        }
    }
}
