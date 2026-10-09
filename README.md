# ERP Modular — Monolito Modular com DDD

Spring Boot **3.5** · Java **21** · sem Spring Modulith (somente Spring Boot + ArchUnit nos testes).

Um único deployable (um JAR, um banco), dividido em **módulos de negócio com fronteiras verificadas por teste**.
A arquitetura está descrita em [`docs/ARQUITETURA-MONOLITO-MODULAR.md`](docs/ARQUITETURA-MONOLITO-MODULAR.md);
a seção "Diferenças em relação ao documento" abaixo explica como cada recurso do Spring Modulith foi substituído.

## Módulos

| Módulo | Responsabilidade | Público (`api` / `events`) | Depende de |
|---|---|---|---|
| `clientes` | Cadastro de clientes | `ClientesApi`, `ClienteResumo` / `ClienteInativadoEvent` | — |
| `estoque` | Produtos, saldo, kardex | `EstoqueApi`, `ProdutoResumo`, `ItemMovimento`, `EstoqueInsuficienteException` / `EstoqueBaixoEvent` | — |
| `compras` | Fornecedores, compras, sugestões de reposição | — / `CompraRecebidaEvent` (→ JMS) | `estoque` |
| `vendas` | Pedidos de venda | — / `VendaConfirmadaEvent` (→ JMS), `VendaCanceladaEvent` | `clientes`, `estoque` |

```
            clientes          estoque
               ▲             ▲   ▲   │ EstoqueBaixoEvent
               │ api     api │   │   ▼
               └── vendas ───┘   └── compras
```

## Estrutura de pacotes

```
com.empresa.erp
├── ErpApplication.java
├── shared                          ← shared kernel (mínimo)
│   ├── domain                      BaseEntity
│   │   ├── vo                      Dinheiro, Email
│   │   └── exception               DomainException (422), ConflitoDeNegocioException (409),
│   │                               EntidadeNaoEncontradaException (404)
│   ├── event                       DomainEventPublisher (porta), @Externalizado, @ListenerDeModulo
│   └── architecture                @Modulo
├── infrastructure                  ← plataforma (transversal)
│   ├── config                      JpaConfig, AsyncConfig, WebConfig, OpenApiConfig (grupo por módulo)
│   ├── datasource                  DataSourceConfig (Hikari, banco único)
│   ├── security                    SecurityConfig, SecurityProperties, Perfis, AuditorAware
│   ├── events                      SpringDomainEventPublisher (memória + outbox)
│   ├── outbox                      OutboxMessage, OutboxService, OutboxRelay (→ JMS)
│   ├── jms                         JmsConfig, IntegracaoExternaListener (simula sistema externo)
│   └── web                         GlobalExceptionHandler (ProblemDetail)
└── modules
    └── <modulo>
        ├── package-info.java       @Modulo(nome, dependeDe)
        ├── api                     PÚBLICO: fachada + records + exceções do contrato
        ├── events                  PÚBLICO: eventos publicados pelo módulo
        ├── domain                  entity, vo, repository (portas), service
        ├── application             service (+ *ApiImpl package-private), listener, dto, mapper
        └── infrastructure          persistence (*RepositoryImpl + jpa), web (controllers)
```

## Regras que o build garante (`ArquiteturaModularTest`)

1. Um módulo só usa outro se ele estiver em `@Modulo(dependeDe = ...)` no seu `package-info.java`.
2. Do outro módulo, só pode usar os pacotes `api` e `events`.
3. A camada `domain` nunca depende de outro módulo (a tradução entre módulos fica na `application`).
4. Sem ciclos entre módulos; `shared` não conhece módulos nem plataforma; módulos não conhecem a plataforma.

Para testar: crie em `vendas.application` um `import com.empresa.erp.modules.estoque.domain.entity.Produto;` e rode `mvn test`.

## Comunicação entre módulos

| Necessidade | Mecanismo | Transação |
|---|---|---|
| Resposta ou consistência imediata (validar cliente, baixar estoque) | Chamada à fachada `api` | **Mesma** transação: estoque insuficiente desfaz a confirmação da venda |
| Avisar outros módulos (estoque baixo → sugestão de reposição) | Evento + `@ListenerDeModulo` | **Após o commit**, assíncrono, transação própria (consistência eventual) |
| Avisar sistemas externos (faturamento, NF-e) | Evento com `@Externalizado("fila")` | Gravado no **outbox** na mesma transação; `OutboxRelay` envia ao JMS |

