package com.empresa.erp.modules.compras.infrastructure.persistence.jpa;

import com.empresa.erp.modules.compras.domain.entity.SugestaoReposicao;
import com.empresa.erp.modules.compras.domain.vo.StatusSugestao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SugestaoReposicaoJpaRepository extends JpaRepository<SugestaoReposicao, Long> {

    Optional<SugestaoReposicao> findFirstByProdutoIdAndStatus(Long produtoId, StatusSugestao status);

    List<SugestaoReposicao> findByStatusOrderByDataUltimoAlertaDesc(StatusSugestao status);

    List<SugestaoReposicao> findByProdutoIdInAndStatus(Collection<Long> produtoIds, StatusSugestao status);
}
