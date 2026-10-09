/**
 * Módulo VENDAS — pedidos de venda.
 * <p>Público: {@code events} (VendaConfirmadaEvent externalizado para JMS, VendaCanceladaEvent).
 * <p>Depende de: clientes (ClientesApi) e estoque (EstoqueApi — consulta, baixa e estorno).
 */
@Modulo(nome = "Vendas", dependeDe = {"clientes", "estoque"})
package com.empresa.erp.modules.vendas;

import com.empresa.erp.shared.architecture.Modulo;
