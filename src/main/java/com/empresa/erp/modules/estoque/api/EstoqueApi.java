package com.empresa.erp.modules.estoque.api;

import java.util.List;

/**
 * Contrato público do módulo Estoque. As operações de movimentação são atômicas:
 * ou todos os itens são processados, ou nenhum (participam da transação de quem chama).
 */
public interface EstoqueApi {

    /** @throws com.empresa.erp.shared.domain.exception.EntidadeNaoEncontradaException se não existir */
    ProdutoResumo buscarProduto(Long produtoId);

    /**
     * Baixa o estoque de uma venda. Valida o saldo de TODOS os itens antes de baixar qualquer um.
     * @throws EstoqueInsuficienteException se algum item não tiver saldo
     */
    void baixar(String documentoReferencia, List<ItemMovimento> itens);

    /** Devolve ao estoque itens de uma venda cancelada. */
    void estornar(String documentoReferencia, List<ItemMovimento> itens);

    /** Entrada por recebimento de compra; exige custo unitário (recalcula o custo médio). */
    void registrarEntradaDeCompra(String documentoReferencia, List<ItemMovimento> itens);
}
