# Raízes do Nordeste — Back-End

API REST do sistema de pedidos multicanal (App, Totem, Balcão) da rede de restaurantes Raízes do Nordeste, desenvolvida em Java/Spring Boot seguindo os princípios de **Clean Architecture** (Domain → Application → Infrastructure → Presentation).

Projeto acadêmico da atividade prática "Roteiro de Atividade Prática 2026 — Projeto Back-End".

## Stack técnica

- **Java 25**
- **Spring Boot 4.1.1** (Web MVC, Data JPA, Security)
- **PostgreSQL 16** (via Docker)
- **Flyway** para versionamento de schema (`ddl-auto: validate` — o schema é sempre controlado por migration, nunca gerado automaticamente pelo Hibernate)
- **MapStruct** + **Lombok**
- **springdoc-openapi** (Swagger UI)
- **JWT** (io.jsonwebtoken) + **Spring Security** para autenticação/autorização

## Arquitetura

```
src/main/java/com/projeto/raizesnordeste/
├── domain/            # Entidades de domínio (POJOs puros) e enums de negócio
├── application/       # Casos de uso (ports + services)
├── infrastructure/     # Persistência JPA, adapters, segurança e configurações
└── presentation/       # Controllers REST, DTOs (records) e tratamento de exceções
```

Cada funcionalidade é implementada como uma fatia vertical completa passando por todas as camadas (ver `Usuario` como referência). Consulte `.claude/projeto/PROMPT.md` para os diagramas Mermaid (DER, arquitetura, fluxograma, casos de uso).

## Como executar

### Pré-requisitos
- JDK 25
- Docker e Docker Compose

### Passo a passo

1. Subir o banco de dados:
   ```bash
   docker-compose up -d
   ```
   Isso cria o banco `raizes_db` em `localhost:5432` (usuário `raizes_user` / senha `raizes_password`).

2. Rodar a aplicação (as migrations do Flyway são aplicadas automaticamente na subida):
   ```bash
   ./mvnw spring-boot:run
   ```

3. A API sobe em `http://localhost:8080`. A documentação Swagger fica disponível em:
   ```
   http://localhost:8080/swagger-ui.html
   ```

### Variáveis de ambiente

| Variável | Descrição | Default (dev) |
|---|---|---|
| `JWT_SECRET` | Chave (Base64) usada para assinar os tokens JWT (HS256) | valor de desenvolvimento embutido em `application.yaml` |
| `JWT_EXPIRATION_MS` | Tempo de expiração do token, em milissegundos | `3600000` (1 hora) |

Em produção, sobrescreva `JWT_SECRET` com uma chave própria (mínimo 256 bits em Base64).

## Autenticação e Autorização

A API utiliza **JWT** (Bearer token). O login não exige token; os demais endpoints protegidos exigem o header `Authorization: Bearer <token>`.

### Perfis de usuário (`PerfilUsuarioEnum`)
- `GERENTE` — administração completa (unidades, estoque, campanhas, exclusão de usuários).
- `ATENDENTE` — operações de atendimento/balcão.
- `CLIENTE` — autoatendimento (App/Totem), acesso aos próprios dados.

### Obtendo um token

```
POST /api/auth/login
Content-Type: application/json

{
  "email": "fulano@exemplo.com",
  "senha": "senha123"
}
```

Resposta:
```json
{
  "token": "<jwt>",
  "tipo": "Bearer",
  "perfil": "CLIENTE"
}
```

Use o token nas próximas requisições:
```
Authorization: Bearer <token>
```

### Regras de autorização já aplicadas

