/**
 * Módulo COMPRAS — fornecedores, pedidos de compra e sugestões de reposição.
 * <p>Público: {@code events} (CompraRecebidaEvent, externalizado para JMS).
 * <p>Depende de: estoque (EstoqueApi para consultar produtos e dar entrada;
 * EstoqueBaixoEvent para gerar sugestões de reposição).
 */
@Modulo(nome = "Compras", dependeDe = {"estoque"})
package com.empresa.erp.modules.compras;

import com.empresa.erp.shared.architecture.Modulo;
