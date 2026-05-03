# Arte Isis API (Spring Boot)

API REST para pedidos, clientes, produtos e catálogo público. Base de dados PostgreSQL com Flyway.

## Pré-requisitos

- JDK 21
- Maven 3.9+
- PostgreSQL 14+ em execução

## 1. Criar base e utilizador

No `psql` ou cliente SQL (ajusta nomes se quiseres):

```sql
CREATE DATABASE arteisis;
CREATE USER arteisis WITH PASSWORD 'arteisis';
GRANT ALL PRIVILEGES ON DATABASE arteisis TO arteisis;
```

No PostgreSQL 15+, pode ser necessário conceder privilégios no schema `public` dentro da base `arteisis`.

## 2. Configurar perfil

O perfil ativo por defeito é `local` (ver `src/main/resources/application.properties`).

Edita `src/main/resources/application-local.properties` se a URL, utilizador ou senha forem diferentes dos valores por defeito:

- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`

Outros perfis: `dev`, `hml`, `prod` (ver ficheiros `application-<perfil>.properties` e variáveis de ambiente descritas lá).

## 3. Compilar e correr

Na raiz do repositório `ArteIsis-backend`:

```bash
mvn clean compile
mvn spring-boot:run
```

Ou com perfil explícito:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=local
```

A API fica em `http://localhost:8080` (porta configurável por perfil).

## 4. Verificar

- `GET http://localhost:8080/api/catalog/products` deve responder `[]` ou lista de produtos após inserires dados pelo painel (admin).

## Autenticação (JWT)

- Rotas públicas: `POST /api/auth/login`, `POST /api/auth/register`, `GET /api/catalog/**`.
- `GET /api/auth/me` e todas as rotas `/api/admin/**` exigem cabeçalho `Authorization: Bearer <token>`.
- **Perfis `hml` e `prod`**: define a variável de ambiente `ARTEISIS_JWT_SECRET` (mínimo 32 caracteres UTF-8 para HS256). Opcional: `ARTEISIS_JWT_EXPIRATION_SECONDS`.
- Na primeira execução com base vazia (tabela `app_user` sem linhas), é criado o utilizador `admin@arteisis.local` com palavra-passe `admin` (altera em produção e remove ou desactiva esta conta quando tiveres IAM próprio).

## CORS

Origens permitidas vêm de `arteisis.cors.allowed-origins` (por perfil). O frontend em desenvolvimento usa `http://localhost:3000` por defeito no `environment.ts`; garante que essa origem está listada no perfil `local` ou ajusta a URL do front.

## Documentação de arquitetura

Ver [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Migrações Flyway

Resumo das versões e alinhamento com as entidades JPA: [docs/FLYWAY.md](docs/FLYWAY.md).

## Postman / OpenAPI

Como importar a API no Postman (URL OpenAPI ou coleção JSON): [docs/POSTMAN.md](docs/POSTMAN.md).

## Scripts (utilizadores)

PowerShell e SQL na pasta [scripts/](scripts/) (ver [scripts/README.md](scripts/README.md)):

- `sql/seed-admin-and-customer.sql` — **SQL** com um **ADMIN** e um **CUSTOMER** de exemplo (`pgcrypto`).
- `add-admin-user.ps1` — insere **ADMIN** personalizado via `psql` + variáveis.
- `add-customer-user.ps1` — regista **CUSTOMER** via `POST /api/auth/register`.
