package com.empresa.erp.shared.domain.vo;

import com.empresa.erp.shared.domain.exception.DomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Valor monetário imutável, nunca negativo, sempre com 2 casas decimais.
 * Ao embutir mais de uma vez na mesma entidade, use @AttributeOverride na coluna "valor".
 */
@Embeddable
public record Dinheiro(@Column(name = "valor", precision = 19, scale = 2) BigDecimal valor)
        implements Comparable<Dinheiro> {

    public static final Dinheiro ZERO = new Dinheiro(BigDecimal.ZERO);

    public Dinheiro {
        Objects.requireNonNull(valor, "Valor monetário é obrigatório");
        if (valor.signum() < 0) {
            throw new DomainException("Valor monetário não pode ser negativo: " + valor);
        }
        valor = valor.setScale(2, RoundingMode.HALF_EVEN);
    }

    public static Dinheiro of(BigDecimal valor) {
        return new Dinheiro(valor);
    }

    public static Dinheiro of(String valor) {
        return new Dinheiro(new BigDecimal(valor));
    }

    public Dinheiro somar(Dinheiro outro) {
        return new Dinheiro(valor.add(outro.valor));
    }

    public Dinheiro multiplicar(int quantidade) {
        return new Dinheiro(valor.multiply(BigDecimal.valueOf(quantidade)));
    }

    public boolean isZero() {
        return valor.signum() == 0;
    }

    @Override
    public int compareTo(Dinheiro outro) {
        return valor.compareTo(outro.valor);
    }
}
