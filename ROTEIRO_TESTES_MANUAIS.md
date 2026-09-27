# 📋 Roteiro de Testes Manuais - Raízes do Nordeste

**Objetivo:** Testar os fluxos da API via Swagger UI (`http://localhost:8080/swagger-ui.html`), validando regras de negócio, payloads (conforme os records de request) e permissões (conforme `SecurityConfig`).

> ⚠️ **Pré-requisito:** este roteiro assume o banco recém-migrado com o seed `V5__seed_dados_exemplo.sql` (Flyway `clean` + `migrate`), e que os testes são executados **em sequência**, de cima para baixo. Vários valores esperados (saldo de fidelidade, estoque) são cumulativos e dependem dos passos anteriores.

---

## 🔐 Credenciais de Teste (seed)

| Perfil        | Email                          | Senha      | CPF             | ID UUID                                |
|---------------|---------------------------------|------------|-----------------|-----------------------------------------|
| **GERENTE**   | `gerente@raizesnordeste.com`    | `Senha123` | 123.456.789-09  | `00000000-0000-0000-0000-000000000001` |
| **ATENDENTE** | `atendente@raizesnordeste.com`  | `Senha123` | 398.457.680-32  | `00000000-0000-0000-0000-000000000002` |
| **CLIENTE**   | `cliente@raizesnordeste.com`    | `Senha123` | 987.654.321-00  | `00000000-0000-0000-0000-000000000003` |

---

## 🔒 Matriz de Permissões (fonte: `SecurityConfig.java`)

| Recurso | Método | Regra |
|---|---|---|
| `/api/auth/**` | * | Público |
| `/api/usuarios` | POST | Público (autocadastro) |
| `/api/usuarios`, `/api/usuarios/**` | GET | `GERENTE` ou `ATENDENTE` (⚠️ CLIENTE **não** pode nem consultar o próprio perfil por aqui) |
| `/api/usuarios/**` | PUT | Qualquer usuário autenticado (⚠️ **sem checagem de dono** — ver seção de observações) |
| `/api/usuarios/**` | DELETE | `GERENTE` |
| `/api/unidades` | POST/PUT/DELETE | `GERENTE` |
| `/api/unidades`, `/api/unidades/**` | GET | Qualquer autenticado |
| `/api/produtos` | POST/PUT/DELETE | `GERENTE` ou `ATENDENTE` |
| `/api/produtos`, `/api/produtos/**` | GET | Qualquer autenticado (⚠️ **não** é público — precisa de token) |
| `/api/estoques` | POST | `GERENTE` |
| `/api/estoques/movimentar` | POST | `GERENTE` ou `ATENDENTE` |
| `/api/estoques`, `/api/estoques/**` | GET | `GERENTE` ou `ATENDENTE` |
| `/api/estoques/**` | DELETE | `GERENTE` |
| `/api/pedidos` | POST | Qualquer autenticado |
| `/api/pedidos`, `/api/pedidos/**` | GET | `GERENTE` ou `ATENDENTE` (⚠️ CLIENTE **não** pode consultar nem o próprio pedido) |
| `/api/pedidos/*/status` | PUT | `GERENTE` ou `ATENDENTE` |
| `/api/pedidos/*/cancelar` | POST | `GERENTE` ou `ATENDENTE` |
| `/api/pagamentos` | POST | Qualquer autenticado |
| `/api/pagamentos/**` | GET | `GERENTE` ou `ATENDENTE` (⚠️ CLIENTE **não** pode consultar o próprio pagamento) |
| `/api/fidelidade/**` | GET | Qualquer autenticado |
| `/api/campanhas` | POST/PUT/DELETE | `GERENTE` |
| `/api/campanhas`, `/api/campanhas/**` | GET | Qualquer autenticado |

---

## ⚠️ Comportamentos importantes da regra de negócio (leia antes de testar)

