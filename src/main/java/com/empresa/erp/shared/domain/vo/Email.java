package com.empresa.erp.shared.domain.vo;

import com.empresa.erp.shared.domain.exception.DomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Locale;
import java.util.regex.Pattern;

@Embeddable
public record Email(@Column(name = "email", length = 150) String valor) {

    private static final Pattern FORMATO = Pattern.compile("^[\\w.%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$");

    public Email {
        if (valor == null || !FORMATO.matcher(valor.trim()).matches()) {
            throw new DomainException("E-mail inválido: " + valor);
        }
        valor = valor.trim().toLowerCase(Locale.ROOT);
    }
}
