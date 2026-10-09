package com.empresa.erp.modules.compras.domain.entity;

import com.empresa.erp.modules.compras.domain.vo.Cnpj;
import com.empresa.erp.shared.domain.BaseEntity;
import com.empresa.erp.shared.domain.exception.DomainException;
import com.empresa.erp.shared.domain.vo.Email;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Getter
@Entity
@Table(name = "fornecedor", schema = "compras")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Fornecedor extends BaseEntity {

    @Column(name = "razao_social", nullable = false, length = 150)
    private String razaoSocial;

    @Embedded
    private Cnpj cnpj;

    @Embedded
    private Email email;

    @Column(nullable = false)
    private boolean ativo;

    public static Fornecedor novo(String razaoSocial, Cnpj cnpj, Email email) {
        if (razaoSocial == null || razaoSocial.isBlank()) {
            throw new DomainException("Razão social é obrigatória");
        }
        Fornecedor f = new Fornecedor();
        f.razaoSocial = razaoSocial.trim();
        f.cnpj = Objects.requireNonNull(cnpj, "CNPJ é obrigatório");
        f.email = Objects.requireNonNull(email, "E-mail é obrigatório");
        f.ativo = true;
        return f;
    }

    public void inativar() {
        this.ativo = false;
    }

    public void exigirAtivo() {
        if (!ativo) {
            throw new DomainException("Fornecedor %d está inativo".formatted(getId()));
        }
    }
}