1. **Desconto de campanha é automático, não é escolha do cliente.** `PedidoService.save()` chama `campanhaPort.findMelhorVigente()` para **todo** pedido novo e aplica o desconto se houver campanha vigente — o request de criação de pedido (`CreatedPedidoRequest`) **não tem campo `idCampanhaAplicada`**. Como a campanha seed "Sexta Nordestina" (10%) fica vigente por 30 dias a partir do seed, ela será aplicada em **todos** os pedidos criados durante os testes, mesmo sem o cliente "pedir" desconto.
2. **Item do pedido só recebe `idProduto` e `quantidade`.** `CreatedItemPedidoRequest` não aceita `nomeProduto`/`precoUnitario` no payload — esses campos são preenchidos pelo backend a partir do cadastro do produto.
3. **O gateway de pagamento mock aprova por padrão.** `MockPagamentoGatewayAdapter` só recusa quando o campo opcional `simularFalha: true` é enviado em `CreatedPagamentoRequest` — a forma de pagamento (PIX, DINHEIRO etc.) não influencia a aprovação.
4. **`transacaoGatewayId` tem o formato `MOCK-<uuid>`**, não `TX-xxxxx`.
5. **Regras de negócio violadas retornam `409 CONFLICT`, não `400`.** `BusinessRuleException` é mapeada para 409 em `GlobalExceptionHandler` (saldo insuficiente, desconto maior que o total, pedido já finalizado, estoque insuficiente, CPF/e-mail/CNPJ duplicado, pagamento duplicado etc.). Só falhas de `@Valid` (bean validation) retornam `400`.
6. **Nenhum estado ordenado de status é imposto**, além de bloquear pedidos já `CANCELADO`/`ENTREGUE`. Ou seja, tecnicamente dá para ir de `AGUARDANDO_PAGAMENTO` direto para `ENTREGUE` via `PUT /status`, sem passar por `COZINHA`/`PRONTO`.
7. **O seed já carrega 3 pedidos, 2 pagamentos aprovados, 1 recusado e um histórico de fidelidade** — a cliente Juliana **não** começa "zerada": saldo inicial de pontos = **45**.

---

## 📍 Dados de Referência (Seed — `V5__seed_dados_exemplo.sql`)

### Unidades
| Nome | ID UUID | CNPJ |
|---|---|---|
| Raízes do Nordeste - Centro | `00000000-0000-0000-0000-000000000010` | 60.123.456/0001-70 |
| Raízes do Nordeste - Boa Viagem | `00000000-0000-0000-0000-000000000011` | 82.944.567/0001-80 |

### Produtos
| Nome | Preço | Categoria | ID UUID |
|---|---|---|---|
| Baião de Dois | R$32,90 | PRATO_PRINCIPAL | `...20` |
| Macaxeira Frita | R$12,50 | ACOMPANHAMENTO | `...21` |
| Suco de Caju | R$8,00 | BEBIDA | `...22` |
| Bolo de Rolo | R$9,90 | SOBREMESA | `...23` |
| Caldinho de Feijão | R$7,50 | ENTRADA | `...24` |
| Tapioca com Coco | R$15,90 | LANCHE | `...25` |
| Combo Festival Nordestino | R$45,00 | OUTROS | `...26` |

(IDs completos: `00000000-0000-0000-0000-000000000020` a `...026`)

### Estoque inicial
| Unidade | Produto | Quantidade |
|---|---|---|
| Centro | Baião de Dois | 40 |
| Centro | Macaxeira Frita | 25 |
| Centro | Suco de Caju | 60 |
| Centro | Bolo de Rolo | 15 |
| Centro | Caldinho de Feijão | 30 |
| Centro | Tapioca com Coco | 20 |
| Centro | Combo Festival Nordestino | 5 |
| Boa Viagem | Baião de Dois | 28 |
| Boa Viagem | Suco de Caju | 50 |
| Boa Viagem | Tapioca com Coco | 18 |
| Boa Viagem | Combo Festival Nordestino | 0 |

Centro tem estoque para os 7 produtos; Boa Viagem só para 4.

### Campanhas
| Nome | Desconto | Vigência | Status | ID UUID |
|---|---|---|---|---|
| Sexta Nordestina | 10% | hoje-2d a hoje+30d | `ativa=true` (vigente) | `...050` |
| Promoção de Verão | 15% | hoje-60d a hoje-30d | `ativa=true`, mas **expirada** | `...051` |
| Campanha Desativada | 20% | hoje-10d a hoje+10d | `ativa=false` (dentro do período, mas inativa) | `...052` |

### Pedidos já existentes (Juliana / Cliente)
| ID | Unidade | Canal | Status | Valor Total | Observação |
|---|---|---|---|---|---|
| `...060` | Centro | APP | ENTREGUE | R$65,80 | Gerou 65 pontos de fidelidade |
| `...061` | Centro | TOTEM | COZINHA | R$21,33 | 20 pontos resgatados + campanha |
| `...062` | Boa Viagem | BALCAO | CANCELADO | R$32,90 | Pagamento em DINHEIRO recusado |

### Fidelidade (Juliana)
Saldo atual: **45 pontos** (65 acumulados no pedido `...060`, − 20 resgatados no pedido `...061`).

---

## 🧪 Fluxo de Testes

### **FASE 1: Autenticação**

