# Arte Isis — API (backend)

API REST em **Spring Boot 3** para pedidos, clientes, produtos, catálogo público e uploads. Usa **PostgreSQL** e **Flyway** para migrações.

Este guia assume **Windows 10/11**. Em Linux ou macOS os passos são parecidos (instalar os mesmos programas e usar os mesmos comandos no terminal).

---

## O que precisa instalado

| Ferramenta | Versão indicada |
|------------|-------------------|
| **JDK** (Java) | **21** — recomendamos o build **Azul Zulu** |
| **Apache Maven** | **3.9+** |
| **PostgreSQL** | **14 ou superior** — instalador **EDB** (oficial) |

---

## 1. Instalar o Java (Azul Zulu JDK 21)

1. Abra a página de downloads da Azul (Zulu OpenJDK):  
   **https://www.azul.com/downloads/?version=java-21-lts&os=windows&package=jdk**
2. Escolha **Windows** → **x86 64-bit** → pacote **JDK** (`.msi` é o mais simples).
3. Execute o instalador:
   - Marque a opção para **definir as variáveis de ambiente** (JAVA_HOME / PATH), se o instalador oferecer.
4. Se **não** definiu automaticamente, configure manualmente:
   1. Tecla **Windows** → digite **variáveis de ambiente** → abra **“Editar as variáveis de ambiente do sistema”**.
   2. **Variáveis de ambiente…**
   3. Em **Variáveis do sistema** → **Novo…**
      - Nome: `JAVA_HOME`  
      - Valor: pasta onde o JDK foi instalado, por exemplo:  
        `C:\Program Files\Zulu\zulu-21\`  
        (confira no Explorador de ficheiros a pasta exata `...\Zulu\zulu-21` ou similar.)
   4. Edite a variável **Path** (do utilizador ou do sistema) → **Novo** → adicione:  
      `%JAVA_HOME%\bin`
   5. **OK** em todas as janelas.
5. **Feche e volte a abrir** o PowerShell ou o **Prompt de Comando** (as variáveis só aplicam em janelas novas).
6. Confirme:
   ```powershell
   java -version
   ```
   Deve aparecer algo como **openjdk version "21"** (ou Zulu 21).  
   Opcional:
   ```powershell
   javac -version
   ```

Se `java` não for reconhecido, o **PATH** ou o **JAVA_HOME** está incorreto ou a janela do terminal é antiga.

---

## 2. Instalar o Apache Maven

1. Página de download: **https://maven.apache.org/download.cgi**
2. Baixe o ficheiro **Binary zip archive** (ex.: `apache-maven-3.9.x-bin.zip`).
3. Extraia para uma pasta fixa, por exemplo: `C:\dev\apache-maven-3.9.9`
4. Variáveis de ambiente (Windows):
   1. **Variáveis de ambiente…** (igual ao passo do Java).
   2. **Novo** (variáveis do sistema ou do utilizador):
      - Nome: `MAVEN_HOME`  
      - Valor: `C:\dev\apache-maven-3.9.9` (a pasta que contém a subpasta `bin`)
   3. Em **Path** → **Novo** → `%MAVEN_HOME%\bin`
   4. **OK**, feche o terminal e abra outro.
5. Confirme:
   ```powershell
   mvn -version
   ```
   Deve mostrar **Apache Maven** e usar o **Java 21** que instalou.

### Anexo — Editar o **Path** no Windows (passo a passo)

Se precisar de acrescentar manualmente uma pasta `bin` ao PATH (Java ou Maven):

1. Prima **Windows + R**, escreva `sysdm.cpl` e **Enter**.
2. Separador **Avançado** → botão **Variáveis de Ambiente…**.
3. Em **Variáveis do utilizador** ou **Variáveis do sistema**, selecione **Path** → **Editar…**.
4. **Novo** → escreva o caminho, por exemplo:
   - `%JAVA_HOME%\bin` **ou** o caminho completo, ex.: `C:\Program Files\Zulu\zulu-21\bin`
   - `%MAVEN_HOME%\bin` **ou** `C:\dev\apache-maven-3.9.9\bin`
5. **OK** em todas as janelas.
6. **Feche** todas as janelas do PowerShell / CMD e **abra uma nova**; só assim o `java` e o `mvn` passam a ser reconhecidos.

---

## 3. Instalar o PostgreSQL (instalador EDB)

1. Download oficial (Windows):  
   **https://www.enterprisedb.com/downloads/postgres-postgresql-downloads**
2. Escolha uma versão **14 ou superior** para Windows **x86-64**.
3. No assistente de instalação:
   - Anote a **porta** (por defeito **5432**).
   - Defina a **palavra-passe do utilizador `postgres`** (superutilizador) e guarde-a.
4. Ao final, pode deixar instalados o **pgAdmin** e as **ferramentas de linha de comando** (útil para `psql`).

### 3.1. Criar a base de dados `arteisis`

1. Abra o **pgAdmin** (ou `psql` na linha de comando).
2. Conecte-se ao servidor **PostgreSQL** em `localhost` com o utilizador **`postgres`** e a palavra-passe que definiu.
3. Execute o SQL (Query Tool no pgAdmin, ou `psql`):

```sql
CREATE DATABASE arteisis
    WITH
    ENCODING = 'UTF8'
    LC_COLLATE = 'Portuguese_Portugal.1252'
    LC_CTYPE = 'Portuguese_Portugal.1252'
    TEMPLATE = template0;
