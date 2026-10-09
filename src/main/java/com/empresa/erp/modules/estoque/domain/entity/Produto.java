package com.empresa.erp.modules.estoque.domain.entity;

import com.empresa.erp.modules.estoque.api.EstoqueInsuficienteException;
import com.empresa.erp.modules.estoque.domain.vo.Sku;
import com.empresa.erp.shared.domain.BaseEntity;
import com.empresa.erp.shared.domain.exception.DomainException;
import com.empresa.erp.shared.domain.vo.Dinheiro;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Agregado Produto. O saldo só muda pelos métodos de domínio — nunca por setter —
 * e cada mudança é registrada em MovimentacaoEstoque pelo EstoqueDomainService.
 */
@Getter
@Entity
@Table(name = "produto", schema = "estoque")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Produto extends BaseEntity {

    @Embedded
    private Sku sku;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 500)
    private String descricao;

    @Embedded
    @AttributeOverride(name = "valor",
            column = @Column(name = "preco_custo", nullable = false, precision = 19, scale = 2))
    private Dinheiro precoCusto;

    @Embedded
    @AttributeOverride(name = "valor",
            column = @Column(name = "preco_venda", nullable = false, precision = 19, scale = 2))
    private Dinheiro precoVenda;

    @Column(name = "quantidade_estoque", nullable = false)
    private int quantidadeEmEstoque;

    @Column(name = "estoque_minimo", nullable = false)
    private int estoqueMinimo;

    @Column(nullable = false)
    private boolean ativo;

    public static Produto novo(Sku sku, String nome, String descricao,
                               Dinheiro precoCusto, Dinheiro precoVenda, int estoqueMinimo) {
        Produto p = new Produto();
        p.sku = Objects.requireNonNull(sku, "SKU é obrigatório");
        p.precoCusto = Objects.requireNonNull(precoCusto, "Preço de custo é obrigatório");
        p.quantidadeEmEstoque = 0;
        p.ativo = true;
        p.atualizar(nome, descricao, precoVenda, estoqueMinimo);
        return p;
    }

    public void atualizar(String nome, String descricao, Dinheiro precoVenda, int estoqueMinimo) {
        if (nome == null || nome.isBlank()) {
            throw new DomainException("Nome do produto é obrigatório");
        }
        Objects.requireNonNull(precoVenda, "Preço de venda é obrigatório");
        if (precoVenda.isZero()) {
            throw new DomainException("Preço de venda deve ser maior que zero");
        }
        if (estoqueMinimo < 0) {
            throw new DomainException("Estoque mínimo não pode ser negativo");
        }
        this.nome = nome.trim();
        this.descricao = descricao;
        this.precoVenda = precoVenda;
        this.estoqueMinimo = estoqueMinimo;
    }

    public void darEntrada(int quantidade) {
        exigirQuantidadePositiva(quantidade);
        this.quantidadeEmEstoque += quantidade;
    }

    /** Entrada por compra: recalcula o custo médio ponderado. */
    public void registrarEntradaComCusto(int quantidade, Dinheiro custoUnitario) {
        exigirQuantidadePositiva(quantidade);
        Objects.requireNonNull(custoUnitario, "Custo unitário é obrigatório na entrada por compra");
        BigDecimal totalAtual = precoCusto.valor().multiply(BigDecimal.valueOf(quantidadeEmEstoque));
        BigDecimal totalEntrada = custoUnitario.valor().multiply(BigDecimal.valueOf(quantidade));
        int novaQuantidade = quantidadeEmEstoque + quantidade;
        this.precoCusto = Dinheiro.of(totalAtual.add(totalEntrada)
                .divide(BigDecimal.valueOf(novaQuantidade), 2, RoundingMode.HALF_EVEN));
        this.quantidadeEmEstoque = novaQuantidade;
    }

    public void darBaixa(int quantidade) {
        exigirQuantidadePositiva(quantidade);
        if (!possuiEstoque(quantidade)) {
            throw new EstoqueInsuficienteException(sku.valor(), quantidadeEmEstoque, quantidade);
        }
        this.quantidadeEmEstoque -= quantidade;
    }

    public boolean possuiEstoque(int quantidade) {
        return quantidadeEmEstoque >= quantidade;
    }

    public boolean abaixoDoMinimo() {
        return quantidadeEmEstoque <= estoqueMinimo;
    }

    public void exigirAtivo() {
        if (!ativo) {
            throw new DomainException("Produto %s está inativo".formatted(sku.valor()));
        }
    }

    public void inativar() {
        this.ativo = false;
    }

    private static void exigirQuantidadePositiva(int quantidade) {
        if (quantidade <= 0) {
            throw new DomainException("Quantidade deve ser maior que zero");
        }
    }
}
