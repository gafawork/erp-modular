# Arquitetura DDD em Monolito Modular — `erp-ddd`

Variação da arquitetura clássica em camadas (ver `ARQUITETURA.md`), reorganizada em **módulos de negócio** dentro do pacote `modules`.

- **Stack:** Spring Boot 3.5 · Java 21 · Spring Data JPA · Spring Security · JMS · Spring Modulith 1.4 (opcional) · ArchUnit
- **Pacote base:** `com.empresa.erp`
- **Ideia central:** um único deployable (um JAR, um banco), mas dividido internamente em módulos com **fronteiras explícitas e verificadas por teste**, como se fossem microsserviços que compartilham o mesmo processo.

---

## 1. Da arquitetura clássica ao monolito modular

Na versão clássica, o código é dividido primeiro por **camada técnica** (`domain`, `application`, `infrastructure`) e só depois por assunto. Todo o domínio fica num pacote só, e nada impede que `Venda` acesse diretamente `Cliente` ou `Produto`.

No monolito modular, a divisão principal é por **capacidade de negócio** (*bounded context*). Cada módulo tem suas próprias camadas DDD e só expõe um contrato público pequeno.

```
CLÁSSICO (por camada)                    MONOLITO MODULAR (por módulo)

com.empresa.erp                          com.empresa.erp
├── domain                               ├── shared            ← shared kernel mínimo
│   ├── entity  (tudo junto)             ├── infrastructure    ← plataforma transversal
│   ├── vo                               └── modules
│   └── ...                                  ├── clientes   ┐
├── application                              ├── estoque    │ cada um com
│   └── ...                                  ├── compras    │ api / domain /
└── infrastructure                           └── vendas     ┘ application / infrastructure
    └── ...
```

| Aspecto | Clássico | Monolito modular |
|---|---|---|
| Critério de divisão | Camada técnica | Capacidade de negócio |
| Relacionamento entre agregados de áreas diferentes | `@ManyToOne` direto (`Venda → Cliente`) | **Somente por ID** (`Venda.clienteId`) |
| Comunicação entre áreas | Qualquer classe chama qualquer classe | Apenas pela **API pública** do módulo ou por **eventos** |
| Fronteiras | Convenção (fácil de quebrar) | **Verificadas por teste** (ArchUnit / Spring Modulith) |
| Banco de dados | Tabelas compartilhadas livremente | Cada módulo é dono das suas tabelas; sem FK entre módulos |
| Evolução para microsserviços | Reescrita | Extrair um módulo trocando a implementação da API por HTTP/mensageria |

---

## 2. Módulos do sistema

| Módulo | Responsabilidade (bounded context) | Agregados | Depende de |
|---|---|---|---|
| `clientes` | Cadastro de clientes | `Cliente` | — |
| `estoque` | Catálogo de produtos, saldo e kardex | `Produto`, `MovimentacaoEstoque` | — |
| `compras` | Fornecedores e pedidos de compra | `Fornecedor`, `Compra` | `estoque :: api` |
| `vendas` | Pedidos de venda | `Venda` | `clientes :: api`, `estoque :: api` |

### Grafo de dependências (acíclico)

```
                ┌────────────┐
                │  clientes  │
                └─────▲──────┘
                      │ api (consulta cliente)
┌────────────┐  ┌─────┴──────┐
│  compras   │  │   vendas   │
└─────┬──────┘  └─────┬──────┘
      │ api            │ api (consulta produto, baixa, estorno)
      │ (entrada,      │
      │  eventos)      │
      └──────►┌────────▼───┐
              │  estoque   │
              └────────────┘

   Eventos publicados:  vendas  → VendaConfirmadaEvent, VendaCanceladaEvent
                        compras → CompraRecebidaEvent
                        estoque → EstoqueBaixoEvent  (consumido por compras)
```

Regras do grafo:

- **Não pode haver ciclo.** `estoque` não conhece `compras` nem `vendas`: quem precisa do estoque chama a API dele. Quando o estoque precisa avisar alguém, publica um evento — e quem tem interesse depende da API do estoque para escutá-lo.
- **Módulos "de baixo" (`clientes`, `estoque`) são os mais estáveis** e não dependem de ninguém.

---

## 3. Árvore completa de pacotes

