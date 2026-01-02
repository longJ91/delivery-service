-- Delivery Service Database Schema
-- Generated for jOOQ Code Generation

-- =====================================================
-- Customer Tables
-- =====================================================
CREATE TABLE customers (
    id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    last_login_at TIMESTAMP,
    deleted_at TIMESTAMP,
    version BIGINT
);

CREATE TABLE customer_addresses (
    id VARCHAR(36) PRIMARY KEY,
    customer_id VARCHAR(36) NOT NULL REFERENCES customers(id),
    name VARCHAR(50) NOT NULL,
    recipient_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    postal_code VARCHAR(10) NOT NULL,
    address1 VARCHAR(200) NOT NULL,
    address2 VARCHAR(200),
    is_default BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL
);

-- =====================================================
-- Seller Tables
-- =====================================================
CREATE TABLE sellers (
    id VARCHAR(36) PRIMARY KEY,
    business_name VARCHAR(200) NOT NULL,
    business_number VARCHAR(20) NOT NULL UNIQUE,
    representative_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(20),
    seller_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    warehouse_postal_code VARCHAR(10),
    warehouse_address1 VARCHAR(200),
    warehouse_address2 VARCHAR(200),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    approved_at TIMESTAMP,
    deleted_at TIMESTAMP,
    version BIGINT
);

CREATE TABLE seller_categories (
    seller_id VARCHAR(36) NOT NULL REFERENCES sellers(id),
    category_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (seller_id, category_id)
);

