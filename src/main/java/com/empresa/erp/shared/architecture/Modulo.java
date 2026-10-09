package com.empresa.erp.shared.architecture;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declara um módulo de negócio no {@code package-info.java} do seu pacote raiz.
 * O teste ArquiteturaModularTest garante que:
 * <ul>
 *   <li>o módulo só usa outros módulos listados em {@link #dependeDe()};</li>
 *   <li>e, nesses módulos, só acessa os pacotes públicos {@code api} e {@code events}.</li>
 * </ul>
 */
@Documented
@Target(ElementType.PACKAGE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Modulo {

    /** Nome legível do módulo. */
    String nome();

    /** Nomes (pacotes) dos módulos dos quais este módulo pode depender. */
    String[] dependeDe() default {};
}
