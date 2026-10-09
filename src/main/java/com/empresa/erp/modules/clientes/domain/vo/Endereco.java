package com.empresa.erp.modules.clientes.domain.vo;

import com.empresa.erp.shared.domain.exception.DomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Locale;

@Embeddable
public record Endereco(
        @Column(name = "logradouro", length = 150) String logradouro,
        @Column(name = "numero", length = 20) String numero,
        @Column(name = "complemento", length = 100) String complemento,
        @Column(name = "bairro", length = 100) String bairro,
        @Column(name = "cidade", length = 100) String cidade,
        @Column(name = "uf", length = 2) String uf,
        @Column(name = "cep", length = 8) String cep) {

    public Endereco {
        exigir(logradouro, "logradouro");
        exigir(numero, "número");
        exigir(cidade, "cidade");
        if (uf == null || !uf.trim().matches("[A-Za-z]{2}")) {
            throw new DomainException("UF inválida: " + uf);
        }
        uf = uf.trim().toUpperCase(Locale.ROOT);
        cep = cep == null ? null : cep.replaceAll("\\D", "");
        if (cep == null || cep.length() != 8) {
            throw new DomainException("CEP inválido: " + cep);
        }
    }

    private static void exigir(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new DomainException("Endereço: campo '%s' é obrigatório".formatted(campo));
        }
    }
}
