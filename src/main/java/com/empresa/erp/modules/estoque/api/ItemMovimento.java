package com.empresa.erp.modules.estoque.api;

import java.math.BigDecimal;

/** Comando de movimentação: produto, quantidade e (somente para compras) custo unitário. */
public record ItemMovimento(Long produtoId, int quantidade, BigDecimal custoUnitario) {

    public static ItemMovimento semCusto(Long produtoId, int quantidade) {
        return new ItemMovimento(produtoId, quantidade, null);
    }

    public static ItemMovimento comCusto(Long produtoId, int quantidade, BigDecimal custoUnitario) {
        return new ItemMovimento(produtoId, quantidade, custoUnitario);
    }
}
