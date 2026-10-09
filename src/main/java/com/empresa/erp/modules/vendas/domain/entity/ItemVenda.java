package com.empresa.erp.modules.vendas.domain.entity;

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

/** Item da venda: produto por ID (sem FK para o schema estoque) + snapshot de SKU, nome e preço. */
@Getter
@Entity
@Table(name = "item_venda", schema = "vendas")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemVenda extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venda_id")
    private Venda venda;

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
            column = @Column(name = "preco_unitario", nullable = false, precision = 19, scale = 2))
    private Dinheiro precoUnitario;

    ItemVenda(Venda venda, Long produtoId, String sku, String nomeProduto, int quantidade, Dinheiro precoUnitario) {
        if (quantidade <= 0) {
            throw new DomainException("Quantidade do item deve ser maior que zero");
        }
        this.venda = venda;
        this.produtoId = produtoId;
        this.sku = sku;
        this.nomeProduto = nomeProduto;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
    }

    void incrementar(int quantidade) {
        if (quantidade <= 0) {
            throw new DomainException("Quantidade do item deve ser maior que zero");
        }
        this.quantidade += quantidade;
    }

    public Dinheiro subtotal() {
        return precoUnitario.multiplicar(quantidade);
    }
}
