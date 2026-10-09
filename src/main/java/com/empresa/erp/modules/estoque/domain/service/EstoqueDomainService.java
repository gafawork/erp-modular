package com.empresa.erp.modules.estoque.domain.service;

import com.empresa.erp.modules.estoque.api.EstoqueInsuficienteException;
import com.empresa.erp.modules.estoque.domain.entity.MovimentacaoEstoque;
import com.empresa.erp.modules.estoque.domain.entity.Produto;
import com.empresa.erp.modules.estoque.domain.repository.MovimentacaoEstoqueRepository;
import com.empresa.erp.modules.estoque.domain.vo.TipoMovimentacao;
import com.empresa.erp.modules.estoque.events.EstoqueBaixoEvent;
import com.empresa.erp.shared.domain.exception.DomainException;
import com.empresa.erp.shared.domain.vo.Dinheiro;
import com.empresa.erp.shared.event.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Regras de movimentação que envolvem Produto + MovimentacaoEstoque.
 * Não conhece outros módulos: recebe produtos já carregados (ver EstoqueApiImpl).
 */
@Service
@RequiredArgsConstructor
public class EstoqueDomainService {

    private final MovimentacaoEstoqueRepository movimentacaoRepository;
    private final DomainEventPublisher eventos;

    /** Item a movimentar, já resolvido para a entidade do módulo. */
    public record ItemEstoque(Produto produto, int quantidade, Dinheiro custoUnitario) {
    }

    public void baixar(String referencia, List<ItemEstoque> itens) {
        // 1. valida o saldo de TODOS (somando itens repetidos do mesmo produto) antes de alterar qualquer um
        Map<Produto, Integer> totais = new LinkedHashMap<>();
        itens.forEach(i -> totais.merge(i.produto(), i.quantidade(), Integer::sum));
        totais.forEach((produto, quantidade) -> {
            produto.exigirAtivo();
            if (!produto.possuiEstoque(quantidade)) {
                throw new EstoqueInsuficienteException(
                        produto.getSku().valor(), produto.getQuantidadeEmEstoque(), quantidade);
            }
        });
        // 2. baixa e registra
        for (ItemEstoque item : itens) {
            item.produto().darBaixa(item.quantidade());
            registrar(item.produto(), TipoMovimentacao.SAIDA_VENDA, item.quantidade(), referencia, "Venda confirmada");
        }
        totais.keySet().forEach(this::verificarEstoqueMinimo);
    }

    public void estornar(String referencia, List<ItemEstoque> itens) {
        for (ItemEstoque item : itens) {
            item.produto().darEntrada(item.quantidade());
            registrar(item.produto(), TipoMovimentacao.ESTORNO_VENDA, item.quantidade(), referencia,
                    "Cancelamento de venda");
        }
    }

    public void registrarEntradaDeCompra(String referencia, List<ItemEstoque> itens) {
        for (ItemEstoque item : itens) {
            if (item.custoUnitario() == null) {
                throw new DomainException("Entrada de compra exige custo unitário (produto %s)"
                        .formatted(item.produto().getSku().valor()));
            }
            item.produto().registrarEntradaComCusto(item.quantidade(), item.custoUnitario());
            registrar(item.produto(), TipoMovimentacao.ENTRADA_COMPRA, item.quantidade(), referencia,
                    "Recebimento de compra");
        }
    }

    public MovimentacaoEstoque ajustarEntrada(Produto produto, int quantidade, String motivo) {
        produto.darEntrada(quantidade);
        return registrar(produto, TipoMovimentacao.AJUSTE_ENTRADA, quantidade, "AJUSTE", motivo);
    }

    public MovimentacaoEstoque ajustarSaida(Produto produto, int quantidade, String motivo) {
        produto.darBaixa(quantidade);
        MovimentacaoEstoque mov = registrar(produto, TipoMovimentacao.AJUSTE_SAIDA, quantidade, "AJUSTE", motivo);
        verificarEstoqueMinimo(produto);
        return mov;
    }

    private MovimentacaoEstoque registrar(Produto produto, TipoMovimentacao tipo, int quantidade,
                                          String referencia, String motivo) {
        return movimentacaoRepository.salvar(
                MovimentacaoEstoque.registrar(produto, tipo, quantidade, referencia, motivo));
    }

    private void verificarEstoqueMinimo(Produto produto) {
        if (produto.abaixoDoMinimo()) {
            eventos.publicar(new EstoqueBaixoEvent(produto.getId(), produto.getSku().valor(),
                    produto.getQuantidadeEmEstoque(), produto.getEstoqueMinimo(), Instant.now()));
        }
    }
}
