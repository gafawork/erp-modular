package com.empresa.erp.shared.event;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca um evento que também deve ser enviado para sistemas externos.
 * Ao ser publicado, o evento é gravado no outbox (mesma transação do negócio)
 * e depois enviado para a fila JMS indicada pelo OutboxRelay.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Externalizado {

    /** Nome da fila JMS de destino. */
    String value();
}
