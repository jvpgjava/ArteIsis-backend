# Scripts utilitários

## SQL puro: admin + utilizador normal

- **`sql/seed-admin-and-customer.sql`** — usa `pgcrypto` + `crypt(...)`. Se o PostgreSQL reclamar de `gen_salt(unknown)`, garante que tens `CREATE EXTENSION pgcrypto` e que o script usa `gen_salt('bf'::text)` (já corrigido neste repo).
- **`sql/seed-admin-and-customer-plain.sql`** — **INSERT comum** com hashes BCrypt já calculados (compatíveis com Spring); **não** precisa de `pgcrypto`. Credenciais: `admin_demo@arteisis.local` / `AdminDemo123!` e `cliente_demo@arteisis.local` / `ClienteDemo123!`.

## `add-admin-user.ps1`

Cria um utilizador com papel **ADMIN** na tabela `app_user` via PostgreSQL (`crypt` bcrypt compatível com `BCryptPasswordEncoder` do Spring).

Requisitos: `psql` no PATH e extensão **pgcrypto** permitida na base (comum em instalações locais).

```powershell
.\scripts\add-admin-user.ps1 `
  -Email "gestor@empresa.pt" `
  -Password "TrocaEstaPass123!" `
  -FullName "Gestor" `
  -DatabaseUrl "postgresql://arteisis:arteisis@127.0.0.1:5432/arteisis"
```

Se o e-mail já existir, o script não duplica (cláusula `WHERE NOT EXISTS`).

## `add-customer-user.ps1`

Cria um utilizador **CUSTOMER** (não admin) através da API pública `POST /api/auth/register` (mesmo fluxo que o frontend).

```powershell
.\scripts\add-customer-user.ps1 `
  -BaseUrl "http://localhost:8080" `
  -Email "cliente@example.com" `
  -Password "ClienteSeguro123!" `
  -FullName "Cliente Teste"
```

A palavra-passe tem de cumprir as regras da API (mínimo 8 caracteres).