#### Teste 1.1 a 1.3: Login (Cliente / Atendente / Gerente)
```
POST /api/auth/login
Content-Type: application/json

{
  "email": "cliente@raizesnordeste.com",
  "senha": "Senha123"
}
```
**Esperado:** Status 200, corpo com `token` (JWT). Repita trocando o email para `atendente@...` e `gerente@...`. Guarde os 3 tokens.

#### Teste 1.4: Login com Credenciais Inválidas
```
POST /api/auth/login
Content-Type: application/json

{
  "email": "invalido@email.com",
  "senha": "SenhaErrada"
}
```
**Esperado:** Status 401 (`BadCredentialsException` → "Email ou senha inválidos").

---

### **FASE 2: Consultar Cardápio**

> Requer token — `GET /api/produtos` **não** é rota pública no `SecurityConfig` (só `/api/auth/**` e Swagger são `permitAll`).

#### Teste 2.1: Listar Produtos
```
GET /api/produtos
Authorization: Bearer <QUALQUER_TOKEN>
```
**Esperado:** Status 200, 7 produtos.

#### Teste 2.2: Sem Token
```
GET /api/produtos
```
**Esperado:** Status 401 Unauthorized.

#### Teste 2.3: Consultar Produto Específico
```
GET /api/produtos/00000000-0000-0000-0000-000000000020
Authorization: Bearer <QUALQUER_TOKEN>
```
**Esperado:** Status 200, Baião de Dois (R$32,90, PRATO_PRINCIPAL).

---

### **FASE 3: Gerenciar Unidades e Consultar Estoque**

#### Teste 3.1: Listar Unidades
```
GET /api/unidades
Authorization: Bearer <QUALQUER_TOKEN>
```
**Esperado:** Status 200, 2 unidades (rota liberada para qualquer autenticado, não só GERENTE).

#### Teste 3.2: Saldo de Estoque de um Produto na Unidade (Centro / Baião)
```
GET /api/estoques/saldo?idUnidade=00000000-0000-0000-0000-000000000010&idProduto=00000000-0000-0000-0000-000000000020
Authorization: Bearer <ATENDENTE_TOKEN>
```
**Esperado:** Status 200, quantidade = 40.
> `GET /api/estoques` (listagem paginada) **não** tem filtro por unidade — só existe o endpoint `/saldo` para consultar unidade+produto específicos.

#### Teste 3.3: Saldo de Estoque Inexistente (Combo Festival em Boa Viagem tem registro com quantidade 0, mas existe)
```
GET /api/estoques/saldo?idUnidade=00000000-0000-0000-0000-000000000011&idProduto=00000000-0000-0000-0000-000000000021
Authorization: Bearer <ATENDENTE_TOKEN>
```
**Esperado:** Status 404 (Macaxeira Frita não tem registro de estoque em Boa Viagem).

#### Teste 3.4: Tentar Consultar Estoque como CLIENTE
```
GET /api/estoques/saldo?idUnidade=00000000-0000-0000-0000-000000000010&idProduto=00000000-0000-0000-0000-000000000020
Authorization: Bearer <CLIENTE_TOKEN>
```
**Esperado:** Status 403 Forbidden (rota exige GERENTE ou ATENDENTE).

---

### **FASE 4: Fluxo de Pedido — Cenário 1 (com desconto automático de campanha)**

**Cenário:** Juliana (cliente) faz um pedido via APP no Centro, sem resgatar pontos.

#### Teste 4.1: Criar Pedido
```
POST /api/pedidos
Authorization: Bearer <CLIENTE_TOKEN>
Content-Type: application/json

{
  "idUsuario": "00000000-0000-0000-0000-000000000003",
  "idUnidade": "00000000-0000-0000-0000-000000000010",
  "canalPedido": "APP",
  "itens": [
    { "idProduto": "00000000-0000-0000-0000-000000000020", "quantidade": 1 },
    { "idProduto": "00000000-0000-0000-0000-000000000022", "quantidade": 2 }
  ],
  "pontosResgatados": 0
}
```
**Esperado:**
- Status 201, pedido criado com status `AGUARDANDO_PAGAMENTO`
- Valor Bruto: R$48,90 (1×32,90 + 2×8,00)
- Desconto Pontos: R$0,00
- Desconto Campanha: **R$4,89** (10% de 48,90 — "Sexta Nordestina" aplicada automaticamente)
- Valor Total: **R$44,01**
- `idCampanhaAplicada` no response = id da campanha `...050`
- Estoque do Baião no Centro: 40 → 39
- Estoque do Suco no Centro: 60 → 58

**Copie o ID do pedido (`<PEDIDO_1_ID>`).**

