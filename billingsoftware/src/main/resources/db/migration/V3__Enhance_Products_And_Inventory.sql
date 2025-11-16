-- Enhance products table with new fields
ALTER TABLE products ADD COLUMN description CLOB;
ALTER TABLE products ADD COLUMN cost_price DECIMAL(12,2);
ALTER TABLE products ADD COLUMN min_stock_level INTEGER;
ALTER TABLE products ADD COLUMN max_stock_level INTEGER;
ALTER TABLE products ADD COLUMN barcode VARCHAR(255);
ALTER TABLE products ADD COLUMN unit VARCHAR(20) DEFAULT 'pcs';
ALTER TABLE products ADD COLUMN track_stock BOOLEAN DEFAULT TRUE;
ALTER TABLE products ADD COLUMN status VARCHAR(20) DEFAULT 'ACTIVE';
ALTER TABLE products ADD COLUMN category_id BIGINT;
ALTER TABLE products ADD COLUMN supplier_id BIGINT;
ALTER TABLE products ADD COLUMN image_url VARCHAR(500);
ALTER TABLE products ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE products ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Add foreign key constraints
ALTER TABLE products ADD CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories(id);
ALTER TABLE products ADD CONSTRAINT fk_products_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id);

-- Add indexes for better performance
CREATE INDEX idx_products_barcode ON products(barcode);
CREATE INDEX idx_products_status ON products(status);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_supplier ON products(supplier_id);
CREATE INDEX idx_products_stock ON products(stock_quantity);

-- Payments table for multiple payment methods
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    method VARCHAR(50) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    reference_number VARCHAR(255),
    card_last4 VARCHAR(4),
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    processed_at TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    INDEX idx_payments_order (order_id),
    INDEX idx_payments_method (method),
    INDEX idx_payments_status (status),
    INDEX idx_payments_date (created_at)
);

-- Discounts table for promotions
CREATE TABLE discounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    type VARCHAR(20) NOT NULL,
    value DECIMAL(12,2) NOT NULL,
    min_order_amount DECIMAL(12,2),
    max_discount_amount DECIMAL(12,2),
    usage_limit INTEGER,
    usage_count INTEGER DEFAULT 0,
    usage_limit_per_customer INTEGER,
    valid_from TIMESTAMP,
    valid_to TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    INDEX idx_discounts_code (code),
    INDEX idx_discounts_status (status),
    INDEX idx_discounts_dates (valid_from, valid_to)
);

-- Junction table for discount-product relationships
CREATE TABLE discount_products (
    discount_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    PRIMARY KEY (discount_id, product_id),
    FOREIGN KEY (discount_id) REFERENCES discounts(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Junction table for discount-category relationships
CREATE TABLE discount_categories (
    discount_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    PRIMARY KEY (discount_id, category_id),
    FOREIGN KEY (discount_id) REFERENCES discounts(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
);
