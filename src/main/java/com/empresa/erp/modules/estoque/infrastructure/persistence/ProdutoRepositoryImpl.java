package com.empresa.erp.modules.estoque.infrastructure.persistence;

import com.empresa.erp.modules.estoque.domain.entity.Produto;
import com.empresa.erp.modules.estoque.domain.repository.ProdutoRepository;
import com.empresa.erp.modules.estoque.infrastructure.persistence.jpa.ProdutoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
class ProdutoRepositoryImpl implements ProdutoRepository {

    private final ProdutoJpaRepository jpa;

    @Override
    public Produto salvar(Produto produto) {
        return jpa.save(produto);
    }

    @Override
    public Optional<Produto> buscarPorId(Long id) {
        return jpa.findById(id);
    }

    @Override
    public boolean existePorSku(String sku) {
        return jpa.existsBySku(sku);
    }

    @Override
    public Page<Produto> listar(Pageable pageable) {
        return jpa.findAll(pageable);
    }

    @Override
    public List<Produto> listarAbaixoDoMinimo() {
        return jpa.findAbaixoDoMinimo();
    }
}
