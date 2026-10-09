package com.empresa.erp.infrastructure.security;

/**
 * Perfis de acesso. Os controllers dos módulos usam os nomes literais em @PreAuthorize
 * (ex.: hasRole('VENDAS')) para não dependerem da plataforma.
 */
public final class Perfis {

    public static final String ADMIN = "ADMIN";
    public static final String ESTOQUE = "ESTOQUE";
    public static final String VENDAS = "VENDAS";
    public static final String COMPRAS = "COMPRAS";

    private Perfis() {
    }
}
