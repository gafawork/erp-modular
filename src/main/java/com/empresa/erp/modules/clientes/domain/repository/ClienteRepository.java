package com.empresa.erp.modules.clientes.domain.repository;

import com.empresa.erp.modules.clientes.domain.entity.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/** Porta de persistência do módulo; implementada em clientes.infrastructure.persistence. */
public interface ClienteRepository {

    Cliente salvar(Cliente cliente);

    Optional<Cliente> buscarPorId(Long id);

    boolean existePorCpf(String cpf);

    Page<Cliente> listar(Pageable pageable);
}
