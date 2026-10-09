package com.empresa.erp.modules.compras.infrastructure.persistence;

import com.empresa.erp.modules.compras.domain.entity.Fornecedor;
import com.empresa.erp.modules.compras.domain.repository.FornecedorRepository;
import com.empresa.erp.modules.compras.infrastructure.persistence.jpa.FornecedorJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
class FornecedorRepositoryImpl implements FornecedorRepository {

    private final FornecedorJpaRepository jpa;

    @Override
    public Fornecedor salvar(Fornecedor fornecedor) {
        return jpa.save(fornecedor);
    }

    @Override
    public Optional<Fornecedor> buscarPorId(Long id) {
        return jpa.findById(id);
    }

    @Override
    public boolean existePorCnpj(String cnpj) {
        return jpa.existsByCnpj(cnpj);
    }

    @Override
    public Page<Fornecedor> listar(Pageable pageable) {
        return jpa.findAll(pageable);
    }
}
