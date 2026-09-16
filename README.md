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

| Endpoint | Regra |
|---|---|
| `POST /api/usuarios` | Público (autocadastro) |
| `GET /api/usuarios` (listagem paginada) | `GERENTE` ou `ATENDENTE` |
| `DELETE /api/usuarios/{id}` | Somente `GERENTE` |
| Demais endpoints | Requer usuário autenticado |

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

## Funcionalidades implementadas

- [x] CRUD de Usuário (`/api/usuarios`), com paginação, validação e CPF único
- [x] Autenticação JWT + autorização por papel (`/api/auth/login`)
- [ ] CRUD de Unidade
- [ ] CRUD de Produto
- [ ] CRUD de Estoque + fluxo de controle de estoque por unidade (fluxo crítico do MVP)
- [ ] CRUD de Pedido/ItemPedido (com filtro por `canalPedido`)
- [ ] Pagamento (mock)
- [ ] Programa de Fidelidade
- [ ] Campanhas e Promoções
- [ ] Testes automatizados
- [ ] Coleção Postman/Insomnia

O desenvolvimento segue um plano de sprints incrementais — cada funcionalidade é entregue e validada isoladamente antes de avançar para a próxima.

## Testes

```bash
./mvnw test
```

(Suíte de testes ainda em construção — ver roadmap acima.)
