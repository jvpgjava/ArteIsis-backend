# Migrações Flyway

As migrações vivem em `src/main/resources/db/migration/` e são aplicadas em ordem (`V1`, `V2`, …) com `ddl-auto=validate` no JPA (o schema na base tem de corresponder às entidades).

## V1__schema.sql

Cria o núcleo do negócio:

| Tabela | Entidade JPA | Notas |
|--------|----------------|-------|
| `customers` | `Customer` | E-mail único |
| `products` | `Product` | `label` / `availability` como texto alinhado aos enums |
| `product_sizes` | `Product.sizes` (`@ElementCollection`) | PK composta |
| `shop_order` | `ShopOrder` | FK para `customers` |
| `order_lines` | `OrderLine` | FK opcional para `products` |

Índices em `V1` cobrem filtros usados nos repositórios (categoria, ativo, pedidos).

## V2__app_user.sql

Cria `app_user` para autenticação da aplicação (JWT), alinhada a `AppUser` e `Role` (`ADMIN`, `CUSTOMER`). Não confundir com `customers` (dados de negócio da loja).

## Integridade

- Após alterar entidades, cria uma nova migração `Vn__...sql` (não edites migrações já aplicadas em ambientes partilhados).
- Se uma migração nova falhar, corrige o script e usa reparo Flyway apenas em desenvolvimento (`repair`), nunca em produção sem processo controlado.
