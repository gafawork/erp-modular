package com.empresa.erp.modules.estoque.domain.vo;

public enum TipoMovimentacao {
    ENTRADA_COMPRA(true),
    SAIDA_VENDA(false),
    ESTORNO_VENDA(true),
    AJUSTE_ENTRADA(true),
    AJUSTE_SAIDA(false);

    private final boolean entrada;

    TipoMovimentacao(boolean entrada) {
        this.entrada = entrada;
    }

    public boolean isEntrada() {
        return entrada;
    }
}