```
src/main/java/com/empresa/erp
├── ErpApplication.java
│
├── shared                                   ← SHARED KERNEL (o mínimo possível)
│   └── domain
│       ├── BaseEntity.java
│       ├── vo
│       │   ├── Dinheiro.java
│       │   └── Email.java
│       └── exception
│           ├── DomainException.java
│           ├── ConflitoDeNegocioException.java
│           └── EntidadeNaoEncontradaException.java
│
├── infrastructure                           ← PLATAFORMA (transversal a todos os módulos)
│   ├── config
│   │   ├── JpaConfig.java
│   │   ├── AsyncConfig.java
│   │   ├── WebConfig.java
│   │   └── OpenApiConfig.java
│   ├── datasource
│   │   └── DataSourceConfig.java
│   ├── security
│   │   ├── SecurityConfig.java
│   │   ├── SecurityProperties.java
│   │   ├── Perfis.java
│   │   └── SpringSecurityAuditorAware.java
│   ├── jms
│   │   └── JmsConfig.java
│   └── web
│       └── GlobalExceptionHandler.java
│
└── modules
    │
    ├── clientes
    │   ├── package-info.java                ← @ApplicationModule
    │   ├── api                              ← ÚNICA parte visível a outros módulos
    │   │   ├── package-info.java            ← @NamedInterface("api")
    │   │   ├── ClientesApi.java
    │   │   ├── ClienteResumo.java
    │   │   └── event
    │   │       └── ClienteInativadoEvent.java
    │   ├── domain
    │   │   ├── entity
    │   │   │   └── Cliente.java
    │   │   ├── vo
    │   │   │   ├── Cpf.java
    │   │   │   └── Endereco.java
    │   │   └── repository
    │   │       └── ClienteRepository.java
    │   ├── application
    │   │   ├── service
    │   │   │   ├── ClienteApplicationService.java
    │   │   │   └── ClientesApiImpl.java
    │   │   ├── dto
    │   │   │   ├── ClienteDto.java
    │   │   │   └── EnderecoDto.java
    │   │   └── mapper
    │   │       ├── ClienteMapper.java
    │   │       └── EnderecoMapper.java
    │   └── infrastructure
    │       ├── persistence
    │       │   ├── ClienteRepositoryImpl.java
    │       │   └── jpa
    │       │       └── ClienteJpaRepository.java
    │       └── web
    │           └── ClienteController.java
    │
    ├── estoque
    │   ├── package-info.java
    │   ├── api
    │   │   ├── package-info.java
    │   │   ├── EstoqueApi.java
    │   │   ├── ProdutoResumo.java
    │   │   ├── ItemMovimento.java
    │   │   ├── EstoqueInsuficienteException.java
    │   │   └── event
    │   │       └── EstoqueBaixoEvent.java
    │   ├── domain
    │   │   ├── entity
    │   │   │   ├── Produto.java
    │   │   │   └── MovimentacaoEstoque.java
    │   │   ├── vo
    │   │   │   ├── Sku.java
    │   │   │   └── TipoMovimentacao.java
    │   │   ├── repository
    │   │   │   ├── ProdutoRepository.java
    │   │   │   └── MovimentacaoEstoqueRepository.java
    │   │   └── service
    │   │       └── EstoqueDomainService.java
    │   ├── application
    │   │   ├── service
    │   │   │   ├── ProdutoApplicationService.java
    │   │   │   └── EstoqueApiImpl.java
    │   │   ├── dto
    │   │   │   ├── ProdutoDto.java
    │   │   │   └── EstoqueDto.java
    │   │   └── mapper
    │   │       └── ProdutoMapper.java
    │   └── infrastructure
    │       ├── persistence
    │       │   ├── ProdutoRepositoryImpl.java
    │       │   ├── MovimentacaoEstoqueRepositoryImpl.java
    │       │   └── jpa
    │       │       ├── ProdutoJpaRepository.java
    │       │       └── MovimentacaoEstoqueJpaRepository.java
    │       └── web
    │           └── ProdutoController.java
    │
    ├── compras
    │   ├── package-info.java
    │   ├── api
    │   │   ├── package-info.java
    │   │   └── event
    │   │       └── CompraRecebidaEvent.java
    │   ├── domain
    │   │   ├── entity
    │   │   │   ├── Fornecedor.java
    │   │   │   ├── Compra.java
    │   │   │   └── ItemCompra.java
    │   │   ├── vo
    │   │   │   ├── Cnpj.java
    │   │   │   └── StatusCompra.java
    │   │   └── repository
    │   │       ├── FornecedorRepository.java
    │   │       └── CompraRepository.java
    │   ├── application
    │   │   ├── service
    │   │   │   ├── FornecedorApplicationService.java
    │   │   │   └── CompraApplicationService.java
    │   │   ├── listener
    │   │   │   └── EstoqueBaixoListener.java
    │   │   ├── dto
    │   │   │   ├── FornecedorDto.java
    │   │   │   └── CompraDto.java
    │   │   └── mapper
    │   │       ├── FornecedorMapper.java
    │   │       └── CompraMapper.java
    │   └── infrastructure
    │       ├── persistence
    │       │   ├── FornecedorRepositoryImpl.java
    │       │   ├── CompraRepositoryImpl.java
    │       │   └── jpa
    │       │       ├── FornecedorJpaRepository.java
    │       │       └── CompraJpaRepository.java
    │       └── web
    │           ├── FornecedorController.java
    │           └── CompraController.java
    │
    └── vendas
        ├── package-info.java
        ├── api
        │   ├── package-info.java
        │   └── event
        │       ├── VendaConfirmadaEvent.java
        │       └── VendaCanceladaEvent.java
        ├── domain
        │   ├── entity
        │   │   ├── Venda.java
        │   │   └── ItemVenda.java
        │   ├── vo
        │   │   └── StatusVenda.java
        │   └── repository
        │       └── VendaRepository.java
        ├── application
        │   ├── service
        │   │   └── VendaApplicationService.java
        │   ├── dto
        │   │   └── VendaDto.java
        │   └── mapper
        │       └── VendaMapper.java
        └── infrastructure
            ├── persistence
            │   ├── VendaRepositoryImpl.java
            │   └── jpa
            │       └── VendaJpaRepository.java
            ├── messaging
            │   └── VendaEventsJmsPublisher.java   (opcional — integração externa)
            └── web
                └── VendaController.java
```

