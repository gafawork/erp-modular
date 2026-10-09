package com.empresa.erp.modules.clientes.api;

/** Projeção imutável exposta a outros módulos (nunca a entidade JPA). */
public record ClienteResumo(Long id, String nome, String cpf, boolean ativo) {
}
