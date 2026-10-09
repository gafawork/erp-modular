package com.empresa.erp.modules.estoque.infrastructure.persistence.jpa;

import com.empresa.erp.modules.estoque.domain.entity.MovimentacaoEstoque;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimentacaoEstoqueJpaRepository extends JpaRepository<MovimentacaoEstoque, Long> {

    @EntityGraph(attributePaths = "produto")
    Page<MovimentacaoEstoque> findByProdutoIdOrderByDataHoraDesc(Long produtoId, Pageable pageable);
}