---

## 4. Pacotes de nível superior

### 4.1 `shared` — Shared Kernel

Código de domínio **compartilhado por todos os módulos**. É um contrato entre os módulos, então deve ser **mínimo e estável**: toda alteração aqui afeta o sistema inteiro.

| Pacote | Conteúdo | Por que é compartilhado |
|---|---|---|
| `shared.domain` | `BaseEntity` (id, `@Version`, auditoria) | Mesma infraestrutura de identidade e auditoria em todos os agregados |
| `shared.domain.vo` | `Dinheiro`, `Email` | Conceitos genéricos, com o mesmo significado em todos os contextos |
| `shared.domain.exception` | `DomainException` (→ 422), `ConflitoDeNegocioException` (→ 409), `EntidadeNaoEncontradaException` (→ 404) | Permite que o `GlobalExceptionHandler` trate erros de qualquer módulo sem conhecer nenhum deles |

**O que não entra no shared:** `Cpf` e `Endereco` (só `clientes` usa), `Cnpj` (só `compras`), enums de status, entidades. Na dúvida, o conceito fica no módulo; só sobe para o shared quando um segundo módulo realmente precisar.

> Regra: `shared` **nunca** depende de `modules`.

### 4.2 `infrastructure` — Plataforma

Configurações técnicas que valem para a aplicação inteira e **não pertencem a nenhum módulo**. Não contém regra de negócio nem conhece entidades dos módulos.

| Pacote | Responsabilidade |
|---|---|
| `infrastructure.config` | `JpaConfig` (transações, auditoria, varredura de repositórios em `modules.*.infrastructure.persistence.jpa`), `AsyncConfig` (`@EnableAsync` para listeners de eventos), `WebConfig` (paginação), `OpenApiConfig` (Swagger, com um grupo por módulo) |
| `infrastructure.datasource` | `DataSourceConfig` — um único `DataSource` HikariCP compartilhado (monolito = um banco) |
| `infrastructure.security` | Autenticação, perfis, `SecurityFilterChain`, `AuditorAware`. As regras de autorização por endpoint ficam nos controllers de cada módulo (`@PreAuthorize`) |
| `infrastructure.jms` | Conexão com o broker e conversor JSON. As **filas e publicadores** de cada módulo ficam no próprio módulo (`modules.vendas.infrastructure.messaging`) |
| `infrastructure.web` | `GlobalExceptionHandler`, que trata as exceções base do `shared` e devolve ProblemDetail (RFC 9457) |

