# InvestHub

**Gerenciador e Agregador de Investimentos** — API RESTful para cadastro de ativos financeiros, registro de aportes/transações e consolidação de carteira.

Repositório: [github.com/Guiantebii/investhub](https://github.com/Guiantebii/investhub)

O sistema prioriza **precisão financeira rigorosa**: valores monetários, preços unitários e quantidades são modelados com `BigDecimal` no Java e persistidos como `NUMERIC` no PostgreSQL, eliminando erros de arredondamento de ponto flutuante.

---

## Objetivo do projeto

Backend de portfólio pessoal **por usuário** (multi-carteira via JWT), com camadas claras (domínio → serviço → API), persistência relacional e cálculos **sem `double`**. Contrato HTTP via DTOs; entidades JPA ficam na camada de persistência.

---

## Tech stack

| Camada | Tecnologia |
|--------|------------|
| **Linguagem** | Java 17 |
| **Framework** | Spring Boot 4.x (Web MVC, Data JPA, Bean Validation, Security) |
| **Autenticação** | JWT (OAuth2 Resource Server, HS256) |
| **Documentação** | springdoc-openapi — Swagger UI / OpenAPI 3 |
| **Banco de dados** | PostgreSQL 16 (Alpine) |
| **Infraestrutura local** | Docker & Docker Compose |
| **Build / qualidade** | Maven Wrapper (`./mvnw`), JaCoCo (cobertura), GitHub Actions CI |
| **Testes** | Testcontainers, WireMock, MockMvc, Spring Security Test |

Pacote base: **`com.investhub.api`** · Classe principal: **`InvestHubApplication`**

---

## Estrutura do repositório

```text
gerenciador-investimentos/
├── README.md
├── .gitignore
├── .github/workflows/investhub-api-ci.yml
└── investhub-api/
    ├── docker-compose.yml
    ├── .env.example
    ├── pom.xml
    └── src/main/java/com/investhub/api/
        ├── config/          # Security, JWT, OpenAPI, cache, Brapi
        ├── controller/
        ├── domain/
        ├── dto/
        ├── exception/
        ├── repository/
        ├── security/
        ├── service/
        └── web/             # GlobalExceptionHandler, ApiErrorResponse
```

---

## Pré-requisitos

- **JDK 17+**
- **Docker** e **Docker Compose**
- **Git** (para clonar) · **Postman**, **curl** ou Swagger UI (para testar a API)

```bash
git clone https://github.com/Guiantebii/investhub.git
cd investhub/investhub-api
```

---

## 1. Variáveis de ambiente

Copie o modelo e ajuste os valores locais (o arquivo `.env` **não** vai para o Git):

```bash
cd investhub-api
cp .env.example .env
```

| Variável | Uso |
|----------|-----|
| `POSTGRES_*` | Credenciais e porta do container Docker |
| `INVESTHUB_DB_*` | JDBC da aplicação (deve bater com o Postgres local) |
| `INVESTHUB_JWT_SECRET` | Chave simétrica JWT (mín. **32 caracteres**) |

Antes de subir a API, exporte as variáveis (o Spring Boot **não** lê `.env` sozinho):

```bash
set -a && source .env && set +a
```

---

## 2. Subir o banco de dados (PostgreSQL — porta 5433)

O container usa a porta **5433** no host para não conflitar com Postgres na **5432**.

```bash
cd investhub-api
docker compose up -d
docker compose ps
```

| Parâmetro | Valor (dev — ver `.env.example`) |
|-----------|-------------------------------------|
| Host | `localhost:5433` |
| Banco | `gerenciador_investimentos` |
| Container | `gerenciador_investimentos_pg` |

Parar: `docker compose down` · Apagar volume: `docker compose down -v` (cuidado).

---

## 3. Rodar a aplicação

```bash
cd investhub-api
set -a && source .env && set +a
./mvnw spring-boot:run
```

Alternativa com build limpo:

```bash
set -a && source .env && set +a
./mvnw clean spring-boot:run
```

- **Base URL:** http://localhost:8080  
- Compilar sem executar: `./mvnw -DskipTests compile`

---

## 4. Documentação interativa (Swagger)

| Recurso | URL |
|---------|-----|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |

Use **Authorize** com `Bearer <accessToken>` (token obtido em `/auth/login`).

Metadados: **InvestHub API** · versão **1.0.0**

---

## 5. Autenticação (JWT)

Rotas de negócio (`/assets`, `/transactions`, `/portfolio`) exigem cabeçalho:

`Authorization: Bearer <accessToken>`

Endpoints públicos: `/auth/register`, `/auth/login`, Swagger e OpenAPI.

### Registro

`POST /auth/register` — cria usuário (e-mail único, senha com mínimo 8 caracteres). Resposta **201** sem expor o hash da senha.

### Login

`POST /auth/login` — valida credenciais e devolve:

```json
{
  "accessToken": "<jwt>",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

Token emitido com **HS256**; claim `sub` = id do usuário. Validade padrão: **1 hora** (`investhub.jwt.access-token-ttl`).

---

## 6. Isolamento de dados (multi-carteira — Fase 7)

Cada usuário autenticado opera **apenas sobre a própria carteira**. O escopo vem do JWT: serviços leem o `sub` (id do usuário) via `SecurityUtils` e filtram persistência por `user_id`.

| Regra | Comportamento |
|--------|----------------|
| **Ativos e transações** | Coluna `user_id` (FK → `users`), migration `V3__add_user_to_investments.sql` |
| **Symbol único** | Por usuário (`uk_assets_user_symbol`) — dois investidores podem cadastrar `PETR4` |
| **Consultas** | Repositórios exigem `userId` (`findByIdAndUser_Id`, etc.) |
| **Outro tenant** | ID de ativo/transação de outro usuário → **404** (sem vazar existência) |
| **Carteira** | `/portfolio/summary` consolida só ativos/transações do token atual |

Fluxo resumido:

```text
Authorization: Bearer <JWT>  →  sub = userId  →  services/repositories  →  WHERE user_id = ?
```

**Upgrade local:** a `V3` remove ativos/transações legados (pré multi-tenant). Após atualizar o código, recrie o volume se necessário:

```bash
cd investhub-api && docker compose down -v && docker compose up -d
```

---

## 7. Posição líquida e vendas (SELL — Fase 8)

O resumo da carteira e as validações de venda usam o **`AssetPositionCalculator`**: uma única fonte de verdade para quantidade, preço médio e valor investido (sempre com `BigDecimal`).

### Regras financeiras

| Conceito | Regra |
|----------|--------|
| **Quantidade líquida** | Σ quantidades **`BUY`** − Σ quantidades **`SELL`** (por ativo e usuário) |
| **Preço médio** | Σ (`quantity × unitPrice` + `fees`) das **compras** ÷ quantidade **comprada** — **não muda** ao registrar uma venda |
| **Total investido** | `quantidade líquida × preço médio` (custo da posição ainda em carteira) |
| **Mark-to-market** | Cotação Brapi × quantidade líquida; P/L sobre o `totalInvested` recalculado |

**Exemplo:** compra de 100 ações a R$ 10,00 (PM = 10) → venda de 40 → carteira mostra **60** ações, PM **10**, investido **600**.

### Validação de venda (`POST /transactions` com `SELL`)

Antes de persistir, o sistema calcula a posição líquida **atual** do ativo. Se a quantidade da venda for maior que a disponível (ou não houver posição), responde **422 Unprocessable Entity** (`InsufficientPositionException`).

### Ativos com posição zerada

Se a quantidade líquida chegar a **zero** (venda total), o ativo **não entra** na lista `positions` do `/portfolio/summary` (sem erro de mark-to-market). O cadastro do ativo permanece em `/assets` para novos aportes.

---

## Endpoints da API (REST)

### Autenticação (`/auth`)

| Método | Caminho | Descrição | HTTP |
|--------|---------|-----------|------|
| `POST` | `/auth/register` | Cadastra usuário | `201` / `409` |
| `POST` | `/auth/login` | Obtém JWT | `200` / `401` |

### Ativos (`/assets`) — autenticado

| Método | Caminho | Descrição | HTTP |
|--------|---------|-----------|------|
| `POST` | `/assets` | Cadastra ativo (`symbol` único **neste usuário**, normalizado para maiúsculas) | `201` |
| `GET` | `/assets` | Lista **os** ativos do usuário autenticado | `200` |
| `GET` | `/assets/{id}` | Detalhe por ID (somente se pertencer ao usuário) | `200` / `404` |

### Transações (`/transactions`) — autenticado

| Método | Caminho | Descrição | HTTP |
|--------|---------|-----------|------|
| `POST` | `/transactions` | Registra transação (`quantity` e `unitPrice` > 0); **`SELL`** valida saldo | `201` / `422` |
| `GET` | `/transactions/asset/{assetId}` | Histórico **paginado** do ativo | `200` / `404` |

Query params (Spring Data): `page` (0-based), `size` (padrão 20, máx. 100), `sort` (padrão `occurredAt,desc`).

Resposta: **`PagedResponse<TransactionResponse>`** (`content`, `page`, `size`, `totalElements`, `totalPages`, `first`, `last`).

**Tipos de transação:** `BUY`, `SELL`, `DIVIDEND`, `FEE`  
**Tipos de ativo:** `STOCK`, `FII`, `ETF`, `BDR`, `CRYPTO`, `OTHER`

### Carteira (`/portfolio`) — autenticado

| Método | Caminho | Descrição | HTTP |
|--------|---------|-----------|------|
| `GET` | `/portfolio/summary` | Posição **líquida** consolidada, mark-to-market (Brapi) e totais | `200` |

Posição e custo médio seguem a **Fase 8** (secção 7): compras formam o preço médio; vendas reduzem só a quantidade líquida.

---

## Exemplos com `curl`

```bash
BASE=http://localhost:8080

# 1) Registrar usuário
curl -s -X POST "$BASE/auth/register" \
  -H 'Content-Type: application/json' \
  -d '{"email":"investidor@example.com","password":"senha-segura"}'

# 2) Login — guardar o accessToken
TOKEN=$(curl -s -X POST "$BASE/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"email":"investidor@example.com","password":"senha-segura"}' \
  | jq -r '.accessToken')

AUTH="Authorization: Bearer $TOKEN"

# 3) Cadastrar ativo
curl -s -X POST "$BASE/assets" \
  -H 'Content-Type: application/json' \
  -H "$AUTH" \
  -d '{"symbol":"petr4","name":"Petrobras PN","assetType":"STOCK","currency":"BRL"}'

# 4) Compra (aporte) — ajuste assetId conforme resposta anterior
curl -s -X POST "$BASE/transactions" \
  -H 'Content-Type: application/json' \
  -H "$AUTH" \
  -d '{"assetId":1,"type":"BUY","occurredAt":"2026-03-15","quantity":100,"unitPrice":36.50,"fees":4.90}'

# 5) Venda parcial (ex.: 40 de 100) — posição líquida passa a 60 no summary
curl -s -X POST "$BASE/transactions" \
  -H 'Content-Type: application/json' \
  -H "$AUTH" \
  -d '{"assetId":1,"type":"SELL","occurredAt":"2026-04-01","quantity":40,"unitPrice":38.00,"fees":4.90}'

# 6) Histórico paginado
curl -s -H "$AUTH" "$BASE/transactions/asset/1?page=0&size=10&sort=occurredAt,desc"

# 7) Resumo da carteira
curl -s -H "$AUTH" "$BASE/portfolio/summary"
```

---

## Erros HTTP (JSON padronizado)

Corpo **`ApiErrorResponse`**:

```json
{
  "timestamp": "2026-03-15T20:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Ativo não encontrado: id=99"
}
```

| HTTP | Situação |
|------|----------|
| **400** | Validação de entrada ou `BusinessValidationException` |
| **401** | JWT ausente/inválido ou credenciais de login incorretas |
| **404** | Recurso inexistente **ou** fora do escopo do usuário autenticado |
| **409** | `symbol` duplicado **no mesmo usuário**, ou e-mail duplicado |
| **422** | Venda (`SELL`) com quantidade maior que a posição líquida disponível |

---

## Configuração

Arquivo principal: `investhub-api/src/main/resources/application.properties`

- JDBC e JWT via **variáveis de ambiente** (sem segredos no repositório)
- **Flyway** — migrations em `src/main/resources/db/migration/`
- `spring.jpa.hibernate.ddl-auto=validate`

**Banco Docker já existente (criado com `ddl-auto=update`):** recrie o volume uma vez para o Flyway aplicar o baseline:

```bash
cd investhub-api && docker compose down -v && docker compose up -d
```

### Testes e cobertura

```bash
cd investhub-api
./mvnw verify
```

- **Testcontainers** + **MockMvc** (exige Docker)
- Relatório **JaCoCo:** `investhub-api/target/site/jacoco/index.html` (gerado na fase `verify`)

### CI (GitHub Actions)

Workflow `.github/workflows/investhub-api-ci.yml`: em cada push/PR na branch `main` que altere `investhub-api/**`, roda `./mvnw verify` (testes + JaCoCo).

---

## Roadmap

- [x] Flyway + paginação + testes de integração — **Fase 4**
- [x] Cotações Brapi + mark-to-market — **Fase 5**
- [x] JWT, JaCoCo, CI GitHub Actions — **Fase 6**
- [x] Multi-carteira (isolamento por `user_id` + JWT) — **Fase 7**
- [x] Posição líquida com `SELL`, preço médio imutável na venda e validação **422** — **Fase 8**

---

## Licença

Projeto de portfólio — defina a licença (ex.: MIT) conforme publicação no GitHub.