## Dados

Um banco, um schema por módulo: `clientes`, `estoque`, `compras`, `vendas` e `plataforma` (outbox).
Referências entre módulos são **apenas por ID** (`venda.cliente_id`, `item_venda.produto_id`), sem FK nem JOIN entre schemas;
itens de venda e compra guardam um *snapshot* de SKU, nome e preço.

## Executar

```bash
mvn spring-boot:run                                    # H2 em memória + Artemis embarcado
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
mvn verify                                             # arquitetura + domínio + fluxo + segurança
```

- Swagger (um grupo por módulo): http://localhost:8080/swagger-ui.html
- Console H2: http://localhost:8080/h2-console — JDBC URL `jdbc:h2:mem:erpdb`, usuário `sa`

| Usuário | Senha | Perfil |
|---|---|---|
| admin | admin123 | ADMIN |
| estoque | estoque123 | ESTOQUE |
| vendas | vendas123 | VENDAS |
| compras | compras123 | COMPRAS |

## Roteiro (curl)

```bash
A="-u admin:admin123 -H Content-Type:application/json"

curl $A -X POST localhost:8080/api/v1/fornecedores \
  -d '{"razaoSocial":"Distribuidora XPTO","cnpj":"11.222.333/0001-81","email":"contato@xpto.com.br"}'
curl $A -X POST localhost:8080/api/v1/produtos \
  -d '{"sku":"MOUSE-01","nome":"Mouse óptico","precoCusto":0,"precoVenda":49.90,"estoqueMinimo":5}'

curl $A -X POST localhost:8080/api/v1/compras \
  -d '{"fornecedorId":1,"itens":[{"produtoId":1,"quantidade":10,"custoUnitario":20.00}]}'
curl $A -X POST localhost:8080/api/v1/compras/1/recebimento        # estoque = 10; evento → outbox → JMS

curl $A -X POST localhost:8080/api/v1/clientes \
  -d '{"nome":"Maria Silva","cpf":"529.982.247-25","email":"maria@email.com",
       "endereco":{"logradouro":"Rua A","numero":"100","cidade":"Rio de Janeiro","uf":"RJ","cep":"20000-000"}}'
curl $A -X POST localhost:8080/api/v1/vendas -d '{"clienteId":1,"itens":[{"produtoId":1,"quantidade":6}]}'
curl $A -X POST localhost:8080/api/v1/vendas/1/confirmacao          # estoque = 4 → EstoqueBaixoEvent

curl $A localhost:8080/api/v1/compras/sugestoes-reposicao            # sugestão criada pelo módulo compras
curl $A localhost:8080/api/v1/produtos/1/movimentacoes               # kardex
```

No log aparecem as mensagens `[EXTERNO] ...` consumidas das filas JMS.

## Diferenças em relação ao documento

O documento de arquitetura cita o Spring Modulith; este projeto usa só Spring Boot. Equivalências:

| No documento (Spring Modulith) | Neste projeto (Spring Boot puro) |
|---|---|
| `@ApplicationModule(allowedDependencies = ...)` | `@Modulo(dependeDe = ...)` em `shared.architecture` |
| `ApplicationModules.verify()` | `ArquiteturaModularTest` (ArchUnit + leitura do `@Modulo`) |
| `@NamedInterface("api")` e `api/event` | Pacotes públicos por convenção: `api` e `events` (irmãos) |
| `@ApplicationModuleListener` | `@ListenerDeModulo` = `@Async` + `@TransactionalEventListener(AFTER_COMMIT)` + `@Transactional(REQUIRES_NEW)` |
| Event Publication Registry + `@Externalized` | Outbox próprio: `@Externalizado` + `OutboxService` + `OutboxRelay` agendado |

**Limitação conhecida:** eventos entre módulos (`@ListenerDeModulo`) trafegam em memória; se a aplicação cair entre o commit e a execução do listener, o evento se perde. Para reações que não podem se perder, use `@Externalizado` (outbox) ou estenda o outbox para também despachar eventos internos.

**Produção:** substitua `ddl-auto` por Flyway (uma pasta de migrations por módulo) e, com várias instâncias, proteja o `OutboxRelay` com lock distribuído (ex.: ShedLock).
