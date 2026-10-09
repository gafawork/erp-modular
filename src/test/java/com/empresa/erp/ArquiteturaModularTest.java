package com.empresa.erp;

import com.empresa.erp.shared.architecture.Modulo;
import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * Garante as fronteiras do monolito modular. Qualquer violação quebra o build.
 *
 * <ol>
 *   <li>Um módulo só usa outro módulo se ele estiver em {@code @Modulo(dependeDe = ...)};</li>
 *   <li>e, nesse módulo, só acessa os pacotes públicos {@code api} e {@code events};</li>
 *   <li>a camada {@code domain} nunca depende de outro módulo (a tradução fica na {@code application});</li>
 *   <li>não há ciclos entre módulos;</li>
 *   <li>shared não conhece módulos nem a plataforma; módulos não conhecem a plataforma.</li>
 * </ol>
 */
@AnalyzeClasses(packages = "com.empresa.erp", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquiteturaModularTest {

    private static final String RAIZ_MODULOS = "com.empresa.erp.modules.";
    private static final Set<String> PACOTES_PUBLICOS = Set.of("api", "events");
    private static final Map<String, Optional<Modulo>> CACHE = new ConcurrentHashMap<>();

    @ArchTest
    static final ArchRule modulosRespeitamFronteiras = classes()
            .that().resideInAPackage("com.empresa.erp.modules..")
            .should(respeitarFronteirasDosModulos());

    @ArchTest
    static final ArchRule modulosSemCiclos = slices()
            .matching("com.empresa.erp.modules.(*)..")
            .should().beFreeOfCycles();

    @ArchTest
    static final ArchRule dominioNaoDependeDeOutrasCamadas = noClasses()
            .that().resideInAPackage("..modules.*.domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..modules.*.application..", "..modules.*.infrastructure..");

    @ArchTest
    static final ArchRule sharedIndependente = noClasses()
            .that().resideInAPackage("com.empresa.erp.shared..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("com.empresa.erp.modules..", "com.empresa.erp.infrastructure..");

    @ArchTest
    static final ArchRule modulosNaoDependemDaPlataforma = noClasses()
            .that().resideInAPackage("com.empresa.erp.modules..")
            .should().dependOnClassesThat().resideInAPackage("com.empresa.erp.infrastructure..");

    // ------------------------------------------------------------------------------------------

    private static ArchCondition<JavaClass> respeitarFronteirasDosModulos() {
        return new ArchCondition<JavaClass>("usar outros módulos só se declarados em @Modulo e só via api/events") {
            @Override
            public void check(JavaClass origem, ConditionEvents events) {
                String moduloOrigem = modulo(origem.getPackageName());
                Optional<Modulo> declaracao = declaracao(moduloOrigem);
                if (declaracao.isEmpty()) {
                    events.add(SimpleConditionEvent.violated(origem,
                            "Módulo '%s' não possui package-info.java com @Modulo".formatted(moduloOrigem)));
                    return;
                }
                Set<String> permitidos = Set.of(declaracao.get().dependeDe());
                boolean origemNoDominio = origem.getPackageName().startsWith(RAIZ_MODULOS + moduloOrigem + ".domain");

                for (Dependency dep : origem.getDirectDependenciesFromSelf()) {
                    String pacoteAlvo = dep.getTargetClass().getPackageName();
                    String moduloAlvo = modulo(pacoteAlvo);
                    if (moduloAlvo == null || moduloAlvo.equals(moduloOrigem)) {
                        continue;
                    }
                    if (origemNoDominio) {
                        violar(events, dep, "a camada domain não pode depender de outro módulo");
                    } else if (!permitidos.contains(moduloAlvo)) {
                        violar(events, dep, "'%s' não declara dependência de '%s' em @Modulo"
                                .formatted(moduloOrigem, moduloAlvo));
                    } else if (!publico(pacoteAlvo, moduloAlvo)) {
                        violar(events, dep, "acesso a pacote interno de '%s' (use api ou events)".formatted(moduloAlvo));
                    }
                }
            }
        };
    }

    private static void violar(ConditionEvents events, Dependency dep, String motivo) {
        events.add(SimpleConditionEvent.violated(dep, motivo + ": " + dep.getDescription()));
    }

    /** "com.empresa.erp.modules.vendas.application.service" → "vendas" */
    private static String modulo(String pacote) {
        if (!pacote.startsWith(RAIZ_MODULOS)) {
            return null;
        }
        String resto = pacote.substring(RAIZ_MODULOS.length());
        int ponto = resto.indexOf('.');
        return ponto < 0 ? resto : resto.substring(0, ponto);
    }

    private static boolean publico(String pacote, String modulo) {
        String resto = pacote.substring((RAIZ_MODULOS + modulo).length());
        return PACOTES_PUBLICOS.stream()
                .anyMatch(p -> resto.equals("." + p) || resto.startsWith("." + p + "."));
    }

    private static Optional<Modulo> declaracao(String modulo) {
        return CACHE.computeIfAbsent(modulo, m -> {
            try {
                Class<?> packageInfo = Class.forName(RAIZ_MODULOS + m + ".package-info");
                return Optional.ofNullable(packageInfo.getAnnotation(Modulo.class));
            } catch (ClassNotFoundException e) {
                return Optional.empty();
            }
        });
    }
}