| Recurso | Método | Regra |
|---|---|---|
| `/api/usuarios` | `POST` | Público (autocadastro) |
| `/api/usuarios`, `/api/usuarios/{id}` | `GET` | `GERENTE` ou `ATENDENTE` |
| `/api/usuarios/{id}` | `PUT` | Qualquer usuário autenticado |
| `/api/usuarios/{id}` | `DELETE` | Somente `GERENTE` |
| `/api/unidades` | `POST` | Somente `GERENTE` |
| `/api/unidades`, `/api/unidades/{id}` | `GET` | Qualquer usuário autenticado |
| `/api/unidades/{id}` | `PUT`, `DELETE` | Somente `GERENTE` |
| `/api/produtos` | `POST` | `GERENTE` ou `ATENDENTE` |
| `/api/produtos`, `/api/produtos/{id}` | `GET` | Qualquer usuário autenticado |
| `/api/produtos/{id}` | `PUT`, `DELETE` | `GERENTE` ou `ATENDENTE` |
| `/api/estoques/movimentar` | `POST` | `GERENTE` ou `ATENDENTE` |
| `/api/estoques` | `POST` | Somente `GERENTE` |
| `/api/estoques`, `/api/estoques/{id}`, `/api/estoques/saldo` | `GET` | `GERENTE` ou `ATENDENTE` |
| `/api/estoques/{id}` | `DELETE` | Somente `GERENTE` |
| `/api/pedidos` | `POST` | Qualquer usuário autenticado |
| `/api/pedidos`, `/api/pedidos/{id}` | `GET` | `GERENTE` ou `ATENDENTE` |
| `/api/pedidos/{id}/status` | `PUT` | `GERENTE` ou `ATENDENTE` |
| `/api/pedidos/{id}/cancelar` | `POST` | `GERENTE` ou `ATENDENTE` |
| `/api/pagamentos` | `POST` | Qualquer usuário autenticado |
| `/api/pagamentos/**` | `GET` | `GERENTE` ou `ATENDENTE` |
| `/api/fidelidade/**` | `GET` | Qualquer usuário autenticado (UC7 do diagrama de casos de uso é atribuído ao Cliente) |
| `/api/campanhas` | `POST` | Somente `GERENTE` |
| `/api/campanhas`, `/api/campanhas/{id}`, `/api/campanhas/vigentes` | `GET` | Qualquer usuário autenticado |
| `/api/campanhas/{id}` | `PUT`, `DELETE` | Somente `GERENTE` |
| Qualquer outro endpoint não listado acima | — | Requer apenas autenticação (fallback) |

> Notas de backlog:
> - A busca de usuário por id (`GET /api/usuarios/{id}`) hoje exige `GERENTE`/`ATENDENTE` — não existe ainda um endpoint de "meu perfil" para que um `CLIENTE` consulte os próprios dados sem essas roles.
> - O pedido é criado com `idUsuario` explícito no corpo da requisição (não derivado do token JWT) — ainda não há extração automática do usuário autenticado a partir do token para preencher esse campo.
> - `GET /api/pedidos` hoje não filtra pedidos por dono (`CLIENTE` não consegue listar só os próprios pedidos) — está restrito a `GERENTE`/`ATENDENTE`.

As senhas são armazenadas com hash **BCrypt** (nunca em texto plano).

## Formato padrão de erro

Todas as respostas de erro seguem o formato:

```json
{
  "requestId": "uuid",
  "timestamp": "2026-09-16T12:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Usuário não encontrado",
  "path": "/api/usuarios/{id}",
  "details": []
}
```

`details` é preenchido com uma entrada por campo em erros de validação (400).

## Fluxo crítico do MVP: Controle de Estoque por Unidade

Fluxo de negócio obrigatório da Roteiro. Um `GERENTE` ou `ATENDENTE` registra a movimentação (`ENTRADA` ou `SAÍDA`) de um produto em uma unidade:

```
POST /api/estoques/movimentar
Authorization: Bearer <token>
Content-Type: application/json

{
  "idUnidade": "uuid-da-unidade",
  "idProduto": "uuid-do-produto",
  "quantidade": 10,
  "tipo": "SAIDA"
}
```

