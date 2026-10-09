package com.empresa.erp.modules.estoque.api;

import java.math.BigDecimal;

/** Projeção imutável de um produto, exposta a outros módulos. */
public record ProdutoResumo(Long id, String sku, String nome, BigDecimal precoVenda,
                            int quantidadeEmEstoque, boolean ativo) {
}
