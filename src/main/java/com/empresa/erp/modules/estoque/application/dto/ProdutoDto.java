package com.empresa.erp.modules.estoque.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public final class ProdutoDto {

    private ProdutoDto() {
    }

    public record Request(
            @NotBlank @Size(max = 50) String sku,
            @NotBlank @Size(max = 150) String nome,
            @Size(max = 500) String descricao,
            @NotNull @DecimalMin("0.00") BigDecimal precoCusto,
            @NotNull @DecimalMin("0.01") BigDecimal precoVenda,
            @NotNull @PositiveOrZero Integer estoqueMinimo) {
    }

    public record AtualizacaoRequest(
            @NotBlank @Size(max = 150) String nome,
            @Size(max = 500) String descricao,
            @NotNull @DecimalMin("0.01") BigDecimal precoVenda,
            @NotNull @PositiveOrZero Integer estoqueMinimo) {
    }

    public record Response(
            Long id,
            String sku,
            String nome,
            String descricao,
            BigDecimal precoCusto,
            BigDecimal precoVenda,
            int quantidadeEmEstoque,
            int estoqueMinimo,
            boolean abaixoDoMinimo,
            boolean ativo) {
    }
}
