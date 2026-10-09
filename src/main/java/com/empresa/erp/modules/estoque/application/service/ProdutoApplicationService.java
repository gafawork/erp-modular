package com.empresa.erp.modules.estoque.application.service;

import com.empresa.erp.modules.estoque.application.dto.EstoqueDto;
import com.empresa.erp.modules.estoque.application.dto.ProdutoDto;
import com.empresa.erp.modules.estoque.application.mapper.ProdutoMapper;
import com.empresa.erp.modules.estoque.domain.entity.MovimentacaoEstoque;
import com.empresa.erp.modules.estoque.domain.entity.Produto;
import com.empresa.erp.modules.estoque.domain.repository.MovimentacaoEstoqueRepository;
import com.empresa.erp.modules.estoque.domain.repository.ProdutoRepository;
import com.empresa.erp.modules.estoque.domain.service.EstoqueDomainService;
import com.empresa.erp.shared.domain.exception.DomainException;
import com.empresa.erp.shared.domain.exception.EntidadeNaoEncontradaException;
import com.empresa.erp.shared.domain.vo.Dinheiro;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Casos de uso expostos pela API REST do módulo Estoque. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProdutoApplicationService {

    private final ProdutoRepository produtoRepository;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;
    private final EstoqueDomainService estoqueDomainService;
    private final ProdutoMapper produtoMapper;

    @Transactional
    public ProdutoDto.Response criar(ProdutoDto.Request request) {
        Produto produto = produtoMapper.toDomain(request);
        if (produtoRepository.existePorSku(produto.getSku().valor())) {
            throw new DomainException("Já existe produto com o SKU " + produto.getSku().valor());
        }
        return produtoMapper.toResponse(produtoRepository.salvar(produto));
    }

    @Transactional
    public ProdutoDto.Response atualizar(Long id, ProdutoDto.AtualizacaoRequest request) {
        Produto produto = obter(id);
        produto.atualizar(request.nome(), request.descricao(), Dinheiro.of(request.precoVenda()),
                request.estoqueMinimo());
        return produtoMapper.toResponse(produto);
    }

    @Transactional
    public void inativar(Long id) {
        obter(id).inativar();
    }

    @Transactional
    public EstoqueDto.MovimentacaoResponse ajustarEstoque(Long id, EstoqueDto.AjusteRequest request) {
        Produto produto = obter(id);
        MovimentacaoEstoque mov = switch (request.tipo()) {
            case ENTRADA -> estoqueDomainService.ajustarEntrada(produto, request.quantidade(), request.motivo());
            case SAIDA -> estoqueDomainService.ajustarSaida(produto, request.quantidade(), request.motivo());
        };
        return produtoMapper.toResponse(mov);
    }

    public ProdutoDto.Response buscar(Long id) {
        return produtoMapper.toResponse(obter(id));
    }

    public Page<ProdutoDto.Response> listar(Pageable pageable) {
        return produtoRepository.listar(pageable).map(produtoMapper::toResponse);
    }

    public List<ProdutoDto.Response> listarAbaixoDoMinimo() {
        return produtoRepository.listarAbaixoDoMinimo().stream().map(produtoMapper::toResponse).toList();
    }

    public Page<EstoqueDto.MovimentacaoResponse> listarMovimentacoes(Long produtoId, Pageable pageable) {
        obter(produtoId);
        return movimentacaoRepository.listarPorProduto(produtoId, pageable).map(produtoMapper::toResponse);
    }

    private Produto obter(Long id) {
        return produtoRepository.buscarPorId(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Produto", id));
    }
}