Regras aplicadas:
- **Consistência sob concorrência**: a leitura do saldo antes da atualização usa *lock pessimista* (`PESSIMISTIC_WRITE`) — duas movimentações simultâneas no mesmo par unidade/produto nunca corrompem o saldo.
- **Validação de saldo**: uma saída (`SAIDA`) maior que o saldo disponível retorna **409 Conflict** (não altera o estoque).
- **Auditoria automática**: toda entidade (`Usuario`, `Unidade`, `Produto`, `Estoque`, etc.) grava automaticamente `criadoPor`/`alteradoPor` com o e-mail do usuário autenticado (via Spring Data JPA Auditing) e `criadoEm`/`alteradoEm` com o timestamp — não é preciso fazer isso manualmente em cada service.
- **Consulta de saldo**: `GET /api/estoques/saldo?idUnidade=...&idProduto=...` retorna o saldo atual de um produto em uma unidade específica.

Consulte o fluxograma atualizado em `.claude/projeto/PROMPT.md` (seção "Fluxograma") para o desenho completo do fluxo, incluindo os casos de erro (404 unidade/produto/estoque inexistente, 409 saldo insuficiente).

## Pedidos multicanal (`canalPedido`)

```
POST /api/pedidos
Authorization: Bearer <token>
Content-Type: application/json

{
  "idUsuario": "uuid-do-usuario",
  "idUnidade": "uuid-da-unidade",
  "canalPedido": "TOTEM",
  "itens": [
    { "idProduto": "uuid-produto-1", "quantidade": 2 },
    { "idProduto": "uuid-produto-2", "quantidade": 1 }
  ],
  "pontosResgatados": 0
}
```

Regras aplicadas:
- **`canalPedido`** aceita `APP`, `TOTEM`, `BALCAO`, `PICKUP` ou `WEB`, e é filtrável na listagem: `GET /api/pedidos?canalPedido=TOTEM`.
- **`precoUnitario` de cada item é calculado no servidor** a partir do preço atual do produto (`Produto.preco`) — nunca confia em valor enviado pelo cliente.
- **Integração com Estoque (Sprint 4)**: ao criar o pedido, cada item gera uma movimentação de `SAIDA` no estoque da unidade (reaproveita `EstoqueService.movimentar`), com todas as garantias já existentes (lock pessimista, 409 se saldo insuficiente, 404 se não houver registro de estoque para o par unidade/produto).
- **`pontosResgatados` (opcional, integração com Fidelidade — Sprint 7)**: converte pontos em desconto sobre o valor do pedido (100 pontos = R$1,00) e debita o saldo do usuário na mesma transação. Retorna 409 se o desconto for maior que o valor do pedido, ou 409 se o saldo de pontos for insuficiente. Ver seção "Programa de Fidelidade" abaixo.
- **Desconto de campanha (automático, integração com Campanhas — Sprint 8)**: se houver campanha vigente (`ativa=true` e dentro do período), o sistema aplica automaticamente o maior `percentualDesconto` disponível **sobre o valor já com desconto de pontos**. Não é algo que o cliente escolhe — é aplicado transparentemente na criação do pedido. Ver seção "Campanhas e Promoções" abaixo.

### Modelo financeiro do pedido

Cada etapa do cálculo é **persistida separadamente** (não só o resultado final), para rastreabilidade — dado assim que um pedido é auditado meses depois, sem depender do estado atual (mutável) de pontos/campanhas:

| Campo | Significado |
|---|---|
| `valorBruto` | Soma dos itens, sem nenhum desconto |
| `valorDescontoPontos` | Valor em R$ abatido pelo resgate de pontos |
| `idCampanhaAplicada` | Campanha vigente aplicada no momento da criação (nullable) |
| `valorDescontoCampanha` | Valor em R$ abatido pela campanha |
| `valorTotal` | `valorBruto - valorDescontoPontos - valorDescontoCampanha` — é isso que o Pagamento (Sprint 6) cobra |

Ordem de cálculo: `valorBruto` → desconto de pontos (fixo, em R$) → desconto de campanha (percentual sobre o restante) → `valorTotal`.

