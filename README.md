# InvestHub

**Gerenciador e Agregador de Investimentos** — API RESTful para cadastro de ativos financeiros, registro de aportes/transações e consolidação de carteira em tempo real.

O sistema prioriza **precisão financeira rigorosa**: todos os valores monetários, preços unitários e quantidades são modelados utilizando `BigDecimal` no Java e persistidos como `NUMERIC` de alta precisão no PostgreSQL, eliminando erros de arredondamento de ponto flutuante.

---

##  Tech Stack

| Camada | Tecnologia |
|--------|------------|
| **Linguagem** | Java 17 |
| **Framework** | Spring Boot 3.x (Spring Web, Spring Data JPA, Bean Validation) |
| **Banco de Dados** | PostgreSQL 16 |
| **Infraestrutura Local** | Docker & Docker Compose |
| **Build Tool** | Maven (`./mvnw`) |
| **Documentação** | Swagger / OpenAPI 3.0 |

---

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
docker compose up -d

# Confirme o status do container
docker compose ps
```

### Credenciais Padrão de Desenvolvimento:
- **Host:** `localhost`
- **Porta:** `5433`
- **Banco de Dados:** `gerenciador_investimentos`
- **Usuário:** `gerenciador_app`
- **Senha:** `gerenciador_dev`

---

## 💻 2. Rodando a Aplicação API

No terminal, estando dentro da pasta `investhub-api/`, execute:

```bash
./mvnw clean spring-boot:run
```

A API estará disponível em `http://localhost:8080`.

---

##  Endpoints da API (REST)

###  Ativos (`/assets`)

| Método | Caminho | Descrição | Status HTTP |
|--------|---------|-----------|-------------|
| `POST` | `/assets` | Cadastra um novo ativo financeiro (ex: `PETR4`, `VALE3`) | `201 Created` |
| `GET`  | `/assets` | Lista todos os ativos cadastrados | `200 OK` |
| `GET`  | `/assets/{id}` | Busca os detalhes de um ativo específico pelo ID | `200 OK` / `404 Not Found` |

###  Transações & Aportes (`/transactions`)

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