#### Teste 4.2: Consultar Pedido Criado (precisa ser ATENDENTE/GERENTE)
```
GET /api/pedidos/<PEDIDO_1_ID>
Authorization: Bearer <ATENDENTE_TOKEN>
```
**Esperado:** Status 200. (Com `CLIENTE_TOKEN` daria 403 — GET de pedidos é restrito a GERENTE/ATENDENTE.)

#### Teste 4.3: Solicitar Pagamento (PIX, aprovado)
```
POST /api/pagamentos
Authorization: Bearer <CLIENTE_TOKEN>
Content-Type: application/json

{
  "idPedido": "<PEDIDO_1_ID>",
  "formaPagamento": "PIX"
}
```
**Esperado:**
- Status 201, `statusPagamento: APROVADO`
- `transacaoGatewayId` no formato `MOCK-<uuid>`
- Pedido avança automaticamente para `COZINHA`

#### Teste 4.4: Atualizar Status para PRONTO (Atendente)
```
PUT /api/pedidos/<PEDIDO_1_ID>/status
Authorization: Bearer <ATENDENTE_TOKEN>
Content-Type: application/json

{ "status": "PRONTO" }
```
**Esperado:** Status 200, pedido `PRONTO`.

#### Teste 4.5: Atualizar Status para ENTREGUE (Atendente)
```
PUT /api/pedidos/<PEDIDO_1_ID>/status
Authorization: Bearer <ATENDENTE_TOKEN>
Content-Type: application/json

{ "status": "ENTREGUE" }
```
**Esperado:**
- Status 200, pedido `ENTREGUE`
- Fidelidade: Juliana ganha `floor(44.01) = 44` pontos → saldo vai de **45 para 89**

---

### **FASE 5: Fidelidade — Consultar Saldo e Histórico**

> Endpoints reais: `GET /api/fidelidade/{idUsuario}` e `GET /api/fidelidade/{idUsuario}/historico` — não existe `/api/fidelidade/pontos`.

#### Teste 5.1: Consultar Saldo de Pontos
```
GET /api/fidelidade/00000000-0000-0000-0000-000000000003
Authorization: Bearer <CLIENTE_TOKEN>
```
**Esperado:** Status 200, `saldoPontos: 89`.

#### Teste 5.2: Consultar Histórico de Pontos
```
GET /api/fidelidade/00000000-0000-0000-0000-000000000003/historico?page=0&linesPerPage=10
Authorization: Bearer <CLIENTE_TOKEN>
```
**Esperado:** Status 200, 3 registros (65 ACUMULADO do seed, 20 RESGATE do seed, 44 ACUMULADO do Teste 4.5), mais recente primeiro (`direction=DESC` é o default).

---

### **FASE 6: Fluxo de Pedido — Cenário 2 (Resgate de Pontos + Campanha)**

**Cenário:** Juliana faz um novo pedido via TOTEM no Centro, resgatando 30 pontos.

#### Teste 6.1: Criar Pedido com Resgate de Pontos
```
POST /api/pedidos
Authorization: Bearer <CLIENTE_TOKEN>
Content-Type: application/json

{
  "idUsuario": "00000000-0000-0000-0000-000000000003",
  "idUnidade": "00000000-0000-0000-0000-000000000010",
  "canalPedido": "TOTEM",
  "itens": [
    { "idProduto": "00000000-0000-0000-0000-000000000025", "quantidade": 1 },
    { "idProduto": "00000000-0000-0000-0000-000000000022", "quantidade": 1 }
  ],
  "pontosResgatados": 30
}
```
**Esperado:**
- Status 201
- Valor Bruto: R$23,90 (15,90 + 8,00)
- Desconto Pontos: R$0,30 (30 pontos ÷ 100)
- Valor após desconto de pontos: R$23,60
- Desconto Campanha (10% automático): R$2,36
- Valor Total: **R$21,24**
- Fidelidade: saldo cai de 89 para **59** (resgatou 30)

**Copie o ID (`<PEDIDO_2_ID>`).**

#### Teste 6.2: Tentar Consultar Pagamento do Pedido 2 como CLIENTE (deve falhar)
```
GET /api/pagamentos/pedido/<PEDIDO_2_ID>
Authorization: Bearer <CLIENTE_TOKEN>
```
**Esperado:** Status 403 Forbidden — `GET /api/pagamentos/**` é restrito a GERENTE/ATENDENTE.

#### Teste 6.3: Consultar Pagamento do Pedido 2 (Atendente, ainda não existe)
```
GET /api/pagamentos/pedido/<PEDIDO_2_ID>
Authorization: Bearer <ATENDENTE_TOKEN>
```
**Esperado:** Status 404 (ainda não há pagamento para este pedido).

