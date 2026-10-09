package com.empresa.erp.modules.compras.domain.repository;

import com.empresa.erp.modules.compras.domain.entity.SugestaoReposicao;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SugestaoReposicaoRepository {

    SugestaoReposicao salvar(SugestaoReposicao sugestao);

    Optional<SugestaoReposicao> buscarPendentePorProduto(Long produtoId);

    List<SugestaoReposicao> listarPendentes();

    List<SugestaoReposicao> listarPendentesPorProdutos(Collection<Long> produtoIds);
}
