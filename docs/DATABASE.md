# Modelagem do Banco de Dados — ArteIsis

---

## Diagrama de Relacionamentos

```
app_user            (autenticação/admin — isolado das demais entidades)

customers ──────< shop_order ──────< order_lines >────── products
                                     (product_id opcional)

products ─── product_sizes           (ElementCollection)
         ─── product_available_sizes (ElementCollection)
         ─── color_variants          (coluna JSON/JSONB inline)

portfolio_item      (independente)
```

---

## Tabelas

### `app_user`

Usuários do sistema (administradores e clientes com login).

| Coluna | Tipo SQL | Restrições |
|---|---|---|
| `id` | `uuid` | PK |
| `email` | `varchar` | unique, not null |
| `password_hash` | `varchar` | not null |
| `role` | `varchar(32)` | not null — enum `Role` |
| `full_name` | `varchar` | — |
| `phone` | `varchar(32)` | — |
| `created_at` | `timestamptz` | not null, auto |
| `updated_at` | `timestamptz` | not null, auto |

**Enum `Role`:** `ADMIN` | `CUSTOMER`

---

### `customers`

Clientes que realizam pedidos (não necessariamente com login).

| Coluna | Tipo SQL | Restrições |
|---|---|---|
| `id` | `uuid` | PK |
| `name` | `varchar` | not null |
| `email` | `varchar` | unique, not null |
| `phone` | `varchar(64)` | not null |
| `created_at` | `timestamptz` | not null, auto |
| `updated_at` | `timestamptz` | not null, auto |

---

### `products`

Catálogo de produtos da loja.

| Coluna | Tipo SQL | Restrições |
|---|---|---|
| `id` | `uuid` | PK |
| `name` | `varchar` | not null |
| `unit_price` | `numeric(12,2)` | not null |
| `category` | `varchar(100)` | not null |
| `stock` | `int` | not null |
| `image_url` | `varchar(1024)` | — |
| `label` | `varchar(32)` | not null — enum `ProductLabel`, default `NONE` |
| `availability` | `varchar(32)` | not null — enum `AvailabilityType`, default `DISPONIVEL` |
| `active` | `boolean` | not null, default `true` |
| `color_variants` | `jsonb` | lista de objetos `ProductColorVariant` |
| `created_at` | `timestamptz` | not null, auto |
| `updated_at` | `timestamptz` | not null, auto |

**Enum `ProductLabel`:** `NONE` | `NOVO` | `DESTAQUE`

**Enum `AvailabilityType`:** `DISPONIVEL` | `SOB_ENCOMENDA`

#### Tabelas auxiliares (ElementCollection)

| Tabela | Coluna | Descrição |
|---|---|---|
| `product_sizes` | `product_id` (FK), `size` (varchar) | Todos os tamanhos do produto |
| `product_available_sizes` | `product_id` (FK), `size` (varchar) | Tamanhos atualmente em estoque |

#### Estrutura do JSON `color_variants`

Armazenado como array de objetos na coluna `jsonb`:

```json
[
  {
    "hex": "#FF5733",
    "imageUrl": "https://cdn.../imagem.jpg",
    "available": true
  }
]
```

| Campo | Tipo | Detalhe |
|---|---|---|
| `hex` | string | Código hexadecimal da cor |
| `imageUrl` | string | URL da imagem para esta variante |
| `available` | boolean | Se esta cor está disponível (default: `true`) |

---

### `shop_order`

Pedidos realizados por clientes.

| Coluna | Tipo SQL | Restrições |
|---|---|---|
| `id` | `uuid` | PK |
| `customer_id` | `uuid` | FK → `customers`, not null |
| `status` | `varchar(32)` | not null — enum `OrderStatus`, default `PENDENTE` |
| `order_date` | `date` | not null |
| `total_amount` | `numeric(12,2)` | not null |
| `created_at` | `timestamptz` | not null, auto |
| `updated_at` | `timestamptz` | not null, auto |

**Enum `OrderStatus`:** `PENDENTE` | `PRODUCAO` | `CONCLUIDO`

---

### `order_lines`

Linhas de item de cada pedido. Orphan removal ativo — linhas são deletadas junto com o pedido.

| Coluna | Tipo SQL | Restrições |
|---|---|---|
| `id` | `uuid` | PK |
| `order_id` | `uuid` | FK → `shop_order`, not null |
| `product_id` | `uuid` | FK → `products`, opcional (produto pode ter sido removido) |
| `description` | `varchar(512)` | not null — snapshot do nome do produto no momento da compra |
| `quantity` | `int` | not null |
| `unit_price` | `numeric(12,2)` | not null — preço no momento da compra |
| `line_total` | `numeric(12,2)` | not null — `quantity × unit_price` |

> `product_id` é opcional para preservar o histórico mesmo que o produto seja desativado ou excluído. O campo `description` guarda o nome do produto no momento da compra.

---

### `portfolio_item`

Imagens do portfólio exibidas no site público.

| Coluna | Tipo SQL | Restrições |
|---|---|---|
| `id` | `uuid` | PK |
| `title` | `varchar` | not null |
| `image_url` | `varchar(1024)` | not null |
| `sort_order` | `int` | not null — controla a ordem de exibição |
| `active` | `boolean` | not null, default `true` |
| `created_at` | `timestamptz` | not null, auto |
| `updated_at` | `timestamptz` | not null, auto |

---

## Resumo das Entidades

| Entidade Java | Tabela SQL | Relacionamentos |
|---|---|---|
| `AppUser` | `app_user` | — |
| `Customer` | `customers` | 1:N com `ShopOrder` |
| `Product` | `products` | 1:N com `OrderLine` |
| `ShopOrder` | `shop_order` | N:1 com `Customer`, 1:N com `OrderLine` |
| `OrderLine` | `order_lines` | N:1 com `ShopOrder`, N:1 com `Product` |
| `PortfolioItem` | `portfolio_item` | — |
