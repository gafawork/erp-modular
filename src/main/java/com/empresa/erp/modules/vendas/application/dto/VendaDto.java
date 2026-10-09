package com.empresa.erp.modules.vendas.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class VendaDto {

    private VendaDto() {
    }

    public record Request(
            @NotNull Long clienteId,
            @NotEmpty List<@Valid ItemRequest> itens) {
    }

    /** Sem preço: o valor vem do cadastro do produto no módulo Estoque. */
    public record ItemRequest(
            @NotNull Long produtoId,
            @NotNull @Positive Integer quantidade) {
    }

    public record Response(
            Long id,
            Long clienteId,
            String nomeCliente,
            String status,
            List<ItemResponse> itens,
            BigDecimal valorTotal,
            Instant dataVenda,
            Instant dataConfirmacao) {
    }

    public record ItemResponse(
            Long produtoId,
            String sku,
            String nomeProduto,
            int quantidade,
            BigDecimal precoUnitario,
            BigDecimal subtotal) {
    }
}
