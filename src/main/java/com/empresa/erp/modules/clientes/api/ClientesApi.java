package com.empresa.erp.modules.clientes.api;

/** Contrato público do módulo Clientes para os demais módulos. */
public interface ClientesApi {

    /** @throws com.empresa.erp.shared.domain.exception.EntidadeNaoEncontradaException se não existir */
    ClienteResumo buscarCliente(Long clienteId);
}
