package com.empresa.erp.modules.compras.domain.entity;

import com.empresa.erp.modules.compras.domain.vo.StatusCompra;
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
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
 * Agregado Compra. Fornecedor pertence ao mesmo módulo (associação JPA permitida);
 * o produto de cada item pertence ao módulo Estoque, por isso é referenciado só por ID + snapshot.
 */
@Getter
@Entity
@Table(name = "compra", schema = "compras")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Compra extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fornecedor_id")
    private Fornecedor fornecedor;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemCompra> itens = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusCompra status;

    @Embedded
    @AttributeOverride(name = "valor",
            column = @Column(name = "valor_total", nullable = false, precision = 19, scale = 2))
    private Dinheiro valorTotal;

    @Column(name = "data_emissao", nullable = false)
    private Instant dataEmissao;

    @Column(name = "data_recebimento")
    private Instant dataRecebimento;

    public static Compra nova(Fornecedor fornecedor) {
        Objects.requireNonNull(fornecedor, "Fornecedor é obrigatório");
        fornecedor.exigirAtivo();
        Compra c = new Compra();
        c.fornecedor = fornecedor;
        c.status = StatusCompra.ABERTA;
        c.valorTotal = Dinheiro.ZERO;
        c.dataEmissao = Instant.now();
        return c;
    }

    public void adicionarItem(Long produtoId, String sku, String nomeProduto, boolean produtoAtivo,
                              int quantidade, Dinheiro custoUnitario) {
        exigirStatus(StatusCompra.ABERTA);
        if (!produtoAtivo) {
            throw new DomainException("Produto %s está inativo".formatted(sku));
        }
        itens.stream()
                .filter(i -> i.getProdutoId().equals(produtoId))
                .findFirst()
                .ifPresentOrElse(
                        i -> i.incrementar(quantidade),
                        () -> itens.add(new ItemCompra(this, produtoId, sku, nomeProduto, quantidade, custoUnitario)));
        recalcularTotal();
    }

    public void marcarComoRecebida() {
        exigirStatus(StatusCompra.ABERTA);
        if (itens.isEmpty()) {
            throw new DomainException("Compra sem itens não pode ser recebida");
        }
        this.status = StatusCompra.RECEBIDA;
        this.dataRecebimento = Instant.now();
    }

    public void cancelar() {
        exigirStatus(StatusCompra.ABERTA);
        this.status = StatusCompra.CANCELADA;
    }

    public List<ItemCompra> getItens() {
        return Collections.unmodifiableList(itens);
    }

    private void recalcularTotal() {
        this.valorTotal = itens.stream().map(ItemCompra::subtotal).reduce(Dinheiro.ZERO, Dinheiro::somar);
    }

    private void exigirStatus(StatusCompra esperado) {
        if (status != esperado) {
            throw new DomainException("Operação inválida: compra está %s (esperado %s)".formatted(status, esperado));
        }
    }
}
