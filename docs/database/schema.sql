-- =============================================================================
-- Delivery Service Database Schema
-- Version: 1.0.0
-- Database: PostgreSQL 15+
-- Created: 2025-01-01
-- =============================================================================
--
-- Bounded Contexts:
--   1. User Domain (customers, customer_addresses, riders)
--   2. Shop Domain (shops, shop_categories, shop_addresses, shop_business_hours)
--   3. Menu Domain (menu_categories, menus, menu_option_groups, menu_options)
--   4. Order Domain (orders, order_items, order_item_options, order_status_histories)
--   5. Delivery Domain (deliveries, delivery_tracking, rider_assignments)
--   6. Payment Domain (payments, refunds, payment_methods)
--   7. Promotion Domain (coupons, customer_coupons, promotions)
--   8. Review Domain (reviews, review_images, review_replies)
--
-- Design Principles:
--   - UUID for distributed ID generation
--   - Soft delete with deleted_at column
--   - Snapshot pattern for order-time data preservation
--   - Optimistic locking with version column
--   - Audit logging with history tables
-- =============================================================================

-- =============================================================================
-- EXTENSIONS
-- =============================================================================
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =============================================================================
-- ENUM TYPES
-- =============================================================================

-- Customer status
CREATE TYPE customer_status AS ENUM ('ACTIVE', 'SUSPENDED', 'WITHDRAWN');

-- Rider status
CREATE TYPE rider_status AS ENUM ('PENDING', 'ACTIVE', 'SUSPENDED', 'INACTIVE');

-- Shop status
CREATE TYPE shop_status AS ENUM ('PENDING', 'ACTIVE', 'SUSPENDED', 'CLOSED');

-- Order status
CREATE TYPE order_status AS ENUM (
    'PENDING',
    'ACCEPTED',
    'PREPARING',
    'READY_FOR_DELIVERY',
    'PICKED_UP',
    'DELIVERED',
    'CANCELLED'
);

-- Delivery status
CREATE TYPE delivery_status AS ENUM (
    'PENDING',
    'ASSIGNED',
    'PICKED_UP',
    'DELIVERING',
    'DELIVERED',
    'FAILED'
);

-- Rider assignment status
CREATE TYPE assignment_status AS ENUM ('REQUESTED', 'ACCEPTED', 'REJECTED', 'CANCELLED');

-- Payment method type
CREATE TYPE payment_method_type AS ENUM (
    'CARD',
    'KAKAO_PAY',
    'NAVER_PAY',
    'TOSS',
    'CASH_ON_DELIVERY'
);

-- Payment status
CREATE TYPE payment_status AS ENUM (
    'PENDING',
    'COMPLETED',
    'FAILED',
    'CANCELLED',
    'REFUNDED',
    'PARTIAL_REFUNDED'
);

-- Refund type
CREATE TYPE refund_type AS ENUM ('FULL', 'PARTIAL');

-- Refund status
CREATE TYPE refund_status AS ENUM ('REQUESTED', 'PROCESSING', 'COMPLETED', 'FAILED');

-- Refund requester
CREATE TYPE refund_requester AS ENUM ('CUSTOMER', 'SHOP', 'SYSTEM');

-- Discount type
CREATE TYPE discount_type AS ENUM ('FIXED', 'PERCENTAGE');

-- Coupon scope
CREATE TYPE coupon_scope AS ENUM ('ALL', 'SHOP_SPECIFIC', 'CATEGORY_SPECIFIC');

-- Customer coupon status
CREATE TYPE customer_coupon_status AS ENUM ('AVAILABLE', 'USED', 'EXPIRED');

-- Promotion type
CREATE TYPE promotion_type AS ENUM ('DISCOUNT', 'FREE_DELIVERY', 'BUNDLE', 'POINT_BOOST');

-- Saved payment method type
CREATE TYPE saved_payment_type AS ENUM ('CARD', 'EASY_PAY');

-- =============================================================================
-- 1. USER DOMAIN
-- =============================================================================