### 4.3 `modules` — Módulos de negócio

Cada subpacote direto de `modules` é um módulo independente, com a **mesma estrutura interna**, descrita na seção 5.

---

## 5. Anatomia de um módulo

```
modules/<modulo>
├── package-info.java      ← declara o módulo e suas dependências permitidas
├── api                    ← PÚBLICO: contrato com os outros módulos
├── domain                 ← INTERNO: regras de negócio do módulo
├── application            ← INTERNO: casos de uso do módulo
└── infrastructure         ← INTERNO: persistência, web e mensageria do módulo
```

Somente o pacote `api` pode ser usado por outros módulos. Todo o resto é **interno**: classes em `domain`, `application` e `infrastructure` de um módulo nunca são importadas por outro módulo.

### 5.1 `api` — Contrato público do módulo

Funciona como a "interface REST" de um microsserviço, só que em Java, dentro do mesmo processo.

| Tipo de classe | Exemplo | Regras |
|---|---|---|
| **Fachada** (interface) | `EstoqueApi`, `ClientesApi` | Operações de alto nível que outros módulos podem chamar. A implementação fica em `application.service` |
| **Dados de leitura** (record) | `ProdutoResumo`, `ClienteResumo` | Projeções imutáveis. **Nunca** devolver a entidade JPA |
| **Comandos** (record) | `ItemMovimento` | Parâmetros de entrada das operações da fachada |
| **Exceções do contrato** | `EstoqueInsuficienteException` | Erros que quem chama precisa tratar |
| **Eventos** (`api.event`) | `VendaConfirmadaEvent`, `EstoqueBaixoEvent` | Fatos publicados pelo módulo. Records imutáveis, só com IDs e valores |

Exemplo — `estoque.api`:

```java
package com.empresa.erp.modules.estoque.api;

public interface EstoqueApi {

    ProdutoResumo buscarProduto(Long produtoId);

    /** Baixa todos os itens ou nenhum. Lança EstoqueInsuficienteException. */
    void baixar(String documentoReferencia, List<ItemMovimento> itens);

    void estornar(String documentoReferencia, List<ItemMovimento> itens);

    void registrarEntradaDeCompra(String documentoReferencia, List<ItemMovimento> itens);
}

public record ProdutoResumo(Long id, String sku, String nome, BigDecimal precoVenda, boolean ativo) { }

public record ItemMovimento(Long produtoId, int quantidade, BigDecimal custoUnitario) {
    public static ItemMovimento semCusto(Long produtoId, int quantidade) {
        return new ItemMovimento(produtoId, quantidade, null);
    }
}

public class EstoqueInsuficienteException extends ConflitoDeNegocioException { ... }
```

```java
package com.empresa.erp.modules.estoque.api.event;

public record EstoqueBaixoEvent(Long produtoId, String sku, int quantidadeAtual, int estoqueMinimo) { }
```

### 5.2 `domain` — Domínio do módulo

Mesmo papel da versão clássica (entidades, VOs, portas de repositório, serviços de domínio), mas **restrito ao contexto do módulo**.

| Subpacote | Conteúdo |
|---|---|
| `domain.entity` | Agregados e entidades internas do módulo |
| `domain.vo` | Value Objects específicos do contexto (ex.: `Cpf` em clientes, `Cnpj` em compras) |
| `domain.repository` | Portas de persistência dos agregados do módulo |
| `domain.service` | Regras que envolvem mais de um agregado **do mesmo módulo** |

**Mudança principal: referências entre módulos são feitas por ID**, nunca por associação JPA.

```java
// CLÁSSICO                                    // MONOLITO MODULAR
@ManyToOne                                     @Column(name = "cliente_id", nullable = false)
private Cliente cliente;                       private Long clienteId;

@ManyToOne                                     @Column(name = "produto_id", nullable = false)
private Produto produto;                       private Long produtoId;
                                               private String sku;          // snapshot
                                               private String nomeProduto;  // snapshot
                                               private Dinheiro precoUnitario;
```

O `ItemVenda` guarda uma **cópia (snapshot)** dos dados do produto no momento da venda. Isso é correto do ponto de vista do negócio — uma venda passada não deve mudar se o produto for renomeado — e elimina a necessidade de consultar outro módulo para exibir a venda.