#### Teste 6.4: Solicitar Pagamento com Cartão
```
POST /api/pagamentos
Authorization: Bearer <CLIENTE_TOKEN>
Content-Type: application/json

{
  "idPedido": "<PEDIDO_2_ID>",
  "formaPagamento": "CARTAO_CREDITO"
}
```
**Esperado:** Status 201, `APROVADO`, pedido avança para `COZINHA`.

#### Teste 6.5: Atualizar direto para ENTREGUE (pulando PRONTO — não há checagem de sequência)
```
PUT /api/pedidos/<PEDIDO_2_ID>/status
Authorization: Bearer <ATENDENTE_TOKEN>
Content-Type: application/json

{ "status": "ENTREGUE" }
```
**Esperado:**
- Status 200, pedido `ENTREGUE` mesmo sem ter passado por `PRONTO`
- Fidelidade: ganha `floor(21.24) = 21` pontos → saldo de 59 para **80**

---

### **FASE 7: Fluxo de Pedido — Cenário 3 (Pagamento Recusado + Cancelamento)**

**Cenário:** Juliana faz um pedido via BALCÃO em Boa Viagem e o pagamento é recusado propositalmente.

#### Teste 7.1: Criar Pedido no Boa Viagem
```
POST /api/pedidos
Authorization: Bearer <CLIENTE_TOKEN>
Content-Type: application/json

{
  "idUsuario": "00000000-0000-0000-0000-000000000003",
  "idUnidade": "00000000-0000-0000-0000-000000000011",
  "canalPedido": "BALCAO",
  "itens": [
    { "idProduto": "00000000-0000-0000-0000-000000000020", "quantidade": 2 }
  ],
  "pontosResgatados": 0
}
```
**Esperado:**
- Status 201
- Valor Bruto: R$65,80 (2×32,90)
- Desconto Campanha (10% automático): R$6,58
- Valor Total: **R$59,22**
- Estoque do Baião em Boa Viagem: 28 → 26

**Copie o ID (`<PEDIDO_3_ID>`).**

#### Teste 7.2: Forçar Recusa do Pagamento (`simularFalha: true`)
```
POST /api/pagamentos
Authorization: Bearer <CLIENTE_TOKEN>
Content-Type: application/json

{
  "idPedido": "<PEDIDO_3_ID>",
  "formaPagamento": "DINHEIRO",
  "simularFalha": true
}
```
**Esperado:**
- Status **402 Payment Required** (`PaymentRequiredException`, não 201!)
- Pagamento gravado com `statusPagamento: RECUSADO`
- Pedido é **automaticamente cancelado** e o estoque é estornado (Baião em Boa Viagem volta de 26 para 28) — isso acontece dentro do próprio `processar` do pagamento, **não precisa chamar `/cancelar` manualmente**.

> Sem `simularFalha: true`, o gateway mock sempre aprova — não existe recusa automática por forma de pagamento.

#### Teste 7.3: Confirmar que o Pedido já está CANCELADO
```
GET /api/pedidos/<PEDIDO_3_ID>
Authorization: Bearer <ATENDENTE_TOKEN>
```
**Esperado:** Status 200, `status: CANCELADO`.

#### Teste 7.4: Tentar Cancelar um Pedido Já Cancelado
```
POST /api/pedidos/<PEDIDO_3_ID>/cancelar
Authorization: Bearer <ATENDENTE_TOKEN>
```
**Esperado:** Status **409 Conflict** (`BusinessRuleException`: "Não é possível alterar um pedido com status CANCELADO") — não 400.

#### Teste 7.5: Tentar Pagar um Pedido Já Cancelado
```
POST /api/pagamentos
Authorization: Bearer <CLIENTE_TOKEN>
Content-Type: application/json

{
  "idPedido": "<PEDIDO_3_ID>",
  "formaPagamento": "PIX"
}
```
**Esperado:** Status **409 Conflict** ("Pedido não está aguardando pagamento (status atual: CANCELADO)").

---

### **FASE 8: Gerenciar Campanhas (GERENTE)**

#### Teste 8.1: Listar Campanhas (paginado)
```
GET /api/campanhas
Authorization: Bearer <GERENTE_TOKEN>
```
**Esperado:** Status 200, 3 campanhas.

#### Teste 8.2: Listar Campanhas Vigentes
```
GET /api/campanhas/vigentes
Authorization: Bearer <GERENTE_TOKEN>
```
**Esperado:** Status 200, retorna uma **lista** (não paginada) contendo apenas "Sexta Nordestina" (10%, `ativa=true` e dentro do período — "Promoção de Verão" está fora do período e "Campanha Desativada" tem `ativa=false`).

