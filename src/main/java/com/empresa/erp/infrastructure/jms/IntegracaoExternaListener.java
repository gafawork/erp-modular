package com.empresa.erp.infrastructure.jms;

import jakarta.jms.JMSException;
import jakarta.jms.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * SIMULA um sistema externo (faturamento, NF-e, BI) consumindo as filas.
 * Recebe o JSON bruto, sem depender das classes dos módulos — como um consumidor real faria.
 * Em produção, este consumidor fica em outra aplicação e esta classe pode ser removida.
 */
@Slf4j
@Component
public class IntegracaoExternaListener {

    @JmsListener(destination = JmsConfig.FILA_VENDAS_CONFIRMADAS)
    public void onVendaConfirmada(TextMessage mensagem) throws JMSException {
        log.info("[EXTERNO] Venda confirmada recebida: {}", mensagem.getText());
    }

    @JmsListener(destination = JmsConfig.FILA_COMPRAS_RECEBIDAS)
    public void onCompraRecebida(TextMessage mensagem) throws JMSException {
        log.info("[EXTERNO] Compra recebida: {}", mensagem.getText());
    }
}
