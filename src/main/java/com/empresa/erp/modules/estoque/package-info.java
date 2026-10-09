/**
 * Módulo ESTOQUE — catálogo de produtos, saldo e kardex (movimentações).
 * <p>Público: {@code api} (EstoqueApi, ProdutoResumo, ItemMovimento, EstoqueInsuficienteException)
 * e {@code events} (EstoqueBaixoEvent).
 * <p>Não depende de nenhum outro módulo: quem precisa de estoque chama a EstoqueApi.
 */
@Modulo(nome = "Estoque")
package com.empresa.erp.modules.estoque;

import com.empresa.erp.shared.architecture.Modulo;
