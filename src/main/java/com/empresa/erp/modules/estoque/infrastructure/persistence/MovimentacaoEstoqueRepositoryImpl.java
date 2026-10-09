package com.empresa.erp.modules.estoque.infrastructure.persistence;

import com.empresa.erp.modules.estoque.domain.entity.MovimentacaoEstoque;
import com.empresa.erp.modules.estoque.domain.repository.MovimentacaoEstoqueRepository;
import com.empresa.erp.modules.estoque.infrastructure.persistence.jpa.MovimentacaoEstoqueJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class MovimentacaoEstoqueRepositoryImpl implements MovimentacaoEstoqueRepository {

    private final MovimentacaoEstoqueJpaRepository jpa;

    @Override
    public MovimentacaoEstoque salvar(MovimentacaoEstoque movimentacao) {
        return jpa.save(movimentacao);
    }

    @Override
    public Page<MovimentacaoEstoque> listarPorProduto(Long produtoId, Pageable pageable) {
        return jpa.findByProdutoIdOrderByDataHoraDesc(produtoId, pageable);
    }
}
