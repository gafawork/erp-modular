package com.empresa.erp.modules.clientes.domain.vo;

import com.empresa.erp.shared.domain.exception.DomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** CPF validado (dígitos verificadores), armazenado somente com números. */
@Embeddable
public record Cpf(@Column(name = "cpf", length = 11, unique = true) String numero) {

    public Cpf {
        if (numero == null || numero.isBlank()) {
            throw new DomainException("CPF é obrigatório");
        }
        numero = numero.replaceAll("\\D", "");
        if (!valido(numero)) {
            throw new DomainException("CPF inválido: " + numero);
        }
    }

    public String formatado() {
        return "%s.%s.%s-%s".formatted(
                numero.substring(0, 3), numero.substring(3, 6), numero.substring(6, 9), numero.substring(9));
    }

    private static boolean valido(String cpf) {
        if (cpf.length() != 11 || cpf.chars().distinct().count() == 1) {
            return false;
        }
        return digito(cpf, 9) == cpf.charAt(9) - '0'
                && digito(cpf, 10) == cpf.charAt(10) - '0';
    }

    private static int digito(String cpf, int tamanho) {
        int soma = 0;
        for (int i = 0; i < tamanho; i++) {
            soma += (cpf.charAt(i) - '0') * (tamanho + 1 - i);
        }
        int resto = 11 - (soma % 11);
        return resto > 9 ? 0 : resto;
    }
}
