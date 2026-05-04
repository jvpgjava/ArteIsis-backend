ALTER TABLE products
    ADD COLUMN IF NOT EXISTS color_variants jsonb;

CREATE TABLE IF NOT EXISTS product_available_sizes (
    product_id UUID NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    size_code VARCHAR(8) NOT NULL,
    PRIMARY KEY (product_id, size_code)
);