### 5.3 `application` — Casos de uso do módulo

| Subpacote | Conteúdo |
|---|---|
| `application.service` | Serviços de aplicação (transação + orquestração) **e a implementação da fachada `api`** (`EstoqueApiImpl`) |
| `application.listener` | Consumidores de eventos de **outros módulos** (ex.: `compras` escuta `EstoqueBaixoEvent`) |
| `application.dto` | DTOs da API REST do módulo |
| `application.mapper` | Conversões DTO ⇄ domínio e domínio ⇄ records da `api` |

A implementação da fachada pode ser **package-private**, já que só o Spring precisa instanciá-la:

```java
package com.empresa.erp.modules.estoque.application.service;

@Service
@Transactional
@RequiredArgsConstructor
class EstoqueApiImpl implements EstoqueApi {

    private final ProdutoRepository produtoRepository;
    private final EstoqueDomainService estoqueDomainService;

    @Override
    @Transactional(readOnly = true)
    public ProdutoResumo buscarProduto(Long produtoId) {
        Produto p = produtoRepository.buscarPorId(produtoId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Produto", produtoId));
        return new ProdutoResumo(p.getId(), p.getSku().valor(), p.getNome(),
                p.getPrecoVenda().valor(), p.isAtivo());
    }

    @Override
    public void baixar(String referencia, List<ItemMovimento> itens) {
        estoqueDomainService.baixar(referencia, itens); // valida tudo antes de baixar
    }
    // estornar, registrarEntradaDeCompra...
}
```

### 5.4 `infrastructure` — Adaptadores do módulo

| Subpacote | Conteúdo |
|---|---|
| `infrastructure.persistence` | `*RepositoryImpl` (implementam as portas do `domain`) e `jpa/*JpaRepository` (Spring Data) — **somente das tabelas do módulo** |
| `infrastructure.web` | Controllers REST do módulo (`/api/v1/<recurso>`), com `@PreAuthorize` |
| `infrastructure.messaging` | (Opcional) publicação/consumo de mensagens com **sistemas externos** via JMS. Comunicação entre módulos internos usa eventos em memória (seção 6.2) |

### 5.5 `package-info.java` — Declaração do módulo

Com Spring Modulith, cada módulo declara suas dependências permitidas:

```java
// modules/vendas/package-info.java
@org.springframework.modulith.ApplicationModule(
        displayName = "Vendas",
        allowedDependencies = { "clientes :: api", "estoque :: api" })
package com.empresa.erp.modules.vendas;
```

```java
// modules/estoque/api/package-info.java
@org.springframework.modulith.NamedInterface("api")
package com.empresa.erp.modules.estoque.api;
```

Sem Spring Modulith, os `package-info.java` servem apenas de documentação, e a verificação é feita com ArchUnit (seção 8).

---

## 6. Comunicação entre módulos

Há duas formas, e a escolha depende de **quem precisa da resposta e quando**.

| | Chamada síncrona (fachada `api`) | Evento de domínio (`api.event`) |
|---|---|---|
| Quando usar | Quem chama **precisa da resposta** ou de **consistência imediata** (validar, reservar, rejeitar) | Quem publica **não precisa saber** quem reage; efeitos colaterais |
| Acoplamento | Quem chama conhece a API de quem é chamado | Quem publica não conhece quem consome |
| Transação | Mesma transação (rollback conjunto) | Transação separada, após o commit (consistência eventual) |
| Exemplos | `vendas → estoque.baixar(...)`, `vendas → clientes.buscarCliente(...)` | `VendaConfirmadaEvent`, `EstoqueBaixoEvent` |

### 6.1 Síncrona — exemplo: confirmar venda

A baixa de estoque precisa acontecer **junto** com a confirmação: se faltar produto, a venda não pode ser confirmada. Como é um monolito com um único banco, as duas operações rodam na mesma transação.

