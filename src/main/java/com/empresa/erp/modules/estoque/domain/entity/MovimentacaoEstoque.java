package com.empresa.erp.modules.estoque.domain.entity;

import com.empresa.erp.modules.estoque.domain.vo.TipoMovimentacao;
import com.empresa.erp.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Registro imutável (kardex) de cada alteração de saldo.
 * A associação com Produto é permitida porque ambos pertencem ao MESMO módulo.
 */
@Getter
@Entity
@Table(name = "movimentacao", schema = "estoque")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MovimentacaoEstoque extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id")
    private Produto produto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimentacao tipo;

    @Column(nullable = false)
    private int quantidade;

    @Column(name = "saldo_apos", nullable = false)
    private int saldoApos;

    @Column(length = 255)
    private String motivo;

    @Column(name = "documento_referencia", length = 50)
    private String documentoReferencia;

    @Column(name = "data_hora", nullable = false)
    private Instant dataHora;

    /** Chamar DEPOIS de alterar o saldo do produto. */
    public static MovimentacaoEstoque registrar(Produto produto, TipoMovimentacao tipo, int quantidade,
                                                String documentoReferencia, String motivo) {
        MovimentacaoEstoque m = new MovimentacaoEstoque();
        m.produto = produto;
        m.tipo = tipo;
        m.quantidade = quantidade;
        m.saldoApos = produto.getQuantidadeEmEstoque();
        m.documentoReferencia = documentoReferencia;
        m.motivo = motivo;
        m.dataHora = Instant.now();
        return m;
    }
}
