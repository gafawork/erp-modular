package com.empresa.erp.modules.estoque.events;

import java.time.Instant;

/** Publicado quando o saldo de um produto fica igual ou abaixo do estoque mínimo. */
public record EstoqueBaixoEvent(Long produtoId, String sku, int quantidadeAtual, int estoqueMinimo,
                                Instant ocorridoEm) {
}
