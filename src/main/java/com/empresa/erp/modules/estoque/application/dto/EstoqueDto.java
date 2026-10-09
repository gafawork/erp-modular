package com.empresa.erp.modules.estoque.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class EstoqueDto {

    private EstoqueDto() {
    }

    public enum TipoAjuste { ENTRADA, SAIDA }

    public record AjusteRequest(
            @NotNull TipoAjuste tipo,
            @NotNull @Positive Integer quantidade,
            @NotBlank @Size(max = 255) String motivo) {
    }

    public record MovimentacaoResponse(
            Long id,
            Long produtoId,
            String sku,
            String tipo,
            int quantidade,
            int saldoApos,
            String motivo,
            String documentoReferencia,
            Instant dataHora,
            String usuario) {
    }
}
