package com.empresa.erp.modules.compras.domain.vo;

import com.empresa.erp.shared.domain.exception.DomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** CNPJ validado (dígitos verificadores), armazenado somente com números. */
@Embeddable
public record Cnpj(@Column(name = "cnpj", length = 14, unique = true) String numero) {

    private static final int[] PESOS_1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] PESOS_2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    public Cnpj {
        if (numero == null || numero.isBlank()) {
            throw new DomainException("CNPJ é obrigatório");
        }
        numero = numero.replaceAll("\\D", "");
        if (!valido(numero)) {
            throw new DomainException("CNPJ inválido: " + numero);
        }
    }

    private static boolean valido(String cnpj) {
        if (cnpj.length() != 14 || cnpj.chars().distinct().count() == 1) {
            return false;
        }
        return digito(cnpj, PESOS_1) == cnpj.charAt(12) - '0'
                && digito(cnpj, PESOS_2) == cnpj.charAt(13) - '0';
    }

    private static int digito(String cnpj, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            soma += (cnpj.charAt(i) - '0') * pesos[i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
