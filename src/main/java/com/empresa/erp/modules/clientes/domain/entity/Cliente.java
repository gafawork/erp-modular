package com.empresa.erp.modules.clientes.domain.entity;

import com.empresa.erp.modules.clientes.domain.vo.Cpf;
import com.empresa.erp.modules.clientes.domain.vo.Endereco;
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
@Table(name = "cliente", schema = "clientes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cliente extends BaseEntity {

    @Column(nullable = false, length = 150)
    private String nome;

    @Embedded
    private Cpf cpf;

    @Embedded
    private Email email;

    @Embedded
    private Endereco endereco;

    @Column(nullable = false)
    private boolean ativo;

    public static Cliente novo(String nome, Cpf cpf, Email email, Endereco endereco) {
        Cliente c = new Cliente();
        c.nome = exigirNome(nome);
        c.cpf = Objects.requireNonNull(cpf, "CPF é obrigatório");
        c.email = Objects.requireNonNull(email, "E-mail é obrigatório");
        c.endereco = Objects.requireNonNull(endereco, "Endereço é obrigatório");
        c.ativo = true;
        return c;
    }

    public void atualizar(String nome, Email email, Endereco endereco) {
        this.nome = exigirNome(nome);
        this.email = Objects.requireNonNull(email, "E-mail é obrigatório");
        this.endereco = Objects.requireNonNull(endereco, "Endereço é obrigatório");
    }

    public void inativar() {
        if (!ativo) {
            throw new DomainException("Cliente já está inativo");
        }
        this.ativo = false;
    }

    public void ativar() {
        this.ativo = true;
    }

    private static String exigirNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new DomainException("Nome do cliente é obrigatório");
        }
        return nome.trim();
    }
}
