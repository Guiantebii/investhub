# InvestHub

<<<<<<< HEAD
**Gerenciador e Agregador de Investimentos** — API RESTful para cadastro de ativos financeiros, registro de aportes/transações e consolidação de carteira em tempo real.

O sistema prioriza **precisão financeira rigorosa**: todos os valores monetários, preços unitários e quantidades são modelados utilizando `BigDecimal` no Java e persistidos como `NUMERIC` de alta precisão no PostgreSQL, eliminando erros de arredondamento de ponto flutuante.
=======
**Gerenciador e Agregador de Investimentos** — API REST para cadastro de ativos, registro de aportes/transações e **consolidação de carteira** com regras de domínio e precisão numérica.

Repositório: [github.com/Guiantebii/investhub](https://github.com/Guiantebii/investhub)

---

## Objetivo do projeto

Demonstrar um backend de portfólio pessoal com camadas claras (domínio → serviço → API), persistência relacional e cálculos financeiros **sem `double`**: valores e quantidades usam `BigDecimal` e são armazenados como `NUMERIC` no PostgreSQL.

---
>>>>>>> 0f97167 (docs: update README with Phase 3 endpoints and Swagger UI details)

---

##  Tech Stack

| Camada | Tecnologia |
|--------|------------|
<<<<<<< HEAD
| **Linguagem** | Java 17 |
| **Framework** | Spring Boot 3.x (Spring Web, Spring Data JPA, Bean Validation) |
| **Banco de Dados** | PostgreSQL 16 |
| **Infraestrutura Local** | Docker & Docker Compose |
| **Build Tool** | Maven (`./mvnw`) |
| **Documentação** | Swagger / OpenAPI 3.0 |
=======
| Linguagem | Java 17 |
| Framework | Spring Boot 4.x (Web MVC, Data JPA, Validation) |
| Documentação | springdoc-openapi (Swagger UI) |
| Banco | PostgreSQL 16 (Alpine) |
| Infra local | Docker Compose |
| Build | Maven Wrapper (`./mvnw`) |

---
>>>>>>> 0f97167 (docs: update README with Phase 3 endpoints and Swagger UI details)

---

<<<<<<< HEAD
##  Estrutura do Repositório

```text
gerenciador-investimentos/
├── README.md               # Documentação principal do repositório
├── .gitignore              # Regras globais do Git
└── investhub-api/          # Módulo principal da aplicação Spring Boot
    ├── docker-compose.yml  # PostgreSQL isolado na porta 5433
    ├── .env.example        # Modelo de variáveis de ambiente
    └── src/
        └── main/
            └── java/
                └── com/investhub/api/  # Camadas de Domínio, DTOs, Services e Controllers
```

---

##  Pré-requisitos

- **Java JDK 17** ou superior instalado
- **Docker** e **Docker Compose** ativos na máquina
- **Postman** (ou similar) para execução dos testes de API

---

##  1. Subindo o Banco de Dados (PostgreSQL na Porta 5433)

Para evitar conflitos com instâncias locais do Postgres na porta padrão (`5432`), este container está mapeado para a porta **5433** do host.

```bash
cd investhub-api

# Opcional: crie o arquivo .env a partir do modelo
cp .env.example .env  

# Suba o container PostgreSQL
=======
```
gerenciador-investimentos/
├── README.md
├── .gitignore
└── investhub-api/
    ├── docker-compose.yml
    ├── pom.xml
    └── src/main/java/com/investhub/api/
        ├── config/          # OpenAPI
        ├── controller/      # REST
        ├── domain/          # Entidades JPA
        ├── dto/             # Request / Response (API)
        ├── exception/
        ├── repository/
        ├── service/
        └── web/             # Tratamento global de erros
```

Pacote base: **`com.investhub.api`** · Classe principal: **`InvestHubApplication`**

---

## Pré-requisitos

- JDK 17+
- Docker e Docker Compose
- Git (opcional, para clonar)

```bash
git clone https://github.com/Guiantebii/investhub.git
cd investhub/investhub-api
```

---

## Banco de dados (Docker — porta 5433)

A porta **5433** no host evita conflito com outros Postgres locais (ex.: containers na 5432).

```bash
cd investhub-api
cp .env.example .env   # opcional — defaults já funcionam
>>>>>>> 0f97167 (docs: update README with Phase 3 endpoints and Swagger UI details)
docker compose up -d

# Confirme o status do container
docker compose ps
```

<<<<<<< HEAD
### Credenciais Padrão de Desenvolvimento:
- **Host:** `localhost`
- **Porta:** `5433`
- **Banco de Dados:** `gerenciador_investimentos`
- **Usuário:** `gerenciador_app`
- **Senha:** `gerenciador_dev`
=======
| Parâmetro | Valor (dev) |
|-----------|-------------|
| Host | `localhost:5433` |
| Banco | `gerenciador_investimentos` |
| Usuário | `gerenciador_app` |
| Senha | `gerenciador_dev` |
| Container | `gerenciador_investimentos_pg` |

Parar o banco: `docker compose down` · Apagar dados: `docker compose down -v` (cuidado).

---
>>>>>>> 0f97167 (docs: update README with Phase 3 endpoints and Swagger UI details)

---

## 💻 2. Rodando a Aplicação API

No terminal, estando dentro da pasta `investhub-api/`, execute:

```bash
./mvnw clean spring-boot:run
```

<<<<<<< HEAD
A API estará disponível em `http://localhost:8080`.

---

##  Endpoints da API (REST)

###  Ativos (`/assets`)

| Método | Caminho | Descrição | Status HTTP |
|--------|---------|-----------|-------------|
| `POST` | `/assets` | Cadastra um novo ativo financeiro (ex: `PETR4`, `VALE3`) | `201 Created` |
| `GET`  | `/assets` | Lista todos os ativos cadastrados | `200 OK` |
| `GET`  | `/assets/{id}` | Busca os detalhes de um ativo específico pelo ID | `200 OK` / `404 Not Found` |
=======
- Base URL: **http://localhost:8080**
- Health implícito: subir sem erro de conexão JDBC indica app + Postgres ok

Compilar sem rodar:

```bash
./mvnw -DskipTests compile
```

---

## Documentação interativa (Swagger / OpenAPI)

| Recurso | URL |
|---------|-----|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |

Metadados configurados: **InvestHub API** · versão **1.0.0** · descrição alinhada ao uso de portfólio e `BigDecimal`.

---

## API REST — endpoints

### Ativos

| Método | Caminho | Descrição |
|--------|---------|-----------|
| `POST` | `/assets` | Cadastrar ativo (`symbol` único; normalizado para maiúsculas) |
| `GET` | `/assets` | Listar todos os ativos |
| `GET` | `/assets/{id}` | Buscar ativo por ID (**404** se não existir) |

### Transações

| Método | Caminho | Descrição |
|--------|---------|-----------|
| `POST` | `/transactions` | Registrar compra/aporte/etc. (`quantity` e `unitPrice` > 0) |
| `GET` | `/transactions/asset/{assetId}` | Histórico do ativo (ordenado por data desc.) |

Tipos: `BUY`, `SELL`, `DIVIDEND`, `FEE` · Tipos de ativo: `STOCK`, `FII`, `ETF`, `BDR`, `CRYPTO`, `OTHER`.

### Carteira (consolidado)

| Método | Caminho | Descrição |
|--------|---------|-----------|
| `GET` | `/portfolio/summary` | Resumo: valor alocado, total de ativos cadastrados e posições |

**Regra atual do resumo:** considera apenas transações **`BUY`**.

- `totalQuantity` — soma das quantidades compradas  
- `totalInvested` — Σ (`quantity × unitPrice` + `fees`) por compra  
- `averagePrice` — `totalInvested ÷ totalQuantity` (escala 4, arredondamento `HALF_UP`)  
- `totalAllocated` — soma do investido nas posições com quantidade > 0  

Vendas (`SELL`) e outros tipos entram em evoluções futuras do modelo de posição.

---

## Exemplos com `curl`

Cadastrar ativo:

```bash
curl -s -X POST http://localhost:8080/assets \
  -H 'Content-Type: application/json' \
  -d '{"symbol":"petr4","name":"Petrobras PN","assetType":"STOCK","currency":"BRL"}' | jq
```

Registrar compra:

```bash
curl -s -X POST http://localhost:8080/transactions \
  -H 'Content-Type: application/json' \
  -d '{"assetId":1,"type":"BUY","occurredAt":"2026-03-15","quantity":100,"unitPrice":36.50,"fees":4.90}' | jq
```

Resumo da carteira:

```bash
curl -s http://localhost:8080/portfolio/summary | jq
```

---

## Erros HTTP (JSON padronizado)

Respostas de erro usam **`ApiErrorResponse`**:

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
| **400** | Validação de entrada ou regra de negócio (`BusinessValidationException`) |
| **404** | Recurso inexistente (`ResourceNotFoundException`) |
| **409** | `symbol` duplicado |

A API expõe **DTOs** nos controllers (não entidades JPA), separando contrato HTTP e persistência.

---

## Configuração

Arquivo: `investhub-api/src/main/resources/application.properties`

- JDBC → `localhost:5433` / `gerenciador_investimentos`
- `spring.jpa.hibernate.ddl-auto=update` (dev)
- Credenciais de dev no properties — **não** use em produção; prefira variáveis de ambiente ou profiles

Secrets locais do Docker: `.env` (ignorado pelo Git); template em `.env.example`.

---

## Roadmap (portfólio)

- [ ] Posição líquida considerando `SELL` e custo médio ajustado  
- [ ] Testes de integração (MockMvc / Testcontainers)  
- [ ] Flyway/Liquibase em vez de `ddl-auto=update` em produção  
- [ ] Autenticação e multi-carteira  

---
>>>>>>> 0f97167 (docs: update README with Phase 3 endpoints and Swagger UI details)

###  Transações & Aportes (`/transactions`)

<<<<<<< HEAD
| Método | Caminho | Descrição | Status HTTP |
|--------|---------|-----------|-------------|
| `POST` | `/transactions` | Registra uma nova compra (`BUY`) ou venda (`SELL`) | `201 Created` |
| `GET`  | `/transactions/asset/{assetId}` | Lista todo o histórico de transações de um ativo | `200 OK` |

###  Carteira (`/portfolio`)

| Método | Caminho | Descrição | Status HTTP |
|--------|---------|-----------|-------------|
| `GET`  | `/portfolio/summary` | Exibe a posição consolidada, preço médio e total investido | `200 OK` |

---

##  Documentação Interativa (Swagger)

Com a aplicação rodando, acesse a documentação OpenAPI diretamente no seu navegador:

 **`http://localhost:8080/swagger-ui.html`**
=======
Projeto de portfólio — defina a licença (ex.: MIT) antes de uso público além do GitHub pessoal.
>>>>>>> 0f97167 (docs: update README with Phase 3 endpoints and Swagger UI details)
