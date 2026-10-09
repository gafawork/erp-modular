package com.empresa.erp.modules.compras.application.service;

import com.empresa.erp.modules.compras.application.dto.CompraDto;
import com.empresa.erp.modules.compras.application.mapper.CompraMapper;
import com.empresa.erp.modules.compras.domain.entity.SugestaoReposicao;
import com.empresa.erp.modules.compras.domain.repository.SugestaoReposicaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SugestaoReposicaoService {

    private final SugestaoReposicaoRepository repository;
    private final CompraMapper compraMapper;

    /** Idempotente: se já houver sugestão pendente para o produto, apenas atualiza o saldo. */
    @Transactional
    public void registrar(Long produtoId, String sku, int quantidadeAtual, int estoqueMinimo) {
        repository.buscarPendentePorProduto(produtoId)
                .ifPresentOrElse(
                        s -> s.atualizarSaldo(quantidadeAtual, estoqueMinimo),
                        () -> repository.salvar(SugestaoReposicao.nova(produtoId, sku, quantidadeAtual, estoqueMinimo)));
    }

    @Transactional
    public void marcarAtendidas(Collection<Long> produtoIds) {
        repository.listarPendentesPorProdutos(produtoIds).forEach(SugestaoReposicao::marcarAtendida);
    }

    public List<CompraDto.SugestaoResponse> listarPendentes() {
        return repository.listarPendentes().stream().map(compraMapper::toResponse).toList();
    }
}
