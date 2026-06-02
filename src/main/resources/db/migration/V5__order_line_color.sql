ALTER TABLE order_lines
    ADD COLUMN IF NOT EXISTS selected_color VARCHAR(32);
