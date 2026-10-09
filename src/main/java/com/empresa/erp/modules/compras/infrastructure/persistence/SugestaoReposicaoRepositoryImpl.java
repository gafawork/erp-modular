package com.empresa.erp.modules.compras.infrastructure.persistence;

import com.empresa.erp.modules.compras.domain.entity.SugestaoReposicao;
import com.empresa.erp.modules.compras.domain.repository.SugestaoReposicaoRepository;
import com.empresa.erp.modules.compras.domain.vo.StatusSugestao;
import com.empresa.erp.modules.compras.infrastructure.persistence.jpa.SugestaoReposicaoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
class SugestaoReposicaoRepositoryImpl implements SugestaoReposicaoRepository {

    private final SugestaoReposicaoJpaRepository jpa;

    @Override
    public SugestaoReposicao salvar(SugestaoReposicao sugestao) {
        return jpa.save(sugestao);
    }

    @Override
    public Optional<SugestaoReposicao> buscarPendentePorProduto(Long produtoId) {
        return jpa.findFirstByProdutoIdAndStatus(produtoId, StatusSugestao.PENDENTE);
    }

    @Override
    public List<SugestaoReposicao> listarPendentes() {
        return jpa.findByStatusOrderByDataUltimoAlertaDesc(StatusSugestao.PENDENTE);
    }

    @Override
    public List<SugestaoReposicao> listarPendentesPorProdutos(Collection<Long> produtoIds) {
        if (produtoIds.isEmpty()) {
            return List.of();
        }
        return jpa.findByProdutoIdInAndStatus(produtoIds, StatusSugestao.PENDENTE);
    }
}
