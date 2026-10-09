package com.empresa.erp.modules.compras.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class FornecedorDto {

    private FornecedorDto() {
    }

    public record Request(
            @NotBlank @Size(max = 150) String razaoSocial,
            @NotBlank String cnpj,
            @NotBlank @Email String email) {
    }

    public record Response(Long id, String razaoSocial, String cnpj, String email, boolean ativo) {
    }
}