#### Teste 8.3: Criar Nova Campanha (GERENTE)
```
POST /api/campanhas
Authorization: Bearer <GERENTE_TOKEN>
Content-Type: application/json

{
  "nome": "Black Friday Nordestina",
  "percentualDesconto": 25.00,
  "dataInicio": "2026-11-01T00:00:00",
  "dataFim": "2026-11-30T23:59:59",
  "ativa": true
}
```
**Esperado:** Status 201, campanha criada com ID retornado.
> ⚠️ Isso cria uma **segunda** campanha vigente hoje só se as datas cobrirem a data atual. Se sobrepuser "Sexta Nordestina" com desconto maior, `findMelhorVigente()` passa a escolher a nova campanha nos pedidos seguintes — atenção ao rodar Fases 4/6/7 novamente depois deste teste.

#### Teste 8.4: Tentar Criar Campanha como ATENDENTE (Deve Falhar)
```
POST /api/campanhas
Authorization: Bearer <ATENDENTE_TOKEN>
Content-Type: application/json

{
  "nome": "Campanha Indevida",
  "percentualDesconto": 5.00,
  "dataInicio": "2026-10-01T00:00:00",
  "dataFim": "2026-10-31T23:59:59",
  "ativa": true
}
```
**Esperado:** Status 403 Forbidden (só GERENTE cria campanha).

#### Teste 8.5: Validação de Vigência Inválida (dataFim antes de dataInicio)
```
POST /api/campanhas
Authorization: Bearer <GERENTE_TOKEN>
Content-Type: application/json

{
  "nome": "Campanha Invertida",
  "percentualDesconto": 10.00,
  "dataInicio": "2026-10-31T00:00:00",
  "dataFim": "2026-10-01T00:00:00",
  "ativa": true
}
```
**Esperado:** Status 409 Conflict ("A dataFim da campanha deve ser posterior à dataInicio").

---

### **FASE 9: Gerenciar Usuários**

#### Teste 9.1: Criar Novo Usuário (rota pública — autocadastro)
```
POST /api/usuarios
Content-Type: application/json

{
  "nome": "Marcos Novo Cliente",
  "cpf": "529.982.247-25",
  "email": "marcos.cliente@exemplo.com",
  "senha": "Senha123",
  "perfil": "CLIENTE",
  "aceiteLgpd": true,
  "aceiteFidelidade": true
}
```
**Esperado:** Status 201, usuário criado (não precisa de token — `POST /api/usuarios` é `permitAll`).

#### Teste 9.2: Listar Usuários (GERENTE ou ATENDENTE)
```
GET /api/usuarios
Authorization: Bearer <GERENTE_TOKEN>
```
**Esperado:** Status 200, lista com 4 usuários (3 do seed + o criado no Teste 9.1).

#### Teste 9.3: CLIENTE Tenta Consultar o Próprio Usuário (Deve Falhar)
```
GET /api/usuarios/00000000-0000-0000-0000-000000000003
Authorization: Bearer <CLIENTE_TOKEN>
```
**Esperado:** Status **403 Forbidden** — `GET /api/usuarios/**` exige GERENTE ou ATENDENTE; não há exceção de "ver o próprio perfil". Esse é um ponto a validar com o time: se a intenção é permitir que o cliente veja os próprios dados, falta essa regra no `SecurityConfig`/controller.

#### Teste 9.4: Atendente Consulta o Usuário Cliente
```
GET /api/usuarios/00000000-0000-0000-0000-000000000003
Authorization: Bearer <ATENDENTE_TOKEN>
```
**Esperado:** Status 200, dados de Juliana.

#### Teste 9.5: Atualizar Seu Próprio Perfil (Cliente)
```
PUT /api/usuarios/00000000-0000-0000-0000-000000000003
Authorization: Bearer <CLIENTE_TOKEN>
Content-Type: application/json

{
  "nome": "Juliana Cliente Pereira Atualizada"
}
```
**Esperado:** Status 200. Como o mapper ignora campos nulos (`NullValuePropertyMappingStrategy.IGNORE`), só o nome muda — não é preciso reenviar `email`/`cpf`.

#### Teste 9.6: CLIENTE Tenta Atualizar Outro Usuário (verificar comportamento real)
```
PUT /api/usuarios/00000000-0000-0000-0000-000000000001
Authorization: Bearer <CLIENTE_TOKEN>
Content-Type: application/json

{ "nome": "Alteração Indevida no Gerente" }
```
**Esperado real pela implementação atual: Status 200 (sucesso).** `PUT /api/usuarios/**` no `SecurityConfig` só exige `authenticated()`, sem checagem de dono, e nem o controller nem o `UsuarioService.update` comparam o `id` do path com o usuário autenticado. **Isso é um gap de autorização** — qualquer usuário logado pode editar o cadastro de qualquer outro. Reporte ao time antes de assumir que deveria dar 403.

