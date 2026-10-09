package com.empresa.erp.modules.vendas.application.service;

import com.empresa.erp.modules.clientes.api.ClienteResumo;
import com.empresa.erp.modules.clientes.api.ClientesApi;
import com.empresa.erp.modules.estoque.api.EstoqueApi;
import com.empresa.erp.modules.estoque.api.ProdutoResumo;
import com.empresa.erp.modules.vendas.application.dto.VendaDto;
import com.empresa.erp.modules.vendas.application.mapper.VendaMapper;
import com.empresa.erp.modules.vendas.domain.entity.Venda;
import com.empresa.erp.modules.vendas.domain.repository.VendaRepository;
import com.empresa.erp.modules.vendas.domain.vo.StatusVenda;
import com.empresa.erp.modules.vendas.events.VendaCanceladaEvent;
import com.empresa.erp.modules.vendas.events.VendaConfirmadaEvent;
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
public class VendaApplicationService {

    private final VendaRepository vendaRepository;
    private final ClientesApi clientesApi;            // ← API pública do módulo Clientes
    private final EstoqueApi estoqueApi;              // ← API pública do módulo Estoque
    private final DomainEventPublisher eventos;
    private final VendaMapper vendaMapper;

    @Transactional
    public VendaDto.Response criar(VendaDto.Request request) {
        ClienteResumo cliente = clientesApi.buscarCliente(request.clienteId());
        Venda venda = Venda.nova(cliente.id(), cliente.nome(), cliente.ativo());
        for (VendaDto.ItemRequest item : request.itens()) {
            ProdutoResumo produto = estoqueApi.buscarProduto(item.produtoId());
            venda.adicionarItem(produto.id(), produto.sku(), produto.nome(), produto.ativo(),
                    Dinheiro.of(produto.precoVenda()), item.quantidade());
        }
        return vendaMapper.toResponse(vendaRepository.salvar(venda));
    }

    /**
     * Confirmação + baixa de estoque na MESMA transação: se faltar estoque,
     * EstoqueInsuficienteException desfaz tudo e a venda continua PENDENTE.
     */
    @Transactional
    public VendaDto.Response confirmar(Long id) {
        Venda venda = obter(id);
        venda.confirmar();
        estoqueApi.baixar("VENDA-" + id, vendaMapper.toItensMovimento(venda));
        eventos.publicar(new VendaConfirmadaEvent(id, venda.getClienteId(), venda.getValorTotal().valor(),
                Instant.now()));
        return vendaMapper.toResponse(venda);
    }

    @Transactional
    public VendaDto.Response cancelar(Long id) {
        Venda venda = obter(id);
        boolean estornar = venda.cancelar() == StatusVenda.CONFIRMADA;
        if (estornar) {
            estoqueApi.estornar("VENDA-" + id, vendaMapper.toItensMovimento(venda));
        }
        eventos.publicar(new VendaCanceladaEvent(id, venda.getClienteId(), estornar, Instant.now()));
        return vendaMapper.toResponse(venda);
    }

    public VendaDto.Response buscar(Long id) {
        return vendaMapper.toResponse(obter(id));
    }

    public Page<VendaDto.Response> listar(Long clienteId, Pageable pageable) {
        Page<Venda> pagina = clienteId == null
                ? vendaRepository.listar(pageable)
                : vendaRepository.listarPorCliente(clienteId, pageable);
        return pagina.map(vendaMapper::toResponse);
    }

    private Venda obter(Long id) {
        return vendaRepository.buscarPorId(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Venda", id));
    }
}
