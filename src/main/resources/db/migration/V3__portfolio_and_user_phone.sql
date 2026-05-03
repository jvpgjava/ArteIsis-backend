ALTER TABLE app_user
    ADD COLUMN phone VARCHAR(32);

CREATE TABLE portfolio_item (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    image_url VARCHAR(1024) NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_portfolio_item_sort ON portfolio_item (sort_order);
CREATE INDEX idx_portfolio_item_active ON portfolio_item (active);
