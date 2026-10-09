package com.empresa.erp.shared.event;

import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Listener de evento vindo de OUTRO módulo. Equivale a:
 * <ul>
 *   <li>{@code @TransactionalEventListener(AFTER_COMMIT)} — só executa se a transação de quem publicou for confirmada;</li>
 *   <li>{@code @Async} — não bloqueia nem atrasa quem publicou;</li>
 *   <li>{@code @Transactional(REQUIRES_NEW)} — roda na sua própria transação: uma falha aqui não desfaz a origem.</li>
 * </ul>
 * Consistência eventual: se a aplicação cair entre o commit e a execução, o evento em memória se perde.
 * Para efeitos que não podem se perder, use {@link Externalizado} (outbox).
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Async
@Transactional(propagation = Propagation.REQUIRES_NEW)
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public @interface ListenerDeModulo {
}
