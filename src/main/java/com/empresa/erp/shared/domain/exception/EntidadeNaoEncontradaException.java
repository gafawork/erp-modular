package com.empresa.erp.shared.domain.exception;

/** Recurso inexistente (HTTP 404). */
public class EntidadeNaoEncontradaException extends DomainException {

    public EntidadeNaoEncontradaException(String entidade, Object id) {
        super("%s não encontrado(a): %s".formatted(entidade, id));
    }
}