- **Status inicial**: todo pedido nasce como `AGUARDANDO_PAGAMENTO`.
- **Atualização de status**: `PUT /api/pedidos/{id}/status`, bloqueada se o pedido já estiver `ENTREGUE` ou `CANCELADO`.
- **Cancelamento**: `POST /api/pedidos/{id}/cancelar` estorna (`ENTRADA`) o estoque de cada item, **estorna os pontos de fidelidade resgatados** (se houver) e marca o pedido como `CANCELADO`; também bloqueado se já finalizado. *O desconto de campanha não precisa de estorno — é só um percentual aplicado ao valor, não debita nenhum saldo separado.*
- Itens do pedido não têm endpoints próprios — são geridos como parte do agregado `Pedido` (criados junto no `POST`, sem CRUD independente).

## Pagamento (mock)

O gateway de pagamento é simulado (`MockPagamentoGatewayAdapter`, camada de infraestrutura — corresponde ao "Serviço Externo: Mock Pagamento" do diagrama de arquitetura em `.claude/projeto/PROMPT.md`). Para permitir testar os dois cenários de forma determinística (sem depender de aleatoriedade), o campo opcional `simularFalha` força a recusa:

```
POST /api/pagamentos
Authorization: Bearer <token>
Content-Type: application/json

{
  "idPedido": "uuid-do-pedido",
  "formaPagamento": "PIX",
  "simularFalha": false
}
```

**Cenário de sucesso** (`simularFalha: false` ou omitido) → `201 Created`, pagamento gravado com `statusPagamento: APROVADO`, e o pedido avança automaticamente para `COZINHA`.

**Cenário de recusa** (`simularFalha: true`) → `402 Payment Required`. O pagamento **é registrado** com `statusPagamento: RECUSADO` (auditoria da tentativa), o pedido é automaticamente cancelado (reaproveita `PedidoService.cancelar`) e o estoque de cada item é estornado — tudo na mesma transação, sem perder o registro do pagamento recusado (`@Transactional(noRollbackFor = PaymentRequiredException.class)`).

Outras regras:
- Só é possível pagar um pedido com status `AGUARDANDO_PAGAMENTO` (senão, `409 Conflict`).
- Um pedido só pode ter um pagamento (`409 Conflict` em tentativa duplicada) — reflete a relação `1:1` `Pedido`↔`Pagamento` do DER.
- `GET /api/pagamentos/pedido/{idPedido}` consulta o pagamento de um pedido específico.

## Programa de Fidelidade

**Adesão automática**: ao cadastrar (`POST /api/usuarios`) ou atualizar (`PUT /api/usuarios/{id}`) um usuário com `aceiteFidelidade: true`, o `ProgramaFidelidade` (saldo inicial `0`) é criado automaticamente na mesma transação — não existe endpoint manual de "aderir ao programa". A criação é idempotente (`UsuarioService` chama `criarPrograma`, que não duplica se o programa já existir).

**Acúmulo automático de pontos**: quando um pedido é atualizado para status `ENTREGUE` (`PUT /api/pedidos/{id}/status`), o sistema credita pontos automaticamente ao usuário do pedido — **1 ponto para cada R$ 1,00 do `valorTotal`** (arredondado para baixo). Se o usuário não participa do programa de fidelidade, a operação é ignorada silenciosamente (não impede a entrega do pedido).

**Resgate de pontos como desconto em pedido**: não existe endpoint de resgate avulso — o resgate acontece exclusivamente na criação do pedido, via o campo `pontosResgatados` (ver seção "Pedidos multicanal" acima). Conversão: **100 pontos = R$ 1,00 de desconto**. Se o pedido resgatado for cancelado (`POST /api/pedidos/{id}/cancelar`), os pontos são devolvidos automaticamente ao saldo do usuário.

Cada acúmulo/resgate/estorno gera uma entrada em `HistoricoPontos` (`GET /api/fidelidade/{idUsuario}/historico`, paginado). *Nota: o enum do schema só distingue `ACUMULADO`/`RESGATE` — o estorno por cancelamento é registrado como `ACUMULADO`, sem um tipo próprio.*

Consultas:
- `GET /api/fidelidade/{idUsuario}` — saldo atual e dados do programa.
- `GET /api/fidelidade/{idUsuario}/historico` — histórico paginado de acúmulos e resgates.

