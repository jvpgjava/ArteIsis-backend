# Postman e documentação OpenAPI

## Importar a API no Postman (recomendado: OpenAPI)

1. Sobe a API (`mvn spring-boot:run` com perfil `local` ou outro onde o OpenAPI esteja ativo).
2. No Postman: **Import** → separador **Link** (ou **Raw text**).
3. Cola o URL do documento OpenAPI em JSON:
   - `http://localhost:8080/v3/api-docs`
4. Confirma a importação; o Postman gera uma coleção com os endpoints expostos pelo SpringDoc.

Interface Swagger UI (opcional): `http://localhost:8080/swagger-ui.html`

Em **produção** (`spring.profiles.active=prod`) o OpenAPI e o Swagger UI vêm desligados por defeito (`springdoc.*.enabled=false` em `application-prod.properties`).

## Coleção estática (opcional)

Existe também um snapshot em JSON para importar sem depender do servidor:

- Ficheiro: [`postman/ArteIsis.postman_collection.json`](postman/ArteIsis.postman_collection.json)
- Ambiente de exemplo: [`postman/ArteIsis-Local.postman_environment.json`](postman/ArteIsis-Local.postman_environment.json)

Depois de importar, corre o pedido **Auth → Login (admin)** e o script de testes grava o `accessToken` na coleção. Os pedidos em **Admin** usam o token Bearer dessa variável.

## Autenticação nas rotas admin

1. `POST /api/auth/login` com um utilizador `ADMIN` (ou cria um com `scripts/add-admin-user.ps1`).
2. Copia o `accessToken` da resposta para a variável `accessToken` da coleção (ou deixa o script do login fazer isso).
3. Os pedidos `GET/POST/PUT/DELETE` em `/api/admin/**` enviam `Authorization: Bearer <token>`.

Rotas públicas: `GET /api/catalog/**`, `POST /api/auth/login`, `POST /api/auth/register`.
