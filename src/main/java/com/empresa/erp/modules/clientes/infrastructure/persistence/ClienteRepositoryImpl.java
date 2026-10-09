package com.empresa.erp.modules.clientes.infrastructure.persistence;

import com.empresa.erp.modules.clientes.domain.entity.Cliente;
import com.empresa.erp.modules.clientes.domain.repository.ClienteRepository;
import com.empresa.erp.modules.clientes.infrastructure.persistence.jpa.ClienteJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
class ClienteRepositoryImpl implements ClienteRepository {

    private final ClienteJpaRepository jpa;

    @Override
    public Cliente salvar(Cliente cliente) {
        return jpa.save(cliente);
    }

    @Override
    public Optional<Cliente> buscarPorId(Long id) {
        return jpa.findById(id);
    }

    @Override
    public boolean existePorCpf(String cpf) {
        return jpa.existsByCpf(cpf);
    }

    @Override
    public Page<Cliente> listar(Pageable pageable) {
        return jpa.findAll(pageable);
    }
}