## Campanhas e Promoções

CRUD completo restrito a `GERENTE` (leitura liberada a qualquer autenticado):

```
POST /api/campanhas
Authorization: Bearer <token>
Content-Type: application/json

{
  "nome": "Semana do Nordeste",
  "percentualDesconto": 10.00,
  "dataInicio": "2026-09-01T00:00:00",
  "dataFim": "2026-09-30T23:59:59",
  "ativa": true
}
```

Regras:
- **Vigência**: `dataFim` deve ser posterior a `dataInicio` (400/409 caso contrário). Uma campanha só é considerada "vigente" quando `ativa=true` **e** o momento atual está entre `dataInicio` e `dataFim` — `ativa` sozinho não basta.
- `GET /api/campanhas/vigentes` — lista as campanhas vigentes agora, ordenadas pelo maior desconto.
- **Aplicação automática no Pedido**: ao criar um pedido, o sistema busca a campanha vigente com o **maior** `percentualDesconto` e aplica automaticamente (não é escolha do cliente) — ver seção "Pedidos multicanal" acima para a ordem de aplicação junto ao desconto de pontos.
- `PedidoResponse` expõe `idCampanhaAplicada` e `valorDescontoCampanha` (ambos persistidos no pedido — migration `V4` — não apenas calculados em memória) e `nomeCampanhaAplicada` (derivado por `JOIN` a partir de `idCampanhaAplicada` na leitura, nunca persistido — evita duplicar dado da campanha no pedido).

## Funcionalidades implementadas

- [x] CRUD de Usuário (`/api/usuarios`), com paginação, validação, CPF e e-mail únicos
- [x] Autenticação JWT + autorização por papel (`/api/auth/login`)
- [x] CRUD de Unidade (`/api/unidades`), com paginação, validação e CNPJ único
- [x] CRUD de Produto (`/api/produtos`), com paginação, validação de preço (> 0) e **categorização** (útil para cardápios)
- [x] CRUD de Estoque + fluxo crítico do MVP: movimentação de estoque por unidade com lock pessimista e auditoria automática (`/api/estoques`)
- [x] CRUD de Pedido/ItemPedido (`/api/pedidos`), com filtro por `canalPedido`, cálculo de valor total no servidor, integração com Estoque (débito/estorno) e atualização de status
- [x] Pagamento mock (`/api/pagamentos`), com cenários de aprovação e recusa determinísticos, integração com Pedido (avança para COZINHA ou cancela) e auditoria da tentativa recusada
- [x] Programa de Fidelidade (`/api/fidelidade`), com adesão automática no cadastro/atualização de usuário, acúmulo automático de pontos em pedidos entregues e resgate como desconto na criação do pedido
- [x] Campanhas e Promoções (`/api/campanhas`), com CRUD restrito a `GERENTE`, regra de vigência e aplicação automática de desconto percentual em pedidos (empilhado com o desconto de pontos)
- [x] Testes unitários dos services (regras de negócio: estoque insuficiente, CPF/CNPJ duplicado, pagamento recusado, etc.)
- [ ] Testes de integração dos controllers
- [ ] Coleção Postman/Insomnia

O desenvolvimento segue um plano de sprints incrementais — cada funcionalidade é entregue e validada isoladamente antes de avançar para a próxima.

## Testes

```bash
./mvnw test
```

Testes unitários dos `application/services` (JUnit 5 + Mockito + AssertJ, sem contexto Spring — dependências mockadas via `@Mock`/`@InjectMocks`), cobrindo as regras de negócio de cada service: CNPJ/CPF/email duplicado, estoque insuficiente na movimentação, pagamento recusado pelo gateway (cancelamento do pedido + estorno de estoque), resgate/estorno de pontos de fidelidade, vigência de campanhas e o cálculo de descontos empilhados (pontos → campanha) na criação de pedidos.

Testes de integração dos controllers (`@SpringBootTest` + `MockMvc`) ainda estão pendentes — ver roadmap acima.
