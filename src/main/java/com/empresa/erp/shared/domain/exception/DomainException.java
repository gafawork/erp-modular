package com.empresa.erp.shared.domain.exception;

/** Regra de negócio violada (HTTP 422). Base de todas as exceções de domínio dos módulos. */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }
}
