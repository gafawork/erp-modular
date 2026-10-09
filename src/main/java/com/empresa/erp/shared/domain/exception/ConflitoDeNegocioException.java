package com.empresa.erp.shared.domain.exception;

/** Operação conflita com o estado atual do sistema (HTTP 409). Ex.: estoque insuficiente. */
public class ConflitoDeNegocioException extends DomainException {

    public ConflitoDeNegocioException(String message) {
        super(message);
    }
}
