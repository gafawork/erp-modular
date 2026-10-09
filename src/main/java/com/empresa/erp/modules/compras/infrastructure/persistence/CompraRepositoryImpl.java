package com.empresa.erp.modules.compras.infrastructure.persistence;

import com.empresa.erp.modules.compras.domain.entity.Compra;
import com.empresa.erp.modules.compras.domain.repository.CompraRepository;
import com.empresa.erp.modules.compras.infrastructure.persistence.jpa.CompraJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
class CompraRepositoryImpl implements CompraRepository {

    private final CompraJpaRepository jpa;

    @Override
    public Compra salvar(Compra compra) {
        return jpa.save(compra);
    }

    @Override
    public Optional<Compra> buscarPorId(Long id) {
        return jpa.findById(id);
    }

    @Override
    public Page<Compra> listar(Pageable pageable) {
        return jpa.findAll(pageable);
    }
}
