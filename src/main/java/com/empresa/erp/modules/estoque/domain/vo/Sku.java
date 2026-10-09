package com.empresa.erp.modules.estoque.domain.vo;

import com.empresa.erp.shared.domain.exception.DomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Locale;

/** Código do produto (Stock Keeping Unit): maiúsculo, sem espaços, até 50 caracteres. */
@Embeddable
public record Sku(@Column(name = "sku", length = 50, unique = true, nullable = false) String valor) {

    public Sku {
        if (valor == null || valor.isBlank()) {
            throw new DomainException("SKU é obrigatório");
        }
        valor = valor.trim().toUpperCase(Locale.ROOT);
        if (!valor.matches("[A-Z0-9._-]{1,50}")) {
            throw new DomainException("SKU inválido (use letras, números, '.', '_' ou '-'): " + valor);
        }
    }
}
