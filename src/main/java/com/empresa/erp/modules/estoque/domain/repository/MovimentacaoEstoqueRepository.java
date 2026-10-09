package com.empresa.erp.modules.estoque.domain.repository;

import com.empresa.erp.modules.estoque.domain.entity.MovimentacaoEstoque;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MovimentacaoEstoqueRepository {

    MovimentacaoEstoque salvar(MovimentacaoEstoque movimentacao);

    Page<MovimentacaoEstoque> listarPorProduto(Long produtoId, Pageable pageable);
}
