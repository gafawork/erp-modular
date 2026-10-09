package com.empresa.erp.modules.clientes.application.service;

import com.empresa.erp.modules.clientes.api.ClienteResumo;
import com.empresa.erp.modules.clientes.api.ClientesApi;
import com.empresa.erp.modules.clientes.application.mapper.ClienteMapper;
import com.empresa.erp.modules.clientes.domain.repository.ClienteRepository;
import com.empresa.erp.shared.domain.exception.EntidadeNaoEncontradaException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementação da fachada pública. Package-private: outros módulos só enxergam a interface. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class ClientesApiImpl implements ClientesApi {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    @Override
    public ClienteResumo buscarCliente(Long clienteId) {
        return clienteRepository.buscarPorId(clienteId)
                .map(clienteMapper::toResumo)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Cliente", clienteId));
    }
}
