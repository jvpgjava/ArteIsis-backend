CREATE TABLE customers (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(64) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_customers_email UNIQUE (email)
);

CREATE TABLE products (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL,
    category VARCHAR(100) NOT NULL,
    stock INTEGER NOT NULL,
    image_url VARCHAR(1024),
    label VARCHAR(32) NOT NULL,
    availability VARCHAR(32) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE product_sizes (
    product_id UUID NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    size_code VARCHAR(8) NOT NULL,
    PRIMARY KEY (product_id, size_code)
);

CREATE TABLE shop_order (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL REFERENCES customers (id),
    status VARCHAR(32) NOT NULL,
    order_date DATE NOT NULL,
    total_amount NUMERIC(12, 2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE order_lines (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES shop_order (id) ON DELETE CASCADE,
    product_id UUID REFERENCES products (id) ON DELETE SET NULL,
    description VARCHAR(512) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL,
    line_total NUMERIC(12, 2) NOT NULL
);

CREATE INDEX idx_products_category ON products (category);
CREATE INDEX idx_products_active ON products (active);
CREATE INDEX idx_shop_order_customer ON shop_order (customer_id);
CREATE INDEX idx_shop_order_status ON shop_order (status);
CREATE INDEX idx_shop_order_date ON shop_order (order_date);
CREATE INDEX idx_order_lines_order ON order_lines (order_id);
