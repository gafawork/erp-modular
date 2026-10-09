package com.empresa.erp.modules.vendas.infrastructure.persistence;

import com.empresa.erp.modules.vendas.domain.entity.Venda;
import com.empresa.erp.modules.vendas.domain.repository.VendaRepository;
import com.empresa.erp.modules.vendas.infrastructure.persistence.jpa.VendaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
class VendaRepositoryImpl implements VendaRepository {

    private final VendaJpaRepository jpa;

    @Override
    public Venda salvar(Venda venda) {
        return jpa.save(venda);
    }

    @Override
    public Optional<Venda> buscarPorId(Long id) {
        return jpa.findById(id);
    }

    @Override
    public Page<Venda> listar(Pageable pageable) {
        return jpa.findAll(pageable);
    }

    @Override
    public Page<Venda> listarPorCliente(Long clienteId, Pageable pageable) {
        return jpa.findByClienteId(clienteId, pageable);
    }
}
