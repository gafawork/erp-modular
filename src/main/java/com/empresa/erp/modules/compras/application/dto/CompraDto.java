package com.empresa.erp.modules.compras.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class CompraDto {

    private CompraDto() {
    }

    public record Request(
            @NotNull Long fornecedorId,
            @NotEmpty List<@Valid ItemRequest> itens) {
    }

    public record ItemRequest(
            @NotNull Long produtoId,
            @NotNull @Positive Integer quantidade,
            @NotNull @DecimalMin("0.00") BigDecimal custoUnitario) {
    }

    public record Response(
            Long id,
            Long fornecedorId,
            String fornecedorRazaoSocial,
            String status,
            List<ItemResponse> itens,
            BigDecimal valorTotal,
            Instant dataEmissao,
            Instant dataRecebimento) {
    }

    public record ItemResponse(
            Long produtoId,
            String sku,
            String nomeProduto,
            int quantidade,
            BigDecimal custoUnitario,
            BigDecimal subtotal) {
    }

    public record SugestaoResponse(
            Long id,
            Long produtoId,
            String sku,
            int quantidadeAtual,
            int estoqueMinimo,
            int quantidadeSugerida,
            String status,
            Instant dataUltimoAlerta) {
    }
}