#### Teste 9.7: Deletar Usuário (GERENTE)
```
DELETE /api/usuarios/00000000-0000-0000-0000-000000000002
Authorization: Bearer <GERENTE_TOKEN>
```
**Esperado:** Status 204. **Atenção:** o usuário `...002` é o ATENDENTE usado nos outros testes — prefira deletar o usuário criado no Teste 9.1 (`marcos.cliente@exemplo.com`) para não invalidar o restante do roteiro.

#### Teste 9.8: CLIENTE ou ATENDENTE Tenta Deletar Usuário
```
DELETE /api/usuarios/00000000-0000-0000-0000-000000000002
Authorization: Bearer <ATENDENTE_TOKEN>
```
**Esperado:** Status 403 Forbidden (`DELETE` exige `GERENTE`).

---

### **FASE 10: Gerenciar Estoque (GERENTE / ATENDENTE)**

#### Teste 10.1: Criar Registro de Estoque (GERENTE)
```
POST /api/estoques
Authorization: Bearer <GERENTE_TOKEN>
Content-Type: application/json

{
  "idUnidade": "00000000-0000-0000-0000-000000000011",
  "idProduto": "00000000-0000-0000-0000-000000000021",
  "quantidade": 15
}
```
**Esperado:** Status 201 (Macaxeira Frita ainda não tinha estoque em Boa Viagem).

#### Teste 10.2: Criar Estoque Duplicado (mesma unidade/produto)
```
POST /api/estoques
Authorization: Bearer <GERENTE_TOKEN>
Content-Type: application/json

{
  "idUnidade": "00000000-0000-0000-0000-000000000011",
  "idProduto": "00000000-0000-0000-0000-000000000021",
  "quantidade": 5
}
```
**Esperado:** Status 409 Conflict ("Já existe registro de estoque para esta unidade e produto").

#### Teste 10.3: Movimentar Estoque — Entrada (GERENTE ou ATENDENTE)
```
POST /api/estoques/movimentar
Authorization: Bearer <ATENDENTE_TOKEN>
Content-Type: application/json

{
  "idUnidade": "00000000-0000-0000-0000-000000000010",
  "idProduto": "00000000-0000-0000-0000-000000000020",
  "quantidade": 10,
  "tipo": "ENTRADA"
}
```
**Esperado:** Status 200, estoque do Baião no Centro sobe (39 → 49, considerando o débito já feito na Fase 4).

#### Teste 10.4: Movimentar Estoque — Saída Maior que o Disponível
```
POST /api/estoques/movimentar
Authorization: Bearer <ATENDENTE_TOKEN>
Content-Type: application/json

{
  "idUnidade": "00000000-0000-0000-0000-000000000010",
  "idProduto": "00000000-0000-0000-0000-000000000023",
  "quantidade": 999,
  "tipo": "SAIDA"
}
```
**Esperado:** Status 409 Conflict ("Quantidade insuficiente em estoque. Disponível: 15, solicitado: 999").

#### Teste 10.5: Tentar Criar Estoque como ATENDENTE
```
POST /api/estoques
Authorization: Bearer <ATENDENTE_TOKEN>
Content-Type: application/json

{
  "idUnidade": "00000000-0000-0000-0000-000000000010",
  "idProduto": "00000000-0000-0000-0000-000000000026",
  "quantidade": 50
}
```
**Esperado:** Status 403 Forbidden — `POST /api/estoques` (criação) é só GERENTE; note que `POST /api/estoques/movimentar` (usado nos testes 10.3/10.4) já aceita ATENDENTE.

---

### **FASE 11: Fluxo Completo Integrado**

#### Teste 11.1: Listar Todos os Pedidos (GERENTE)
```
GET /api/pedidos?page=0&linesPerPage=20&direction=DESC&orderBy=criadoEm
Authorization: Bearer <GERENTE_TOKEN>
```
**Esperado:** Status 200, 6 pedidos (3 do seed + os 3 criados nas Fases 4/6/7).

#### Teste 11.2: Filtrar Pedidos por Canal
```
GET /api/pedidos?canalPedido=APP
Authorization: Bearer <GERENTE_TOKEN>
```
**Esperado:** Status 200, apenas pedidos do canal APP (seed `...060` + Teste 4.1).