```java
package com.empresa.erp.modules.vendas.application.service;

@Service
@RequiredArgsConstructor
public class VendaApplicationService {

    private final VendaRepository vendaRepository;
    private final ClientesApi clientesApi;           // ← API de outro módulo
    private final EstoqueApi estoqueApi;             // ← API de outro módulo
    private final ApplicationEventPublisher eventos;
    private final VendaMapper mapper;

    @Transactional
    public VendaDto.Response criar(VendaDto.Request req) {
        ClienteResumo cliente = clientesApi.buscarCliente(req.clienteId());
        Venda venda = Venda.nova(cliente.id(), cliente.ativo());
        for (var item : req.itens()) {
            ProdutoResumo produto = estoqueApi.buscarProduto(item.produtoId());
            venda.adicionarItem(produto.id(), produto.sku(), produto.nome(), produto.ativo(),
                    Dinheiro.of(produto.precoVenda()), item.quantidade());
        }
        return mapper.toResponse(vendaRepository.salvar(venda));
    }

    @Transactional
    public VendaDto.Response confirmar(Long id) {
        Venda venda = obter(id);
        venda.confirmar();
        estoqueApi.baixar("VENDA-" + id, venda.itensParaMovimento());  // EstoqueInsuficiente → rollback
        eventos.publishEvent(new VendaConfirmadaEvent(id, venda.getClienteId(), venda.getValorTotal().valor()));
        return mapper.toResponse(venda);
    }
}
```

### 6.2 Assíncrona — eventos entre módulos

Os eventos entre módulos trafegam **em memória** com o `ApplicationEventPublisher` do Spring — não é preciso broker para isso dentro do monolito.

O consumidor usa `@ApplicationModuleListener` (Spring Modulith), que equivale a: `@Async` + `@TransactionalEventListener(AFTER_COMMIT)` + `@Transactional(REQUIRES_NEW)`. Ou seja, roda **depois** do commit de quem publicou, em outra transação: uma falha no consumidor não desfaz a venda.

```java
package com.empresa.erp.modules.compras.application.listener;

@Component
@RequiredArgsConstructor
class EstoqueBaixoListener {

    private final CompraApplicationService compras;

    @ApplicationModuleListener
    void on(EstoqueBaixoEvent evento) {
        compras.registrarSugestaoDeReposicao(evento.produtoId(), evento.estoqueMinimo());
    }
}
```

Sem Spring Modulith, use a combinação equivalente:

```java
@Async
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
@Transactional(propagation = Propagation.REQUIRES_NEW)
void on(EstoqueBaixoEvent evento) { ... }
```

### 6.3 Garantia de entrega — Event Publication Registry (outbox)

Um evento em memória se perde se a aplicação cair entre o commit e a execução do listener. O **Event Publication Registry** do Spring Modulith (`spring-modulith-starter-jpa`) resolve isso: grava cada publicação numa tabela **na mesma transação** do negócio e marca como concluída quando o listener termina. Publicações pendentes são reprocessadas na reinicialização. É o padrão **Transactional Outbox**, sem código adicional.

### 6.4 Integração com sistemas externos (JMS)

Quando outro sistema (faturamento, NF-e, BI) precisa do evento, ele é **externalizado** para uma fila JMS. Com Spring Modulith (`spring-modulith-events-jms`), basta anotar o evento:

```java
@Externalized("erp.vendas.confirmadas")
public record VendaConfirmadaEvent(Long vendaId, Long clienteId, BigDecimal valorTotal) { }
```

A externalização também passa pelo registry, então herda a garantia de entrega. Sem Spring Modulith, um listener em `modules.vendas.infrastructure.messaging` recebe o evento após o commit e chama o `JmsTemplate`.

### 6.5 Mapa de interações

| Origem | Destino | Tipo | Operação |
|---|---|---|---|
| vendas | clientes | Síncrono | `ClientesApi.buscarCliente` (validar cliente ativo) |
| vendas | estoque | Síncrono | `EstoqueApi.buscarProduto`, `baixar`, `estornar` |
| compras | estoque | Síncrono | `EstoqueApi.buscarProduto`, `registrarEntradaDeCompra` (custo médio) |
| estoque | compras | Evento | `EstoqueBaixoEvent` → sugestão de reposição |
| vendas | externo | Evento + JMS | `VendaConfirmadaEvent` → `erp.vendas.confirmadas` |
| compras | externo | Evento + JMS | `CompraRecebidaEvent` → `erp.compras.recebidas` |

---

## 7. Dados: cada módulo é dono das suas tabelas

Um único banco, mas com **propriedade clara** das tabelas.

