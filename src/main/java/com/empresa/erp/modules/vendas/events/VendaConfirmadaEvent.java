package com.empresa.erp.modules.vendas.events;

import com.empresa.erp.shared.event.Externalizado;

import java.math.BigDecimal;
import java.time.Instant;

/** Venda confirmada. Também enviado à fila JMS erp.vendas.confirmadas (via outbox). */
@Externalizado("erp.vendas.confirmadas")
public record VendaConfirmadaEvent(Long vendaId, Long clienteId, BigDecimal valorTotal, Instant ocorridoEm) {
}
