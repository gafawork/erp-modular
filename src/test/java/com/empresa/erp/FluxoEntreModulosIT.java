package com.empresa.erp;

import com.empresa.erp.infrastructure.outbox.OutboxMessageJpaRepository;
import com.empresa.erp.modules.clientes.application.dto.ClienteDto;
import com.empresa.erp.modules.clientes.application.dto.EnderecoDto;
import com.empresa.erp.modules.clientes.application.service.ClienteApplicationService;
import com.empresa.erp.modules.compras.application.dto.CompraDto;
import com.empresa.erp.modules.compras.application.dto.FornecedorDto;
import com.empresa.erp.modules.compras.application.service.CompraApplicationService;
import com.empresa.erp.modules.compras.application.service.FornecedorApplicationService;
import com.empresa.erp.modules.compras.application.service.SugestaoReposicaoService;
import com.empresa.erp.modules.estoque.api.EstoqueInsuficienteException;
import com.empresa.erp.modules.estoque.application.dto.ProdutoDto;
import com.empresa.erp.modules.estoque.application.service.ProdutoApplicationService;
import com.empresa.erp.modules.vendas.application.dto.VendaDto;
import com.empresa.erp.modules.vendas.application.service.VendaApplicationService;
import com.empresa.erp.shared.domain.exception.DomainException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

/**
 * Fluxo completo atravessando os quatro módulos:
 * compras → (EstoqueApi) estoque → vendas → (EstoqueApi) baixa → (evento) compras: sugestão de reposição
 * e eventos externalizados → outbox → JMS.
 */
@SpringBootTest(properties = "app.outbox.intervalo-ms=200")
class FluxoEntreModulosIT {

    @Autowired FornecedorApplicationService fornecedores;
    @Autowired ProdutoApplicationService produtos;
    @Autowired CompraApplicationService compras;
    @Autowired SugestaoReposicaoService sugestoes;
    @Autowired ClienteApplicationService clientes;
    @Autowired VendaApplicationService vendas;
    @Autowired OutboxMessageJpaRepository outbox;

    @Test
    void compraAbasteceEstoque_vendaBaixa_alertaGeraSugestao_eventosSaemPeloOutbox() {
        var fornecedor = fornecedores.criar(
                new FornecedorDto.Request("Distribuidora XPTO Ltda", "11.222.333/0001-81", "contato@xpto.com.br"));
        var produto = produtos.criar(new ProdutoDto.Request(
                "MOUSE-01", "Mouse óptico", "USB", new BigDecimal("0"), new BigDecimal("49.90"), 5));

        // COMPRAS → ESTOQUE (síncrono, mesma transação)
        var compra = compras.criar(new CompraDto.Request(fornecedor.id(),
                List.of(new CompraDto.ItemRequest(produto.id(), 10, new BigDecimal("20.00")))));
        assertThat(compra.itens().getFirst().sku()).isEqualTo("MOUSE-01");   // snapshot vindo do Estoque
        compras.receber(compra.id());

        var aposCompra = produtos.buscar(produto.id());
        assertThat(aposCompra.quantidadeEmEstoque()).isEqualTo(10);
        assertThat(aposCompra.precoCusto()).isEqualByComparingTo("20.00");

        // VENDAS → CLIENTES + ESTOQUE
        var cliente = clientes.criar(new ClienteDto.Request("Maria Silva", "529.982.247-25", "maria@email.com",
                new EnderecoDto("Rua A", "100", null, "Centro", "Rio de Janeiro", "rj", "20000-000")));
        var venda = vendas.criar(new VendaDto.Request(cliente.id(), List.of(new VendaDto.ItemRequest(produto.id(), 6))));
        assertThat(venda.nomeCliente()).isEqualTo("Maria Silva");
        assertThat(venda.valorTotal()).isEqualByComparingTo("299.40");

        vendas.confirmar(venda.id());
        assertThat(produtos.buscar(produto.id()).quantidadeEmEstoque()).isEqualTo(4);   // 4 <= mínimo 5

        // ESTOQUE → COMPRAS (evento assíncrono após o commit)
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(sugestoes.listarPendentes())
                        .anySatisfy(s -> {
                            assertThat(s.produtoId()).isEqualTo(produto.id());
                            assertThat(s.quantidadeAtual()).isEqualTo(4);
                        }));

        // Eventos externalizados (CompraRecebida + VendaConfirmada) enviados ao JMS pelo relay
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            assertThat(outbox.countByEnviadoEmIsNotNull()).isGreaterThanOrEqualTo(2);
            assertThat(outbox.countByEnviadoEmIsNull()).isZero();
        });

        // Cancelamento estorna o estoque
        vendas.cancelar(venda.id());
        assertThat(produtos.buscar(produto.id()).quantidadeEmEstoque()).isEqualTo(10);
        assertThat(produtos.listarMovimentacoes(produto.id(), PageRequest.of(0, 10)).getTotalElements()).isEqualTo(3);

        // Estoque insuficiente: rollback de tudo, venda continua PENDENTE
        var vendaGrande = vendas.criar(new VendaDto.Request(cliente.id(),
                List.of(new VendaDto.ItemRequest(produto.id(), 50))));
        assertThatThrownBy(() -> vendas.confirmar(vendaGrande.id())).isInstanceOf(EstoqueInsuficienteException.class);
        assertThat(produtos.buscar(produto.id()).quantidadeEmEstoque()).isEqualTo(10);
        assertThat(vendas.buscar(vendaGrande.id()).status()).isEqualTo("PENDENTE");

        // Nova compra recebida atende a sugestão pendente
        var reposicao = compras.criar(new CompraDto.Request(fornecedor.id(),
                List.of(new CompraDto.ItemRequest(produto.id(), 5, new BigDecimal("22.00")))));
        compras.receber(reposicao.id());
        assertThat(sugestoes.listarPendentes()).noneMatch(s -> s.produtoId().equals(produto.id()));
    }

    @Test
    void naoPermiteClienteComCpfDuplicado() {
        var endereco = new EnderecoDto("Rua B", "1", null, null, "Niterói", "RJ", "24000000");
        clientes.criar(new ClienteDto.Request("João", "111.444.777-35", "joao@email.com", endereco));
        assertThatThrownBy(() -> clientes.criar(
                new ClienteDto.Request("Outro João", "11144477735", "outro@email.com", endereco)))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("CPF");
    }
}
