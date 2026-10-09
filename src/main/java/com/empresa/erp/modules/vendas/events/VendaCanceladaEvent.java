package com.empresa.erp.modules.vendas.events;

import java.time.Instant;

/** Venda cancelada; estoqueEstornado indica se a venda já havia baixado estoque. */
public record VendaCanceladaEvent(Long vendaId, Long clienteId, boolean estoqueEstornado, Instant ocorridoEm) {
}
