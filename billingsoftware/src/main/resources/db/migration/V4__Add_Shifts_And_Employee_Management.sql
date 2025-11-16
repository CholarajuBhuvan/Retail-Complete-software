-- Shifts table for employee time tracking
CREATE TABLE shifts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    clock_in TIMESTAMP NOT NULL,
    clock_out TIMESTAMP,
    sales_amount DECIMAL(12,2) DEFAULT 0,
    orders_processed INTEGER DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    notes TEXT,
    location VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_shifts_user (user_id),
    INDEX idx_shifts_status (status),
    INDEX idx_shifts_date (clock_in),
    INDEX idx_shifts_active (user_id, status)
);

-- Add audit fields to orders table for better tracking
ALTER TABLE orders 
ADD COLUMN processed_by BIGINT,
ADD COLUMN discount_code VARCHAR(100),
ADD COLUMN discount_amount DECIMAL(12,2) DEFAULT 0;

-- Add foreign key for processed_by
ALTER TABLE orders 
ADD CONSTRAINT fk_orders_processed_by FOREIGN KEY (processed_by) REFERENCES users(id);

-- Add index for better performance
CREATE INDEX idx_orders_processed_by ON orders(processed_by);
CREATE INDEX idx_orders_date ON orders(created_at);

-- Update existing data to set default values
UPDATE products SET 
    status = 'ACTIVE',
    track_stock = TRUE,
    unit = 'pcs',
    created_at = CURRENT_TIMESTAMP,
    updated_at = CURRENT_TIMESTAMP
WHERE status IS NULL;

-- Add sample categories
INSERT INTO categories (name, description, color, created_at, updated_at) VALUES
('Electronics', 'Electronic devices and accessories', '#3B82F6', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Food & Beverages', 'Food items and drinks', '#10B981', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Clothing', 'Apparel and fashion items', '#F59E0B', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Books & Stationery', 'Books, notebooks and office supplies', '#8B5CF6', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Health & Beauty', 'Healthcare and cosmetic products', '#EF4444', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Add sample suppliers
INSERT INTO suppliers (name, contact_person, email, phone, address, status, created_at, updated_at) VALUES
('TechCorp Solutions', 'John Smith', 'john@techcorp.com', '+1234567890', '123 Tech Street, Silicon Valley', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Fresh Foods Ltd', 'Sarah Johnson', 'sarah@freshfoods.com', '+1234567891', '456 Market Avenue, Food District', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Fashion Forward Inc', 'Mike Davis', 'mike@fashionforward.com', '+1234567892', '789 Style Boulevard, Fashion Quarter', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
