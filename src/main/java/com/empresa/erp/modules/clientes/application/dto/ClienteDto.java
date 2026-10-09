package com.empresa.erp.modules.clientes.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class ClienteDto {

    private ClienteDto() {
    }

    public record Request(
            @NotBlank @Size(max = 150) String nome,
            @NotBlank String cpf,
            @NotBlank @Email String email,
            @NotNull @Valid EnderecoDto endereco) {
    }

    public record AtualizacaoRequest(
            @NotBlank @Size(max = 150) String nome,
            @NotBlank @Email String email,
            @NotNull @Valid EnderecoDto endereco) {
    }

    public record Response(
            Long id,
            String nome,
            String cpf,
            String email,
            EnderecoDto endereco,
            boolean ativo,
            Instant criadoEm) {
    }
}