-- =====================================================
-- Category Tables
-- =====================================================
CREATE TABLE categories (
    id VARCHAR(36) PRIMARY KEY,
    parent_id VARCHAR(36),
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    image_url VARCHAR(500),
    display_order INT NOT NULL,
    depth INT NOT NULL,
    is_active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- =====================================================
-- Product Tables
-- =====================================================
CREATE TABLE products (
    id VARCHAR(36) PRIMARY KEY,
    seller_id VARCHAR(36) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    base_price DECIMAL(12, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_stock_quantity INT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP,
    version BIGINT
);

CREATE TABLE product_variants (
    id VARCHAR(36) PRIMARY KEY,
    product_id VARCHAR(36) NOT NULL REFERENCES products(id),
    name VARCHAR(200) NOT NULL,
    sku VARCHAR(50),
    option_values VARCHAR(2000),
    additional_price DECIMAL(10, 2) NOT NULL,
    stock_quantity INT NOT NULL,
    is_active BOOLEAN NOT NULL
);

CREATE TABLE product_categories (
    product_id VARCHAR(36) NOT NULL REFERENCES products(id),
    category_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (product_id, category_id)
);

CREATE TABLE product_images (
    product_id VARCHAR(36) NOT NULL REFERENCES products(id),
    image_url VARCHAR(500),
    display_order INT NOT NULL,
    PRIMARY KEY (product_id, display_order)
);

-- =====================================================
-- Order Tables
-- =====================================================
CREATE TABLE orders (
    id VARCHAR(36) PRIMARY KEY,
    order_number VARCHAR(30) NOT NULL UNIQUE,
    customer_id VARCHAR(36) NOT NULL,
    seller_id VARCHAR(36) NOT NULL,
    status VARCHAR(30) NOT NULL,
    shipping_recipient_name VARCHAR(100),
    shipping_phone_number VARCHAR(20),
    shipping_postal_code VARCHAR(10),
    shipping_address1 VARCHAR(200),
    shipping_address2 VARCHAR(200),
    shipping_delivery_note VARCHAR(500),
    subtotal_amount DECIMAL(12, 2) NOT NULL,
    shipping_fee DECIMAL(10, 2) NOT NULL,
    discount_amount DECIMAL(10, 2) NOT NULL,
    total_amount DECIMAL(12, 2) NOT NULL,
    order_memo VARCHAR(500),
    shipping_memo VARCHAR(500),
    coupon_id VARCHAR(36),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    paid_at TIMESTAMP,
    confirmed_at TIMESTAMP,
    shipped_at TIMESTAMP,
    delivered_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    version BIGINT
);

CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL REFERENCES orders(id),
    product_id VARCHAR(36) NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    variant_id VARCHAR(36),
    variant_name VARCHAR(200),
    sku VARCHAR(50),
    option_values VARCHAR(2000),
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL
);

-- =====================================================
-- Review Tables
-- =====================================================
CREATE TABLE reviews (
    id VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL UNIQUE,
    customer_id VARCHAR(36) NOT NULL,
    seller_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    rating INT NOT NULL,
    content TEXT,
    is_visible BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP
);

CREATE TABLE review_images (
    id VARCHAR(36) PRIMARY KEY,
    review_id VARCHAR(36) NOT NULL REFERENCES reviews(id),
    image_url VARCHAR(255) NOT NULL,
    display_order INT NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE review_replies (
    id VARCHAR(36) PRIMARY KEY,
    review_id VARCHAR(36) NOT NULL UNIQUE REFERENCES reviews(id),
    seller_id VARCHAR(36) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- =====================================================
-- Cart Tables
-- =====================================================
CREATE TABLE carts (
    id VARCHAR(36) PRIMARY KEY,
    customer_id VARCHAR(36) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE cart_items (
    id VARCHAR(36) PRIMARY KEY,
    cart_id VARCHAR(36) NOT NULL REFERENCES carts(id),
    product_id VARCHAR(36) NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    variant_id VARCHAR(36),
    variant_name VARCHAR(200),
    seller_id VARCHAR(36) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(12, 2) NOT NULL,
    thumbnail_url VARCHAR(500),
    added_at TIMESTAMP NOT NULL
);

-- =====================================================
-- Coupon Tables
-- =====================================================
CREATE TABLE coupons (
    id VARCHAR(36) PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    discount_type VARCHAR(20) NOT NULL,
    discount_value DECIMAL(10, 2) NOT NULL,
    minimum_order_amount DECIMAL(10, 2) NOT NULL,
    maximum_discount_amount DECIMAL(10, 2),
    scope VARCHAR(20) NOT NULL,
    scope_target_id VARCHAR(36),
    total_quantity INT,
    used_quantity INT NOT NULL,
    valid_from TIMESTAMP,
    valid_until TIMESTAMP,
    is_active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT
);

-- =====================================================
-- Webhook Tables
-- =====================================================
CREATE TABLE webhook_subscriptions (
    id VARCHAR(36) PRIMARY KEY,
    seller_id VARCHAR(36) NOT NULL,
    name VARCHAR(100) NOT NULL,
    endpoint_url VARCHAR(500) NOT NULL,
    secret VARCHAR(100) NOT NULL,
    is_active BOOLEAN NOT NULL,
    failure_count INT NOT NULL,
    last_delivery_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT
);

CREATE TABLE webhook_subscribed_events (
    subscription_id VARCHAR(36) NOT NULL REFERENCES webhook_subscriptions(id),
    event_type VARCHAR(50) NOT NULL,
    PRIMARY KEY (subscription_id, event_type)
);

CREATE TABLE webhook_deliveries (
    id VARCHAR(36) PRIMARY KEY,
    subscription_id VARCHAR(36) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    payload TEXT NOT NULL,
    endpoint_url VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL,
    http_status_code INT,
    response_body TEXT,
    attempt_count INT NOT NULL,
    next_retry_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    delivered_at TIMESTAMP
);

-- =====================================================
-- Shipment Tables
-- =====================================================
CREATE TABLE shipments (
    id VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    seller_id VARCHAR(36) NOT NULL,
    carrier VARCHAR(50) NOT NULL,
    tracking_number VARCHAR(100),
    status VARCHAR(30) NOT NULL,
    sender_name VARCHAR(100),
    sender_phone VARCHAR(20),
    sender_address VARCHAR(500),
    recipient_name VARCHAR(100),
    recipient_phone VARCHAR(20),
    recipient_address VARCHAR(500),
    delivery_note VARCHAR(500),
    shipped_at TIMESTAMP,
    delivered_at TIMESTAMP,
    estimated_delivery TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT
);

CREATE TABLE shipment_trackings (
    id VARCHAR(36) PRIMARY KEY,
    shipment_id VARCHAR(36) NOT NULL REFERENCES shipments(id),
    status VARCHAR(30) NOT NULL,
    location VARCHAR(200),
    description VARCHAR(500),
    event_time TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL
);

-- =====================================================
-- Payment Tables
-- =====================================================
CREATE TABLE payments (
    id VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    customer_id VARCHAR(36) NOT NULL,
    payment_method VARCHAR(30) NOT NULL,
    payment_key VARCHAR(100),
    amount DECIMAL(12, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    pg_provider VARCHAR(30),
    pg_transaction_id VARCHAR(100),
    card_company VARCHAR(30),
    card_number_masked VARCHAR(20),
    installment_months INT,
    approved_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    cancel_reason VARCHAR(500),
    metadata TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT
);

-- =====================================================
-- Return Tables
-- =====================================================
CREATE TABLE returns (
    id VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    customer_id VARCHAR(36) NOT NULL,
    seller_id VARCHAR(36) NOT NULL,
    return_type VARCHAR(20) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    status VARCHAR(30) NOT NULL,
    refund_amount DECIMAL(12, 2),
    refund_method VARCHAR(30),
    pickup_address VARCHAR(500),
    pickup_note VARCHAR(500),
    admin_note VARCHAR(500),
    requested_at TIMESTAMP NOT NULL,
    approved_at TIMESTAMP,
    completed_at TIMESTAMP,
    rejected_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT
);

CREATE TABLE return_items (
    id VARCHAR(36) PRIMARY KEY,
    return_id VARCHAR(36) NOT NULL REFERENCES returns(id),
    order_item_id BIGINT NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    variant_id VARCHAR(36),
    variant_name VARCHAR(200),
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    reason VARCHAR(500)
);

-- =====================================================
-- Indexes
-- =====================================================
CREATE INDEX idx_customers_email ON customers(email);
CREATE INDEX idx_customers_status ON customers(status);
CREATE INDEX idx_customer_addresses_customer_id ON customer_addresses(customer_id);

CREATE INDEX idx_sellers_business_number ON sellers(business_number);
CREATE INDEX idx_sellers_status ON sellers(status);

CREATE INDEX idx_categories_parent_id ON categories(parent_id);
CREATE INDEX idx_categories_depth ON categories(depth);
CREATE INDEX idx_categories_display_order ON categories(display_order);

CREATE INDEX idx_products_seller_id ON products(seller_id);
CREATE INDEX idx_products_status ON products(status);
CREATE INDEX idx_products_name ON products(name);
CREATE INDEX idx_product_variants_product_id ON product_variants(product_id);
CREATE INDEX idx_product_variants_sku ON product_variants(sku);

CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_seller_id ON orders(seller_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);
CREATE INDEX idx_order_items_variant_id ON order_items(variant_id);

CREATE INDEX idx_reviews_customer ON reviews(customer_id);
CREATE INDEX idx_reviews_seller ON reviews(seller_id);
CREATE INDEX idx_reviews_product ON reviews(product_id);
CREATE INDEX idx_reviews_rating ON reviews(rating);
CREATE INDEX idx_review_images_review ON review_images(review_id);
CREATE INDEX idx_review_replies_seller ON review_replies(seller_id);

CREATE INDEX idx_carts_customer_id ON carts(customer_id);
CREATE INDEX idx_cart_items_cart_id ON cart_items(cart_id);
CREATE INDEX idx_cart_items_product_id ON cart_items(product_id);

CREATE INDEX idx_coupons_code ON coupons(code);
CREATE INDEX idx_coupons_valid_until ON coupons(valid_until);

CREATE INDEX idx_webhook_subscriptions_seller_id ON webhook_subscriptions(seller_id);
CREATE INDEX idx_webhook_subscriptions_is_active ON webhook_subscriptions(is_active);
CREATE INDEX idx_webhook_deliveries_subscription_id ON webhook_deliveries(subscription_id);
CREATE INDEX idx_webhook_deliveries_status ON webhook_deliveries(status);
CREATE INDEX idx_webhook_deliveries_created_at ON webhook_deliveries(created_at);
