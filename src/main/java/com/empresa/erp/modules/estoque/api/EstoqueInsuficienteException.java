package com.empresa.erp.modules.estoque.api;

import com.empresa.erp.shared.domain.exception.ConflitoDeNegocioException;

/** Parte do contrato público: quem chama EstoqueApi.baixar pode recebê-la (HTTP 409). */
public class EstoqueInsuficienteException extends ConflitoDeNegocioException {

    public EstoqueInsuficienteException(String sku, int disponivel, int solicitado) {
        super("Estoque insuficiente para o produto %s: disponível=%d, solicitado=%d"
                .formatted(sku, disponivel, solicitado));
    }
}
