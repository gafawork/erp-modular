package com.empresa.erp.modules.estoque.domain;

import com.empresa.erp.modules.estoque.api.EstoqueInsuficienteException;
import com.empresa.erp.modules.estoque.domain.entity.Produto;
import com.empresa.erp.modules.estoque.domain.vo.Sku;
import com.empresa.erp.shared.domain.exception.DomainException;
import com.empresa.erp.shared.domain.vo.Dinheiro;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProdutoTest {

    private Produto novoProduto() {
        return Produto.novo(new Sku("tec-01"), "Teclado", null, Dinheiro.of("10.00"), Dinheiro.of("25.00"), 2);
    }

    @Test
    void produtoNovoComecaSemEstoqueESkuNormalizado() {
        Produto p = novoProduto();
        assertThat(p.getSku().valor()).isEqualTo("TEC-01");
        assertThat(p.getQuantidadeEmEstoque()).isZero();
    }

    @Test
    void entradaComCustoRecalculaCustoMedioPonderado() {
        Produto p = novoProduto();
        p.registrarEntradaComCusto(10, Dinheiro.of("10.00"));
        p.registrarEntradaComCusto(10, Dinheiro.of("20.00"));
        assertThat(p.getQuantidadeEmEstoque()).isEqualTo(20);
        assertThat(p.getPrecoCusto()).isEqualTo(Dinheiro.of("15.00"));
    }

    @Test
    void naoPermiteBaixaMaiorQueOSaldo() {
        Produto p = novoProduto();
        p.darEntrada(3);
        assertThatThrownBy(() -> p.darBaixa(5)).isInstanceOf(EstoqueInsuficienteException.class);
        assertThat(p.getQuantidadeEmEstoque()).isEqualTo(3);
    }

    @Test
    void indicaEstoqueAbaixoDoMinimo() {
        Produto p = novoProduto();
        p.darEntrada(5);
        p.darBaixa(3);
        assertThat(p.abaixoDoMinimo()).isTrue();
    }

    @Test
    void skuInvalidoERejeitado() {
        assertThatThrownBy(() -> new Sku("com espaço")).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> Dinheiro.of("-1")).isInstanceOf(DomainException.class);
    }
}