| Módulo | Schema (PostgreSQL) | Tabelas |
|---|---|---|
| clientes | `clientes` | `cliente` |
| estoque | `estoque` | `produto`, `movimentacao` |
| compras | `compras` | `fornecedor`, `compra`, `item_compra` |
| vendas | `vendas` | `venda`, `item_venda` |
| plataforma | `public` | `event_publication` (registry do Spring Modulith) |

Regras:

1. **Só o módulo dono lê e escreve nas suas tabelas.** Outro módulo que precise do dado usa a `api`.
2. **Nada de FK entre schemas.** `vendas.item_venda.produto_id` é uma coluna simples, sem constraint para `estoque.produto`. A integridade é garantida pela aplicação (a `EstoqueApi` valida o produto).
3. **Nada de JOIN entre módulos.** Relatórios que cruzam módulos usam uma *read model* própria (alimentada por eventos) ou consultas na camada de relatórios, nunca dentro do domínio.
4. **Migrations por módulo:** `db/migration/clientes`, `db/migration/estoque`, etc., registradas no Flyway com `spring.flyway.locations`.

Com isso, extrair um módulo para um microsserviço vira "mover o schema para outro banco", e não "desembaraçar dezenas de JOINs".

---

## 8. Garantindo as fronteiras com testes

Sem verificação automática, fronteiras de pacote se deterioram em poucos meses. Duas opções complementares:

### 8.1 Spring Modulith

```java
class ModularidadeTest {

    // A raiz é o pacote "modules": cada subpacote direto vira um módulo
    static final ApplicationModules MODULOS = ApplicationModules.of("com.empresa.erp.modules");

    @Test
    void verificaFronteirasEDependencias() {
        MODULOS.verify();   // falha com acesso a pacote interno, dependência não declarada ou ciclo
    }

    @Test
    void geraDocumentacao() {
        new Documenter(MODULOS).writeDocumentation();   // diagramas C4/PlantUML em target/spring-modulith-docs
    }
}
```

Também permite testar um módulo isolado, subindo só os beans dele:

```java
@ApplicationModuleTest   // em modules/estoque/... → sobe apenas o módulo estoque
class EstoqueModuleIT { ... }
```

### 8.2 ArchUnit (sem depender do Spring Modulith)

```java
@AnalyzeClasses(packages = "com.empresa.erp")
class ArquiteturaModularTest {

    private static final String MODULOS = "com.empresa.erp.modules.";

    /** Um módulo só pode usar o pacote "api" de outro módulo. */
    @ArchTest
    static final ArchRule acessoEntreModulosSomentePelaApi = classes()
            .that().resideInAPackage("com.empresa.erp.modules..")
            .should(new ArchCondition<>("acessar outros módulos apenas pelo pacote api") {
                @Override
                public void check(JavaClass origem, ConditionEvents events) {
                    String moduloOrigem = modulo(origem.getPackageName());
                    for (Dependency dep : origem.getDirectDependenciesFromSelf()) {
                        String pacoteAlvo = dep.getTargetClass().getPackageName();
                        String moduloAlvo = modulo(pacoteAlvo);
                        boolean outroModulo = moduloAlvo != null && !moduloAlvo.equals(moduloOrigem);
                        if (outroModulo && !pacoteAlvo.startsWith(MODULOS + moduloAlvo + ".api")) {
                            events.add(SimpleConditionEvent.violated(dep, dep.getDescription()));
                        }
                    }
                }
            });

    @ArchTest
    static final ArchRule modulosSemCiclos = slices()
            .matching("com.empresa.erp.modules.(*)..")
            .should().beFreeOfCycles();

    @ArchTest
    static final ArchRule sharedNaoDependeDeModulos = noClasses()
            .that().resideInAPackage("com.empresa.erp.shared..")
            .should().dependOnClassesThat().resideInAPackage("com.empresa.erp.modules..");

    @ArchTest
    static final ArchRule dominioNaoDependeDasOutrasCamadas = noClasses()
            .that().resideInAPackage("..modules.*.domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..modules.*.application..", "..modules.*.infrastructure..",
                    "com.empresa.erp.infrastructure..");

    private static String modulo(String pacote) {
        if (!pacote.startsWith(MODULOS)) return null;
        String resto = pacote.substring(MODULOS.length());
        int ponto = resto.indexOf('.');
        return ponto < 0 ? resto : resto.substring(0, ponto);
    }
}
```

---

