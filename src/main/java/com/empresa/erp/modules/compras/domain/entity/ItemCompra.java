package com.empresa.erp.modules.compras.domain.entity;

import com.empresa.erp.shared.domain.BaseEntity;
import com.empresa.erp.shared.domain.exception.DomainException;
import com.empresa.erp.shared.domain.vo.Dinheiro;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Item da compra: produto referenciado por ID (sem FK para o schema estoque) + snapshot de SKU e nome. */
@Getter
@Entity
@Table(name = "item_compra", schema = "compras")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemCompra extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "compra_id")
    private Compra compra;

    @Column(name = "produto_id", nullable = false)
    private Long produtoId;

    @Column(nullable = false, length = 50)
    private String sku;

    @Column(name = "nome_produto", nullable = false, length = 150)
    private String nomeProduto;

    @Column(nullable = false)
    private int quantidade;

    @Embedded
    @AttributeOverride(name = "valor",
            column = @Column(name = "custo_unitario", nullable = false, precision = 19, scale = 2))
    private Dinheiro custoUnitario;

    ItemCompra(Compra compra, Long produtoId, String sku, String nomeProduto, int quantidade, Dinheiro custoUnitario) {
        if (quantidade <= 0) {
            throw new DomainException("Quantidade do item deve ser maior que zero");
        }
        this.compra = compra;
        this.produtoId = produtoId;
        this.sku = sku;
        this.nomeProduto = nomeProduto;
        this.quantidade = quantidade;
        this.custoUnitario = custoUnitario;
    }

    void incrementar(int quantidade) {
        if (quantidade <= 0) {
            throw new DomainException("Quantidade do item deve ser maior que zero");
        }
        this.quantidade += quantidade;
    }

    public Dinheiro subtotal() {
        return custoUnitario.multiplicar(quantidade);
    }
}
