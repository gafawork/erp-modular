package com.empresa.erp.modules.vendas.domain.entity;

import com.empresa.erp.modules.vendas.domain.vo.StatusVenda;
import com.empresa.erp.shared.domain.BaseEntity;
import com.empresa.erp.shared.domain.exception.DomainException;
import com.empresa.erp.shared.domain.vo.Dinheiro;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Agregado Venda. O cliente pertence a outro módulo: referência só por ID + snapshot do nome.
 * Não conhece Cliente nem Produto, nem a API de outros módulos.
 */
@Getter
@Entity
@Table(name = "venda", schema = "vendas")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Venda extends BaseEntity {

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "nome_cliente", nullable = false, length = 150)
    private String nomeCliente;

    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemVenda> itens = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusVenda status;

    @Embedded
    @AttributeOverride(name = "valor",
            column = @Column(name = "valor_total", nullable = false, precision = 19, scale = 2))
    private Dinheiro valorTotal;

    @Column(name = "data_venda", nullable = false)
    private Instant dataVenda;

    @Column(name = "data_confirmacao")
    private Instant dataConfirmacao;

    public static Venda nova(Long clienteId, String nomeCliente, boolean clienteAtivo) {
        Objects.requireNonNull(clienteId, "Cliente é obrigatório");
        if (!clienteAtivo) {
            throw new DomainException("Cliente %d está inativo".formatted(clienteId));
        }
        Venda v = new Venda();
        v.clienteId = clienteId;
        v.nomeCliente = nomeCliente;
        v.status = StatusVenda.PENDENTE;
        v.valorTotal = Dinheiro.ZERO;
        v.dataVenda = Instant.now();
        return v;
    }

    /** O preço é "congelado" no momento em que o item é adicionado. */
    public void adicionarItem(Long produtoId, String sku, String nomeProduto, boolean produtoAtivo,
                              Dinheiro precoUnitario, int quantidade) {
        exigirStatus(StatusVenda.PENDENTE);
        if (!produtoAtivo) {
            throw new DomainException("Produto %s está inativo".formatted(sku));
        }
        itens.stream()
                .filter(i -> i.getProdutoId().equals(produtoId))
                .findFirst()
                .ifPresentOrElse(
                        i -> i.incrementar(quantidade),
                        () -> itens.add(new ItemVenda(this, produtoId, sku, nomeProduto, quantidade, precoUnitario)));
        recalcularTotal();
    }

    public void confirmar() {
        exigirStatus(StatusVenda.PENDENTE);
        if (itens.isEmpty()) {
            throw new DomainException("Venda sem itens não pode ser confirmada");
        }
        this.status = StatusVenda.CONFIRMADA;
        this.dataConfirmacao = Instant.now();
    }

    /** @return o status anterior (para saber se é preciso estornar o estoque). */
    public StatusVenda cancelar() {
        if (status == StatusVenda.CANCELADA) {
            throw new DomainException("Venda já está cancelada");
        }
        StatusVenda anterior = this.status;
        this.status = StatusVenda.CANCELADA;
        return anterior;
    }

    public List<ItemVenda> getItens() {
        return Collections.unmodifiableList(itens);
    }

    private void recalcularTotal() {
        this.valorTotal = itens.stream().map(ItemVenda::subtotal).reduce(Dinheiro.ZERO, Dinheiro::somar);
    }

    private void exigirStatus(StatusVenda esperado) {
        if (status != esperado) {
            throw new DomainException("Operação inválida: venda está %s (esperado %s)".formatted(status, esperado));
        }
    }
}
