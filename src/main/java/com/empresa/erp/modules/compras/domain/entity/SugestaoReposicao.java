package com.empresa.erp.modules.compras.domain.entity;

import com.empresa.erp.modules.compras.domain.vo.StatusSugestao;
import com.empresa.erp.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Sugestão de compra gerada quando o módulo Estoque avisa que um produto ficou abaixo do mínimo.
 * Exemplo de reação a evento de outro módulo (consistência eventual).
 */
@Getter
@Entity
@Table(name = "sugestao_reposicao", schema = "compras")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SugestaoReposicao extends BaseEntity {

    @Column(name = "produto_id", nullable = false)
    private Long produtoId;

    @Column(nullable = false, length = 50)
    private String sku;

    @Column(name = "quantidade_atual", nullable = false)
    private int quantidadeAtual;

    @Column(name = "estoque_minimo", nullable = false)
    private int estoqueMinimo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusSugestao status;

    @Column(name = "data_ultimo_alerta", nullable = false)
    private Instant dataUltimoAlerta;

    public static SugestaoReposicao nova(Long produtoId, String sku, int quantidadeAtual, int estoqueMinimo) {
        SugestaoReposicao s = new SugestaoReposicao();
        s.produtoId = produtoId;
        s.sku = sku;
        s.status = StatusSugestao.PENDENTE;
        s.atualizarSaldo(quantidadeAtual, estoqueMinimo);
        return s;
    }

    public void atualizarSaldo(int quantidadeAtual, int estoqueMinimo) {
        this.quantidadeAtual = quantidadeAtual;
        this.estoqueMinimo = estoqueMinimo;
        this.dataUltimoAlerta = Instant.now();
    }

    /** Quantidade sugerida: repor até o dobro do mínimo. */
    public int quantidadeSugerida() {
        return Math.max(estoqueMinimo * 2 - quantidadeAtual, 1);
    }

    public void marcarAtendida() {
        this.status = StatusSugestao.ATENDIDA;
    }
}