```

Se o `CREATE DATABASE` falhar por causa de *locale*, use uma variante mais simples:

```sql
CREATE DATABASE arteisis;
```

4. (Opcional) Em PostgreSQL **15+**, pode ser necessário conceder permissões no schema `public` dentro da base `arteisis` para o utilizador que a API usará. Se usar só o utilizador `postgres`, normalmente já tem acesso total.

---

## 4. Configurar a ligação à base (`application-local.properties`)

1. No projeto, abra o ficheiro:  
   `src/main/resources/application-local.properties`
2. Ajuste estes valores **à sua máquina** (utilizador, palavra-passe e nome da base):

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/arteisis
spring.datasource.username=postgres
spring.datasource.password=A_SUA_PALAVRA_PASSO_POSTGRES
```

3. O perfil ativo por defeito é **`local`** (ver `application.properties`: `spring.profiles.active=local`).

**Segurança:** não commite palavras-passe reais em repositórios públicos. Para equipa, use variáveis de ambiente ou ficheiros locais ignorados pelo Git, se adoptarem essa política.

Outros perfis existentes: `dev`, `hml`, `prod` — ver `application-<perfil>.properties`.

---

## 5. Clonar o repositório e ir à pasta do backend

```powershell
cd C:\caminho\para\o\projeto\ArteIsis-backend
```

---

## 6. Compilar e correr a API

Na raiz de **ArteIsis-backend**:

```powershell
mvn clean compile -DskipTests
mvn spring-boot:run
```

Com perfil explícito (se precisar):

```powershell
mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=local"
```

- A API sobe em **http://localhost:8080** (porta definida em `application.properties`).
- Na **primeira execução** com a tabela `app_user` vazia, o sistema pode criar o administrador **`admin@arteisis.local`** com palavra-passe **`admin`** — altere em produção.

### Flyway

As migrações em `src/main/resources/db/migration` aplicam-se automaticamente ao arrancar (se `spring.flyway.enabled=true`).

---

## 7. Verificar se está tudo certo

No navegador ou com `curl`:

- `GET http://localhost:8080/api/catalog/products`  
  Deve responder `[]` ou uma lista JSON (conforme já existam produtos).

---

## CORS

Origens permitidas: `arteisis.cors.allowed-origins` no `application-local.properties` (por exemplo `http://localhost:3000` e `http://localhost:4200`). O frontend em desenvolvimento deve usar uma origem listada aí.

---

## Autenticação (JWT)

- Rotas públicas: `POST /api/auth/login`, `POST /api/auth/register`, `GET /api/catalog/**`.
- `GET /api/auth/me` e rotas **`/api/admin/**`** exigem cabeçalho: `Authorization: Bearer <token>`.
- Em **homologação/produção**, defina **`ARTEISIS_JWT_SECRET`** (mínimo 32 caracteres). Opcional: `ARTEISIS_JWT_EXPIRATION_SECONDS`.

---

## Documentação extra no repositório

| Documento | Conteúdo |
|-----------|----------|
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Arquitetura da API |
| [docs/FLYWAY.md](docs/FLYWAY.md) | Migrações e tabelas |
| [docs/POSTMAN.md](docs/POSTMAN.md) | Postman / OpenAPI |
| [scripts/README.md](scripts/README.md) | Scripts SQL e PowerShell (seeds, utilizadores) |

---

## Resumo rápido (quem já tem tudo instalado)

```powershell
cd ArteIsis-backend
# Editar application-local.properties se necessário
mvn spring-boot:run
```

Depois suba o frontend (ver `ArteIsis-frontend/README.md`) e aceda ao painel em `/admin` com utilizador **ADMIN**.