-- Customers (고객)
CREATE TABLE customers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    profile_image_url VARCHAR(255),
    status customer_status NOT NULL DEFAULT 'ACTIVE',
    point_balance DECIMAL(12, 0) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

-- Customer Addresses (고객 배송지)
CREATE TABLE customer_addresses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id UUID NOT NULL REFERENCES customers(id),
    name VARCHAR(50) NOT NULL,
    recipient_name VARCHAR(50) NOT NULL,
    recipient_phone VARCHAR(20) NOT NULL,
    road_address VARCHAR(200) NOT NULL,
    detail_address VARCHAR(100),
    postal_code VARCHAR(10),
    latitude DECIMAL(10, 7),
    longitude DECIMAL(10, 7),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

-- Riders (라이더)
CREATE TABLE riders (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    profile_image_url VARCHAR(255),
    vehicle_type VARCHAR(20) NOT NULL,
    vehicle_number VARCHAR(20),
    status rider_status NOT NULL DEFAULT 'PENDING',
    rating_avg DECIMAL(2, 1) DEFAULT 0.0,
    delivery_count INT NOT NULL DEFAULT 0,
    is_available BOOLEAN NOT NULL DEFAULT FALSE,
    current_latitude DECIMAL(10, 7),
    current_longitude DECIMAL(10, 7),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

-- =============================================================================
-- 2. SHOP DOMAIN
-- =============================================================================

-- Shop Categories (가게 카테고리)
CREATE TABLE shop_categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(50) NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    icon_url VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Shops (가게)
CREATE TABLE shops (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    owner_id UUID NOT NULL REFERENCES customers(id),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    phone VARCHAR(20) NOT NULL,
    business_number VARCHAR(12) UNIQUE,
    category_id UUID REFERENCES shop_categories(id),
    min_order_amount DECIMAL(10, 0) NOT NULL DEFAULT 0,
    delivery_fee DECIMAL(10, 0) NOT NULL DEFAULT 0,
    estimated_delivery_time INT NOT NULL DEFAULT 30,
    rating_avg DECIMAL(2, 1) NOT NULL DEFAULT 0.0,
    review_count INT NOT NULL DEFAULT 0,
    status shop_status NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

-- Shop Addresses (가게 주소)
CREATE TABLE shop_addresses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    shop_id UUID NOT NULL UNIQUE REFERENCES shops(id),
    road_address VARCHAR(200) NOT NULL,
    detail_address VARCHAR(100),
    postal_code VARCHAR(10),
    latitude DECIMAL(10, 7) NOT NULL,
    longitude DECIMAL(10, 7) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Shop Business Hours (영업시간)
CREATE TABLE shop_business_hours (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    shop_id UUID NOT NULL REFERENCES shops(id),
    day_of_week SMALLINT NOT NULL CHECK (day_of_week >= 0 AND day_of_week <= 6),
    open_time TIME,
    close_time TIME,
    is_closed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (shop_id, day_of_week)
);

-- =============================================================================
-- 3. MENU DOMAIN
-- =============================================================================

-- Menu Categories (메뉴 카테고리)
CREATE TABLE menu_categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    shop_id UUID NOT NULL REFERENCES shops(id),
    name VARCHAR(50) NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Menus (메뉴)
CREATE TABLE menus (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    shop_id UUID NOT NULL REFERENCES shops(id),
    category_id UUID REFERENCES menu_categories(id),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10, 0) NOT NULL,
    image_url VARCHAR(255),
    is_popular BOOLEAN NOT NULL DEFAULT FALSE,
    is_recommended BOOLEAN NOT NULL DEFAULT FALSE,
    is_sold_out BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

-- Menu Option Groups (옵션 그룹)
CREATE TABLE menu_option_groups (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    menu_id UUID NOT NULL REFERENCES menus(id),
    name VARCHAR(50) NOT NULL,
    is_required BOOLEAN NOT NULL DEFAULT FALSE,
    min_selections INT NOT NULL DEFAULT 0,
    max_selections INT NOT NULL DEFAULT 1,
    display_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Menu Options (개별 옵션)
CREATE TABLE menu_options (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    option_group_id UUID NOT NULL REFERENCES menu_option_groups(id),
    name VARCHAR(50) NOT NULL,
    additional_price DECIMAL(10, 0) NOT NULL DEFAULT 0,
    is_sold_out BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- 4. PROMOTION DOMAIN (Orders depend on coupons, so define first)
-- =============================================================================

-- Coupons (쿠폰)
CREATE TABLE coupons (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    discount_type discount_type NOT NULL,
    discount_value DECIMAL(10, 0) NOT NULL,
    max_discount_amount DECIMAL(10, 0),
    min_order_amount DECIMAL(10, 0) NOT NULL DEFAULT 0,
    max_uses_total INT,
    max_uses_per_user INT NOT NULL DEFAULT 1,
    current_uses INT NOT NULL DEFAULT 0,
    scope coupon_scope NOT NULL DEFAULT 'ALL',
    shop_id UUID REFERENCES shops(id),
    valid_from TIMESTAMP NOT NULL,
    valid_until TIMESTAMP NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Promotions (프로모션)
CREATE TABLE promotions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    promotion_type promotion_type NOT NULL,
    condition_json JSONB NOT NULL DEFAULT '{}',
    benefit_json JSONB NOT NULL DEFAULT '{}',
    start_at TIMESTAMP NOT NULL,
    end_at TIMESTAMP NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    priority INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- 4. ORDER DOMAIN
-- =============================================================================

-- Orders (주문)
CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_number VARCHAR(20) NOT NULL UNIQUE,
    customer_id UUID NOT NULL REFERENCES customers(id),
    shop_id UUID NOT NULL REFERENCES shops(id),
    delivery_address_id UUID REFERENCES customer_addresses(id),
    delivery_address_snapshot JSONB NOT NULL,
    status order_status NOT NULL DEFAULT 'PENDING',
    total_amount DECIMAL(12, 0) NOT NULL,
    delivery_fee DECIMAL(10, 0) NOT NULL DEFAULT 0,
    discount_amount DECIMAL(10, 0) NOT NULL DEFAULT 0,
    final_amount DECIMAL(12, 0) NOT NULL,
    coupon_id UUID REFERENCES coupons(id),
    order_request TEXT,
    delivery_request TEXT,
    estimated_delivery_at TIMESTAMP,
    ordered_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    accepted_at TIMESTAMP,
    prepared_at TIMESTAMP,
    picked_up_at TIMESTAMP,
    delivered_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    cancel_reason VARCHAR(200),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

-- Order Items (주문 항목)
CREATE TABLE order_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id UUID NOT NULL REFERENCES orders(id),
    menu_id UUID NOT NULL REFERENCES menus(id),
    menu_name_snapshot VARCHAR(100) NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10, 0) NOT NULL,
    total_price DECIMAL(10, 0) NOT NULL,
    item_request TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Order Item Options (주문 항목 옵션)
CREATE TABLE order_item_options (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_item_id UUID NOT NULL REFERENCES order_items(id),
    option_id UUID NOT NULL REFERENCES menu_options(id),
    option_name_snapshot VARCHAR(50) NOT NULL,
    additional_price DECIMAL(10, 0) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Order Status Histories (주문 상태 변경 이력)
CREATE TABLE order_status_histories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id UUID NOT NULL REFERENCES orders(id),
    previous_status order_status,
    new_status order_status NOT NULL,
    changed_by VARCHAR(50) NOT NULL,
    changed_by_id UUID,
    reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Customer Coupons (고객 보유 쿠폰)
CREATE TABLE customer_coupons (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id UUID NOT NULL REFERENCES customers(id),
    coupon_id UUID NOT NULL REFERENCES coupons(id),
    status customer_coupon_status NOT NULL DEFAULT 'AVAILABLE',
    used_at TIMESTAMP,
    used_order_id UUID REFERENCES orders(id),
    issued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- 5. DELIVERY DOMAIN
-- =============================================================================

-- Deliveries (배달)
CREATE TABLE deliveries (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id UUID NOT NULL UNIQUE REFERENCES orders(id),
    rider_id UUID REFERENCES riders(id),
    status delivery_status NOT NULL DEFAULT 'PENDING',
    pickup_address_snapshot JSONB NOT NULL,
    delivery_address_snapshot JSONB NOT NULL,
    distance_meters INT,
    assigned_at TIMESTAMP,
    picked_up_at TIMESTAMP,
    delivered_at TIMESTAMP,
    delivery_photo_url VARCHAR(255),
    failure_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Delivery Tracking (실시간 배달 추적)
CREATE TABLE delivery_tracking (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    delivery_id UUID NOT NULL REFERENCES deliveries(id),
    latitude DECIMAL(10, 7) NOT NULL,
    longitude DECIMAL(10, 7) NOT NULL,
    recorded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Rider Assignments (라이더 배정 이력)
CREATE TABLE rider_assignments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    delivery_id UUID NOT NULL REFERENCES deliveries(id),
    rider_id UUID NOT NULL REFERENCES riders(id),
    status assignment_status NOT NULL DEFAULT 'REQUESTED',
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    responded_at TIMESTAMP,
    reject_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- 6. PAYMENT DOMAIN
-- =============================================================================

-- Payments (결제)
CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id UUID NOT NULL UNIQUE REFERENCES orders(id),
    payment_method payment_method_type NOT NULL,
    status payment_status NOT NULL DEFAULT 'PENDING',
    amount DECIMAL(12, 0) NOT NULL,
    pg_provider VARCHAR(50),
    pg_transaction_id VARCHAR(100),
    card_company VARCHAR(50),
    card_number_masked VARCHAR(20),
    installment_months SMALLINT NOT NULL DEFAULT 0,
    paid_at TIMESTAMP,
    failed_at TIMESTAMP,
    failure_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Refunds (환불)
CREATE TABLE refunds (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    payment_id UUID NOT NULL REFERENCES payments(id),
    order_id UUID NOT NULL REFERENCES orders(id),
    refund_type refund_type NOT NULL,
    amount DECIMAL(12, 0) NOT NULL,
    reason TEXT,
    status refund_status NOT NULL DEFAULT 'REQUESTED',
    pg_refund_id VARCHAR(100),
    requested_by refund_requester NOT NULL,
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Payment Methods (저장된 결제수단)
CREATE TABLE payment_methods (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id UUID NOT NULL REFERENCES customers(id),
    method_type saved_payment_type NOT NULL,
    provider VARCHAR(50) NOT NULL,
    token VARCHAR(255) NOT NULL,
    display_name VARCHAR(50) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

-- =============================================================================
-- 7. REVIEW DOMAIN
-- =============================================================================

-- Reviews (리뷰)
CREATE TABLE reviews (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id UUID NOT NULL UNIQUE REFERENCES orders(id),
    customer_id UUID NOT NULL REFERENCES customers(id),
    shop_id UUID NOT NULL REFERENCES shops(id),
    rating SMALLINT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    content TEXT,
    is_visible BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

-- Review Images (리뷰 이미지)
CREATE TABLE review_images (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    review_id UUID NOT NULL REFERENCES reviews(id),
    image_url VARCHAR(255) NOT NULL,
    display_order SMALLINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Review Replies (사장님 답글)
CREATE TABLE review_replies (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    review_id UUID NOT NULL UNIQUE REFERENCES reviews(id),
    shop_id UUID NOT NULL REFERENCES shops(id),
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- INDEXES
-- =============================================================================

-- Customer indexes
CREATE INDEX idx_customers_email ON customers(email) WHERE deleted_at IS NULL;
CREATE INDEX idx_customers_phone ON customers(phone) WHERE deleted_at IS NULL;
CREATE INDEX idx_customer_addresses_customer ON customer_addresses(customer_id) WHERE deleted_at IS NULL;

-- Rider indexes
CREATE INDEX idx_riders_status_available ON riders(status, is_available) WHERE deleted_at IS NULL;
CREATE INDEX idx_riders_location ON riders(current_latitude, current_longitude)
    WHERE is_available = TRUE AND deleted_at IS NULL;

-- Shop indexes
CREATE INDEX idx_shops_category_status ON shops(category_id, status) WHERE deleted_at IS NULL;
CREATE INDEX idx_shops_owner ON shops(owner_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_shops_rating ON shops(rating_avg DESC) WHERE status = 'ACTIVE' AND deleted_at IS NULL;

-- Menu indexes
CREATE INDEX idx_menus_shop ON menus(shop_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_menus_category ON menus(category_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_menu_categories_shop ON menu_categories(shop_id);

-- Order indexes
CREATE INDEX idx_orders_customer ON orders(customer_id, created_at DESC);
CREATE INDEX idx_orders_shop_status ON orders(shop_id, status, created_at DESC);
CREATE INDEX idx_orders_status ON orders(status, created_at DESC);
CREATE INDEX idx_orders_number ON orders(order_number);
CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_order_status_histories_order ON order_status_histories(order_id, created_at DESC);

-- Delivery indexes
CREATE INDEX idx_deliveries_order ON deliveries(order_id);
CREATE INDEX idx_deliveries_rider ON deliveries(rider_id, status) WHERE rider_id IS NOT NULL;
CREATE INDEX idx_deliveries_status ON deliveries(status, created_at DESC);
CREATE INDEX idx_delivery_tracking_delivery ON delivery_tracking(delivery_id, recorded_at DESC);
CREATE INDEX idx_rider_assignments_delivery ON rider_assignments(delivery_id);
CREATE INDEX idx_rider_assignments_rider ON rider_assignments(rider_id, status);

-- Payment indexes
CREATE INDEX idx_payments_order ON payments(order_id);
CREATE INDEX idx_payments_status ON payments(status, created_at DESC);
CREATE INDEX idx_refunds_payment ON refunds(payment_id);
CREATE INDEX idx_refunds_order ON refunds(order_id);
CREATE INDEX idx_payment_methods_customer ON payment_methods(customer_id) WHERE deleted_at IS NULL;

-- Coupon indexes
CREATE INDEX idx_coupons_code ON coupons(code) WHERE is_active = TRUE;
CREATE INDEX idx_coupons_valid ON coupons(valid_until, is_active) WHERE is_active = TRUE;
CREATE INDEX idx_customer_coupons_customer ON customer_coupons(customer_id, status);
CREATE INDEX idx_customer_coupons_expiry ON customer_coupons(expires_at) WHERE status = 'AVAILABLE';

-- Review indexes
CREATE INDEX idx_reviews_shop ON reviews(shop_id, is_visible, created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX idx_reviews_customer ON reviews(customer_id, created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX idx_review_images_review ON review_images(review_id);

-- =============================================================================
-- TRIGGERS
-- =============================================================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply updated_at trigger to all tables with updated_at column
DO $$
DECLARE
    t text;
BEGIN
    FOR t IN
        SELECT table_name
        FROM information_schema.columns
        WHERE column_name = 'updated_at'
        AND table_schema = 'public'
    LOOP
        EXECUTE format('
            CREATE TRIGGER update_%I_updated_at
            BEFORE UPDATE ON %I
            FOR EACH ROW
            EXECUTE FUNCTION update_updated_at_column()
        ', t, t);
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- Function to generate order number
CREATE OR REPLACE FUNCTION generate_order_number()
RETURNS TRIGGER AS $$
DECLARE
    date_part VARCHAR(8);
    seq_num INT;
BEGIN
    date_part := TO_CHAR(CURRENT_DATE, 'YYYYMMDD');

    SELECT COALESCE(MAX(CAST(SUBSTRING(order_number FROM 13) AS INT)), 0) + 1
    INTO seq_num
    FROM orders
    WHERE order_number LIKE 'ORD-' || date_part || '-%';

    NEW.order_number := 'ORD-' || date_part || '-' || LPAD(seq_num::TEXT, 5, '0');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER generate_order_number_trigger
BEFORE INSERT ON orders
FOR EACH ROW
WHEN (NEW.order_number IS NULL OR NEW.order_number = '')
EXECUTE FUNCTION generate_order_number();

-- Function to update shop rating when review is added/updated
CREATE OR REPLACE FUNCTION update_shop_rating()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE shops
    SET rating_avg = (
        SELECT COALESCE(AVG(rating), 0)
        FROM reviews
        WHERE shop_id = COALESCE(NEW.shop_id, OLD.shop_id)
        AND is_visible = TRUE
        AND deleted_at IS NULL
    ),
    review_count = (
        SELECT COUNT(*)
        FROM reviews
        WHERE shop_id = COALESCE(NEW.shop_id, OLD.shop_id)
        AND is_visible = TRUE
        AND deleted_at IS NULL
    )
    WHERE id = COALESCE(NEW.shop_id, OLD.shop_id);

    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_shop_rating_on_review
AFTER INSERT OR UPDATE OR DELETE ON reviews
FOR EACH ROW
EXECUTE FUNCTION update_shop_rating();

-- =============================================================================
-- COMMENTS
-- =============================================================================

COMMENT ON TABLE customers IS '고객 정보';
COMMENT ON TABLE customer_addresses IS '고객 배송지 주소';
COMMENT ON TABLE riders IS '배달 라이더 정보';
COMMENT ON TABLE shops IS '가게 정보';
COMMENT ON TABLE shop_categories IS '가게 카테고리';
COMMENT ON TABLE shop_addresses IS '가게 주소';
COMMENT ON TABLE shop_business_hours IS '가게 영업시간';
COMMENT ON TABLE menu_categories IS '메뉴 카테고리 (가게별)';
COMMENT ON TABLE menus IS '메뉴';
COMMENT ON TABLE menu_option_groups IS '메뉴 옵션 그룹';
COMMENT ON TABLE menu_options IS '메뉴 옵션';
COMMENT ON TABLE orders IS '주문';
COMMENT ON TABLE order_items IS '주문 항목';
COMMENT ON TABLE order_item_options IS '주문 항목별 선택 옵션';
COMMENT ON TABLE order_status_histories IS '주문 상태 변경 이력';
COMMENT ON TABLE deliveries IS '배달 정보';
COMMENT ON TABLE delivery_tracking IS '실시간 배달 위치 추적';
COMMENT ON TABLE rider_assignments IS '라이더 배정 이력';
COMMENT ON TABLE payments IS '결제 정보';
COMMENT ON TABLE refunds IS '환불 정보';
COMMENT ON TABLE payment_methods IS '저장된 결제수단';
COMMENT ON TABLE coupons IS '쿠폰';
COMMENT ON TABLE customer_coupons IS '고객 보유 쿠폰';
COMMENT ON TABLE promotions IS '프로모션/이벤트';
COMMENT ON TABLE reviews IS '리뷰';
COMMENT ON TABLE review_images IS '리뷰 이미지';
COMMENT ON TABLE review_replies IS '사장님 답글';

-- =============================================================================
-- SAMPLE DATA (Optional - for development)
-- =============================================================================

-- Insert sample shop categories
INSERT INTO shop_categories (id, name, display_order, icon_url) VALUES
    (uuid_generate_v4(), '치킨', 1, '/icons/chicken.svg'),
    (uuid_generate_v4(), '피자', 2, '/icons/pizza.svg'),
    (uuid_generate_v4(), '한식', 3, '/icons/korean.svg'),
    (uuid_generate_v4(), '중식', 4, '/icons/chinese.svg'),
    (uuid_generate_v4(), '일식', 5, '/icons/japanese.svg'),
    (uuid_generate_v4(), '분식', 6, '/icons/snack.svg'),
    (uuid_generate_v4(), '카페/디저트', 7, '/icons/cafe.svg'),
    (uuid_generate_v4(), '패스트푸드', 8, '/icons/fastfood.svg');

-- =============================================================================
-- END OF SCHEMA
-- =============================================================================
