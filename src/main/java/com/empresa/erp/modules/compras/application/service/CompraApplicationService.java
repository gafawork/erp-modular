package com.empresa.erp.modules.compras.application.service;

import com.empresa.erp.modules.compras.application.dto.CompraDto;
import com.empresa.erp.modules.compras.application.mapper.CompraMapper;
import com.empresa.erp.modules.compras.domain.entity.Compra;
import com.empresa.erp.modules.compras.domain.entity.Fornecedor;
import com.empresa.erp.modules.compras.domain.entity.ItemCompra;
import com.empresa.erp.modules.compras.domain.repository.CompraRepository;
import com.empresa.erp.modules.compras.domain.repository.FornecedorRepository;
import com.empresa.erp.modules.compras.events.CompraRecebidaEvent;
import com.empresa.erp.modules.estoque.api.EstoqueApi;
import com.empresa.erp.modules.estoque.api.ProdutoResumo;
import com.empresa.erp.shared.domain.exception.EntidadeNaoEncontradaException;
import com.empresa.erp.shared.domain.vo.Dinheiro;
import com.empresa.erp.shared.event.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompraApplicationService {

    private final CompraRepository compraRepository;
    private final FornecedorRepository fornecedorRepository;
    private final SugestaoReposicaoService sugestaoService;
    private final EstoqueApi estoqueApi;              // ← API pública de outro módulo
    private final DomainEventPublisher eventos;
    private final CompraMapper compraMapper;

    @Transactional
    public CompraDto.Response criar(CompraDto.Request request) {
        Fornecedor fornecedor = fornecedorRepository.buscarPorId(request.fornecedorId())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Fornecedor", request.fornecedorId()));
        Compra compra = Compra.nova(fornecedor);
        for (CompraDto.ItemRequest item : request.itens()) {
            ProdutoResumo produto = estoqueApi.buscarProduto(item.produtoId());
            compra.adicionarItem(produto.id(), produto.sku(), produto.nome(), produto.ativo(),
                    item.quantidade(), Dinheiro.of(item.custoUnitario()));
        }
        return compraMapper.toResponse(compraRepository.salvar(compra));
    }

    /**
     * Recebimento: tudo na mesma transação (consistência imediata):
     * status da compra + entrada no estoque (outro módulo) + sugestões atendidas + evento (outbox).
     */
    @Transactional
    public CompraDto.Response receber(Long id) {
        Compra compra = obter(id);
        compra.marcarComoRecebida();
        estoqueApi.registrarEntradaDeCompra("COMPRA-" + id, compraMapper.toItensMovimento(compra));
        sugestaoService.marcarAtendidas(compra.getItens().stream().map(ItemCompra::getProdutoId).toList());
        eventos.publicar(new CompraRecebidaEvent(id, compra.getFornecedor().getId(),
                compra.getValorTotal().valor(), Instant.now()));
        return compraMapper.toResponse(compra);
    }

    @Transactional
    public CompraDto.Response cancelar(Long id) {
        Compra compra = obter(id);
        compra.cancelar();
        return compraMapper.toResponse(compra);
    }

    public CompraDto.Response buscar(Long id) {
        return compraMapper.toResponse(obter(id));
    }

    public Page<CompraDto.Response> listar(Pageable pageable) {
        return compraRepository.listar(pageable).map(compraMapper::toResponse);
    }

    private Compra obter(Long id) {
        return compraRepository.buscarPorId(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Compra", id));
    }
}