#### Teste 11.3: Saldo Final de Fidelidade
```
GET /api/fidelidade/00000000-0000-0000-0000-000000000003
Authorization: Bearer <GERENTE_TOKEN>
```
**Esperado:** Saldo = **80 pontos** (45 seed → 89 após Fase 4 → 59 após resgate na Fase 6 → 80 após entrega na Fase 6). A Fase 7 não altera pontos porque o pedido foi cancelado antes de chegar a `ENTREGUE`.

---

## 📊 Validações Críticas (com status HTTP corrigido)

| Cenário | Requisição | Resultado Esperado |
|---|---|---|
| Criar pedido sem itens | `POST /api/pedidos` com `itens: []` | **400** (falha de `@NotEmpty`, bean validation) |
| Resgatar mais pontos que o saldo | `pontosResgatados` > saldo atual | **409** (`BusinessRuleException`) |
| Desconto (pontos) maior que o valor do pedido | pontos resgatados custam mais que o valor bruto | **409** |
| Pagar pedido que não está aguardando pagamento | `POST /api/pagamentos` em pedido `CANCELADO`/`ENTREGUE` | **409** |
| Pagamento duplicado | 2º `POST /api/pagamentos` para o mesmo pedido | **409** |
| Pagamento recusado pelo gateway | `simularFalha: true` | **402 Payment Required** (não 400) |
| Alterar status de pedido finalizado | `PUT /status` ou `POST /cancelar` em pedido `CANCELADO`/`ENTREGUE` | **409** |
| Acesso sem token | Qualquer rota protegida sem header `Authorization` | **401** |
| Token inválido/expirado | `Authorization: Bearer <token inválido>` | **401** |
| Operação acima do nível de permissão | ex.: CLIENTE tenta `DELETE /api/usuarios/{id}` | **403** |
| Produto/pedido/usuário inexistente | UUID válido mas não cadastrado | **404** |
| Estoque insuficiente para saída/venda | Pedido ou movimentação além do disponível | **409** |
| CPF/e-mail/CNPJ duplicado | Cadastro de usuário/unidade repetindo documento | **409** |
| Vigência de campanha inválida | `dataFim` ≤ `dataInicio` | **409** |

---

## 🔧 Dicas Práticas

1. **Token JWT:** copie o token do login e use em `Authorization: Bearer <TOKEN>` nas próximas chamadas.
2. **Cálculo de descontos:** Desconto de Pontos = pontos ÷ 100; depois, Desconto de Campanha = valor restante × (percentual da melhor campanha vigente ÷ 100) — **sempre aplicado automaticamente**, sem campo no payload.
3. **Acúmulo de pontos:** só ocorre quando o pedido chega a `ENTREGUE`; valor = `floor(valorTotal)`.
4. **Estoque:** debita na criação do pedido; é estornado tanto no cancelamento manual (`POST /cancelar`) quanto no cancelamento automático por pagamento recusado.
5. **Campanha "melhor vigente":** maior `percentualDesconto` entre as campanhas com `ativa=true` e dentro do período — reavalie os cálculos deste roteiro se você criar uma campanha nova com desconto maior (ver aviso no Teste 8.3).
6. **Gateway de pagamento:** sempre aprova, a menos que `simularFalha: true` seja enviado.

---

## 📝 Checklist de Testes

- [ ] Os 3 logins do seed funcionam; login inválido retorna 401
- [ ] `GET /api/produtos` exige token (não é público)
- [ ] Pedido criado aplica desconto de campanha automaticamente, sem campo no payload
- [ ] Estoque reduz na criação do pedido e é estornado no cancelamento (manual e automático)
- [ ] Pagamento aprova por padrão; `simularFalha: true` força recusa (402) e cancela o pedido
- [ ] Regras de negócio violadas retornam 409, não 400
- [ ] Pontos de fidelidade acumulam só na entrega e podem ser resgatados na criação do pedido
- [ ] `GET /api/pedidos` e `GET /api/pagamentos/**` são restritos a GERENTE/ATENDENTE (CLIENTE recebe 403)
- [ ] `GET /api/usuarios/**` é restrito a GERENTE/ATENDENTE (CLIENTE não vê nem o próprio perfil)
- [ ] `PUT /api/usuarios/**` não valida o dono do recurso — reportar como gap
- [ ] `GET /api/campanhas/vigentes` retorna lista simples com a campanha de maior desconto vigente
- [ ] Rotas de escrita restritas (GERENTE/ATENDENTE conforme a matriz) recusam perfis sem permissão com 403

---

**Última Atualização:** 2026-09-27
**Status:** ✅ Revisado contra `SecurityConfig`, records de request, controllers, `GlobalExceptionHandler` e seed `V5__seed_dados_exemplo.sql`
