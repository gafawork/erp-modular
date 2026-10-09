package com.empresa.erp.modules.estoque.application.service;

import com.empresa.erp.modules.estoque.api.EstoqueApi;
import com.empresa.erp.modules.estoque.api.ItemMovimento;
import com.empresa.erp.modules.estoque.api.ProdutoResumo;
import com.empresa.erp.modules.estoque.application.mapper.ProdutoMapper;
import com.empresa.erp.modules.estoque.domain.entity.Produto;
import com.empresa.erp.modules.estoque.domain.repository.ProdutoRepository;
import com.empresa.erp.modules.estoque.domain.service.EstoqueDomainService;
import com.empresa.erp.modules.estoque.domain.service.EstoqueDomainService.ItemEstoque;
import com.empresa.erp.shared.domain.exception.EntidadeNaoEncontradaException;
import com.empresa.erp.shared.domain.vo.Dinheiro;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementação da fachada pública do Estoque (package-private).
 * Traduz os records da api para entidades do módulo e delega ao EstoqueDomainService.
 * Propagation.REQUIRED (padrão): participa da transação do módulo chamador.
 */
@Service
@Transactional
@RequiredArgsConstructor
class EstoqueApiImpl implements EstoqueApi {

    private final ProdutoRepository produtoRepository;
    private final EstoqueDomainService estoqueDomainService;
    private final ProdutoMapper produtoMapper;

    @Override
    @Transactional(readOnly = true)
    public ProdutoResumo buscarProduto(Long produtoId) {
        return produtoMapper.toResumo(obter(produtoId));
    }

    @Override
    public void baixar(String documentoReferencia, List<ItemMovimento> itens) {
        estoqueDomainService.baixar(documentoReferencia, resolver(itens));
    }

    @Override
    public void estornar(String documentoReferencia, List<ItemMovimento> itens) {
        estoqueDomainService.estornar(documentoReferencia, resolver(itens));
    }

    @Override
    public void registrarEntradaDeCompra(String documentoReferencia, List<ItemMovimento> itens) {
        estoqueDomainService.registrarEntradaDeCompra(documentoReferencia, resolver(itens));
    }

    private List<ItemEstoque> resolver(List<ItemMovimento> itens) {
        return itens.stream()
                .map(i -> new ItemEstoque(obter(i.produtoId()), i.quantidade(),
                        i.custoUnitario() == null ? null : Dinheiro.of(i.custoUnitario())))
                .toList();
    }

    private Produto obter(Long id) {
        return produtoRepository.buscarPorId(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Produto", id));
    }
}
