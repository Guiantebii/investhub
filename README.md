# InvestHub

**Gerenciador e Agregador de Investimentos** — API para cadastro de ativos, registro de aportes/transações e evolução futura de consolidação de carteira.

O sistema prioriza **precisão financeira**: valores monetários e quantidades são modelados com `BigDecimal` e persistidos como `NUMERIC` no PostgreSQL, evitando erros de arredondamento típicos de ponto flutuante.

## Tech stack

| Camada | Tecnologia |
|--------|------------|
| Linguagem | Java 17 |
| Framework | Spring Boot 4.x (Web, Data JPA, Validation) |
| Banco | PostgreSQL 16 |
| Infra local | Docker Compose |
| Build | Maven |

## Estrutura do repositório

```
investhub-api/          # Aplicação Spring Boot (módulo principal)
  docker-compose.yml    # PostgreSQL na porta 5433 (host)
  src/main/java/com/investhub/api/
```

## Pré-requisitos

- JDK 17+
- Docker e Docker Compose
- Maven (ou use `./mvnw` dentro de `investhub-api/`)

## Subir o banco de dados (porta 5433)

O Postgres usa a porta **5433** no host para não conflitar com outros containers na 5432.

```bash
cd investhub-api
cp .env.example .env   # opcional — valores padrão já funcionam
docker compose up -d
docker compose ps
```

Credenciais padrão de desenvolvimento:

- **Banco:** `gerenciador_investimentos`
- **Usuário:** `gerenciador_app`
- **Senha:** `gerenciador_dev`
- **Host:** `localhost:5433`

## Rodar a API

```bash
cd investhub-api
./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080` (porta padrão Spring Boot).

### Endpoints principais (baseline)

| Método | Caminho | Descrição |
|--------|---------|-----------|
| `POST` | `/assets` | Cadastrar ativo |
| `GET` | `/assets` | Listar ativos |
| `POST` | `/transactions` | Registrar transação/aporte |
| `GET` | `/transactions/asset/{assetId}` | Listar transações de um ativo |

## Variáveis e perfil

Configuração em `investhub-api/src/main/resources/application.properties`. Não commite senhas reais; use `.env` apenas para o Docker (arquivo ignorado pelo Git).

## Licença

Projeto de portfólio — defina a licença conforme sua preferência antes de publicar no GitHub.
