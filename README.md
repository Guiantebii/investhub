# InvestHub

**Gerenciador e Agregador de Investimentos** — API RESTful para cadastro de ativos financeiros, registro de aportes/transações e consolidação de carteira.

Repositório: [github.com/Guiantebii/investhub](https://github.com/Guiantebii/investhub)

O sistema prioriza **precisão financeira rigorosa**: valores monetários, preços unitários e quantidades são modelados com `BigDecimal` no Java e persistidos como `NUMERIC` no PostgreSQL, eliminando erros de arredondamento de ponto flutuante.

---

## Objetivo do projeto

Backend de portfólio pessoal com camadas claras (domínio → serviço → API), persistência relacional e cálculos **sem `double`**. Contrato HTTP via DTOs; entidades JPA ficam na camada de persistência.

---

## Tech stack

| Camada | Tecnologia |
|--------|------------|
| **Linguagem** | Java 17 |
| **Framework** | Spring Boot 4.x (Web MVC, Data JPA, Bean Validation) |
| **Documentação** | springdoc-openapi — Swagger UI / OpenAPI 3 |
| **Banco de dados** | PostgreSQL 16 (Alpine) |
| **Infraestrutura local** | Docker & Docker Compose |
| **Build** | Maven Wrapper (`./mvnw`) |

Pacote base: **`com.investhub.api`** · Classe principal: **`InvestHubApplication`**

---

## Estrutura do repositório

```text
gerenciador-investimentos/
├── README.md
├── .gitignore
└── investhub-api/
    ├── docker-compose.yml
    ├── .env.example
    ├── pom.xml
    └── src/main/java/com/investhub/api/
        ├── config/          # OpenAPI
        ├── controller/
        ├── domain/
        ├── dto/
        ├── exception/
        ├── repository/
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

## 1. Subir o banco de dados (PostgreSQL — porta 5433)

O container usa a porta **5433** no host para não conflitar com Postgres na **5432**.

```bash
cd investhub-api
cp .env.example .env   # opcional — defaults já funcionam
docker compose up -d
docker compose ps
```

| Parâmetro | Valor (dev) |
|-----------|-------------|
| Host | `localhost:5433` |
| Banco | `gerenciador_investimentos` |
| Usuário | `gerenciador_app` |
| Senha | `gerenciador_dev` |
| Container | `gerenciador_investimentos_pg` |

Parar: `docker compose down` · Apagar volume: `docker compose down -v` (cuidado).

---

## 2. Rodar a aplicação

```bash
cd investhub-api
./mvnw spring-boot:run
```

Alternativa com build limpo:

```bash
./mvnw clean spring-boot:run
```

- **Base URL:** http://localhost:8080  
- Compilar sem executar: `./mvnw -DskipTests compile`

---

## 3. Documentação interativa (Swagger)

| Recurso | URL |
|---------|-----|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |

Metadados: **InvestHub API** · versão **1.0.0**

---

## Endpoints da API (REST)

### Ativos (`/assets`)

| Método | Caminho | Descrição | HTTP |
|--------|---------|-----------|------|
| `POST` | `/assets` | Cadastra ativo (`symbol` único, normalizado para maiúsculas) | `201` |
| `GET` | `/assets` | Lista todos os ativos | `200` |
| `GET` | `/assets/{id}` | Detalhe por ID | `200` / `404` |

### Transações (`/transactions`)

| Método | Caminho | Descrição | HTTP |
|--------|---------|-----------|------|
| `POST` | `/transactions` | Registra transação (`quantity` e `unitPrice` > 0) | `201` |
| `GET` | `/transactions/asset/{assetId}` | Histórico do ativo (data desc.) | `200` / `404` |

**Tipos de transação:** `BUY`, `SELL`, `DIVIDEND`, `FEE`  
**Tipos de ativo:** `STOCK`, `FII`, `ETF`, `BDR`, `CRYPTO`, `OTHER`

### Carteira (`/portfolio`)

| Método | Caminho | Descrição | HTTP |
|--------|---------|-----------|------|
| `GET` | `/portfolio/summary` | Posição consolidada e totais | `200` |

**Regra do resumo (Fase 3):** apenas transações **`BUY`**.

- `totalQuantity` — soma das quantidades compradas  
- `totalInvested` — Σ (`quantity × unitPrice` + `fees`)  
- `averagePrice` — `totalInvested ÷ totalQuantity` (escala 4, `HALF_UP`)  
- `totalAllocated` — soma investida nas posições com quantidade > 0  
- `totalAssetsRegistered` — total de ativos cadastrados  

`SELL` e ajuste de posição líquida: evolução futura (ver roadmap).

---

## Exemplos com `curl`

```bash
# Cadastrar ativo
curl -s -X POST http://localhost:8080/assets \
  -H 'Content-Type: application/json' \
  -d '{"symbol":"petr4","name":"Petrobras PN","assetType":"STOCK","currency":"BRL"}'

# Compra (aporte)
curl -s -X POST http://localhost:8080/transactions \
  -H 'Content-Type: application/json' \
  -d '{"assetId":1,"type":"BUY","occurredAt":"2026-03-15","quantity":100,"unitPrice":36.50,"fees":4.90}'

# Resumo da carteira
curl -s http://localhost:8080/portfolio/summary
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
| **404** | `ResourceNotFoundException` |
| **409** | `symbol` duplicado |

---

## Configuração

Arquivo: `investhub-api/src/main/resources/application.properties`

- JDBC → `localhost:5433` / `gerenciador_investimentos`  
- `spring.jpa.hibernate.ddl-auto=update` (desenvolvimento)  
- Credenciais de dev no properties — use variáveis de ambiente ou profiles em produção  

Docker: `.env` local (ignorado pelo Git); modelo em `.env.example`.

---

## Roadmap

- [ ] Posição líquida com `SELL` e custo médio ajustado  
- [ ] Testes de integração (MockMvc / Testcontainers)  
- [ ] Migrações Flyway/Liquibase (substituir `ddl-auto=update` em prod)  
- [ ] Autenticação e multi-carteira  

---

## Licença

Projeto de portfólio — defina a licença (ex.: MIT) conforme publicação no GitHub.