## 9. Dependências Maven adicionais

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.modulith</groupId>
            <artifactId>spring-modulith-bom</artifactId>
            <version>1.4.3</version>   <!-- linha 1.4.x, compatível com Spring Boot 3.5 -->
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <!-- Núcleo + Event Publication Registry em JPA (outbox) -->
    <dependency>
        <groupId>org.springframework.modulith</groupId>
        <artifactId>spring-modulith-starter-jpa</artifactId>
    </dependency>
    <!-- Externalização de eventos para JMS -->
    <dependency>
        <groupId>org.springframework.modulith</groupId>
        <artifactId>spring-modulith-events-jms</artifactId>
    </dependency>
    <!-- Verificação de módulos e testes por módulo -->
    <dependency>
        <groupId>org.springframework.modulith</groupId>
        <artifactId>spring-modulith-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <!-- Alternativa/complemento para regras de arquitetura -->
    <dependency>
        <groupId>com.tngtech.archunit</groupId>
        <artifactId>archunit-junit5</artifactId>
        <version>1.4.1</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

> Confira as versões mais recentes das linhas 1.4.x (Modulith) e 1.4.x (ArchUnit) no momento da adoção.

---

## 10. Fluxo completo — confirmar venda

```
POST /api/v1/vendas/42/confirmacao
 │
 ├─ infrastructure.security        autentica; @PreAuthorize("hasAnyRole('ADMIN','VENDAS')")
 ├─ vendas.infrastructure.web      VendaController.confirmar(42)
 ├─ vendas.application.service     VendaApplicationService.confirmar ── BEGIN TX
 │     ├─ vendas.domain            Venda.confirmar()                 (PENDENTE → CONFIRMADA)
 │     ├─ estoque.api              EstoqueApi.baixar("VENDA-42", itens)
 │     │     └─ estoque (interno)  valida todos os saldos → baixa → kardex
 │     │                           saldo ≤ mínimo → publica EstoqueBaixoEvent
 │     └─ publica VendaConfirmadaEvent
 │                                  registry grava as publicações na mesma TX
 │                                 ── COMMIT
 ├─ após o commit (assíncrono, outras transações):
 │     ├─ compras.application.listener   EstoqueBaixoListener → sugestão de reposição
 │     └─ externalização JMS             VendaConfirmadaEvent → fila erp.vendas.confirmadas
 └─ 200 OK                         (estoque insuficiente → 409 e rollback de tudo)
```

---

## 11. Evoluindo um módulo para microsserviço

Como as fronteiras já existem, extrair um módulo (ex.: `estoque`) é um trabalho mecânico:

1. Mover `modules/estoque` para um novo projeto Spring Boot, junto com seu schema de banco.
2. No monolito, manter `estoque.api` e trocar a implementação de `EstoqueApi` por um **cliente HTTP** (`RestClient`/OpenFeign) que chama o novo serviço.
3. Eventos publicados pelo estoque passam a chegar pelo broker (já externalizados via JMS/Kafka) em vez de em memória.
4. Revisar operações que hoje dependem da mesma transação (`baixar` junto com a confirmação da venda): entre serviços isso vira uma **saga** (reserva → confirmação/compensação).

O item 4 é o motivo para **não** extrair módulos sem necessidade real: a transação única é uma das maiores vantagens do monolito modular.

---

## 12. Onde colocar código novo

| Preciso de... | Onde |
|---|---|
| Uma nova área de negócio (ex.: financeiro) | Novo módulo `modules/financeiro` com a estrutura `api/domain/application/infrastructure` |
| Regra de negócio de um único agregado | `modules/<m>/domain/entity` |
| Regra entre agregados do **mesmo** módulo | `modules/<m>/domain/service` |
| Que outro módulo consulte ou execute algo | Novo método na fachada `modules/<m>/api` + implementação em `application.service` |
| Avisar outros módulos de um fato | Novo record em `modules/<m>/api/event` + `publishEvent` no serviço de aplicação |
| Reagir a um evento de outro módulo | `modules/<m>/application/listener` com `@ApplicationModuleListener` |
| Mandar um evento para um sistema externo | `@Externalized` no evento ou adaptador em `modules/<m>/infrastructure/messaging` |
| Um conceito usado por **vários** módulos | Só então em `shared` — e com cautela |
| Configuração técnica global | `infrastructure` (raiz) |
