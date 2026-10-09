package com.empresa.erp.modules.compras.events;

import com.empresa.erp.shared.event.Externalizado;

import java.math.BigDecimal;
import java.time.Instant;

/** Mercadoria recebida. Também enviado à fila JMS erp.compras.recebidas (via outbox). */
@Externalizado("erp.compras.recebidas")
public record CompraRecebidaEvent(Long compraId, Long fornecedorId, BigDecimal valorTotal, Instant ocorridoEm) {
}
