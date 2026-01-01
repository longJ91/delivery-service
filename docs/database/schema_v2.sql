-- =============================================================================
-- Delivery Service Database Schema v2.0
-- Type: Product Delivery Service (물품 배송 서비스)
-- Version: 2.0.0
-- Database: PostgreSQL 15+
-- Created: 2025-01-01
-- =============================================================================
--
-- Changes from v1.0 (Food Delivery):
--   - Menu Domain → Product Domain (전면 재설계)
--   - Shop Domain → Seller Domain (명칭 변경)
--   - Delivery Domain → Shipment Domain (택배 모델)
--   - Added Return/Exchange Domain (반품/교환)
--   - Removed: riders, rider_assignments, shop_business_hours
--
-- Bounded Contexts:
--   1. User Domain (customers, customer_addresses)
--   2. Seller Domain (sellers, seller_categories, seller_addresses)
--   3. Product Domain (product_categories, products, product_variants, product_images, product_specifications)
--   4. Order Domain (orders, order_items, order_status_histories)
--   5. Shipment Domain (shipments, shipment_tracking, shipping_carriers)
--   6. Payment Domain (payments, refunds, payment_methods)
--   7. Promotion Domain (coupons, customer_coupons, promotions)
--   8. Review Domain (reviews, review_images, review_replies)
--   9. Return Domain (returns, return_items) - NEW
--
-- Design Constraints:
--   - Courier-only delivery (택배사 연동만 지원)
--   - Single warehouse per seller (판매자당 단일 창고)
--   - Domestic shipping only (국내 배송만)
--   - Return/Exchange supported (반품/교환 지원)
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

-- Seller type
CREATE TYPE seller_type AS ENUM ('INDIVIDUAL', 'BUSINESS');

-- Seller status
CREATE TYPE seller_status AS ENUM ('PENDING', 'ACTIVE', 'SUSPENDED', 'CLOSED');

-- Product status
CREATE TYPE product_status AS ENUM ('DRAFT', 'ACTIVE', 'INACTIVE', 'ARCHIVED');

-- Order status (물품 배송용)
CREATE TYPE order_status AS ENUM (
    'PENDING',              -- 주문 대기 (결제 전)
    'PAID',                 -- 결제 완료
    'CONFIRMED',            -- 주문 확정 (판매자 확인)
    'PREPARING',            -- 상품 준비중
    'SHIPPED',              -- 출고 완료
    'IN_TRANSIT',           -- 배송중
    'OUT_FOR_DELIVERY',     -- 배달중
    'DELIVERED',            -- 배송 완료
    'CANCELLED',            -- 취소
    'RETURN_REQUESTED',     -- 반품 요청
    'RETURNED'              -- 반품 완료
);

-- Shipment status
CREATE TYPE shipment_status AS ENUM (
    'PENDING',              -- 배송 준비 대기
    'READY_TO_SHIP',        -- 출고 준비 완료
    'PICKED_UP',            -- 택배사 수거 완료
    'IN_TRANSIT',           -- 배송중 (허브 이동)
    'OUT_FOR_DELIVERY',     -- 배달중 (최종 배송)
    'DELIVERED',            -- 배송 완료
    'FAILED',               -- 배송 실패
    'RETURNED'              -- 반송
);

-- Shipping carrier (택배사)
CREATE TYPE shipping_carrier AS ENUM (
    'CJ_LOGISTICS',         -- CJ대한통운
    'HANJIN',               -- 한진택배
    'LOTTE',                -- 롯데택배
    'LOGEN',                -- 로젠택배
    'POST_OFFICE',          -- 우체국택배
    'EPOST',                -- 우편등기
    'GS_POSTBOX',           -- GS편의점택배
    'CU_POST',              -- CU편의점택배
    'OTHER'                 -- 기타
);

-- Payment method type
CREATE TYPE payment_method_type AS ENUM (
    'CARD',
    'KAKAO_PAY',
    'NAVER_PAY',
    'TOSS',
    'BANK_TRANSFER',
    'VIRTUAL_ACCOUNT'
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
CREATE TYPE refund_requester AS ENUM ('CUSTOMER', 'SELLER', 'SYSTEM');

-- Discount type
CREATE TYPE discount_type AS ENUM ('FIXED', 'PERCENTAGE');

-- Coupon scope
CREATE TYPE coupon_scope AS ENUM ('ALL', 'SELLER_SPECIFIC', 'CATEGORY_SPECIFIC');

-- Customer coupon status
CREATE TYPE customer_coupon_status AS ENUM ('AVAILABLE', 'USED', 'EXPIRED');

-- Promotion type
CREATE TYPE promotion_type AS ENUM ('DISCOUNT', 'FREE_SHIPPING', 'BUNDLE', 'POINT_BOOST');

-- Saved payment method type
CREATE TYPE saved_payment_type AS ENUM ('CARD', 'EASY_PAY');

-- Return reason
CREATE TYPE return_reason AS ENUM (
    'CHANGE_OF_MIND',       -- 단순 변심
    'WRONG_ITEM',           -- 오배송
    'DEFECTIVE',            -- 불량/파손
    'DIFFERENT_FROM_DESC',  -- 상품 설명과 다름
    'DELAYED_DELIVERY',     -- 배송 지연
    'OTHER'                 -- 기타
);

-- Return status
CREATE TYPE return_status AS ENUM (
    'REQUESTED',            -- 반품 요청
    'APPROVED',             -- 반품 승인
    'REJECTED',             -- 반품 거절
    'COLLECTING',           -- 수거중
    'COLLECTED',            -- 수거 완료
    'INSPECTING',           -- 검수중
    'COMPLETED',            -- 반품 완료 (환불 처리)
    'CANCELLED'             -- 반품 취소
);

-- Return type
CREATE TYPE return_type AS ENUM ('RETURN', 'EXCHANGE');

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
    postal_code VARCHAR(10) NOT NULL,
    road_address VARCHAR(200) NOT NULL,
    detail_address VARCHAR(100),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

-- =============================================================================
-- 2. SELLER DOMAIN
-- =============================================================================

-- Seller Categories (판매자 분류)
CREATE TABLE seller_categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(50) NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    icon_url VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Sellers (판매자)
CREATE TABLE sellers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    owner_id UUID NOT NULL REFERENCES customers(id),
    category_id UUID REFERENCES seller_categories(id),

    -- 기본 정보
    seller_type seller_type NOT NULL DEFAULT 'BUSINESS',
    name VARCHAR(100) NOT NULL,
    company_name VARCHAR(100),
    representative_name VARCHAR(50),
    description TEXT,

    -- 사업자 정보
    business_number VARCHAR(12) UNIQUE,
    mail_order_number VARCHAR(50),

    -- 연락처
    phone VARCHAR(20) NOT NULL,
    cs_phone VARCHAR(20),
    cs_email VARCHAR(255),

    -- 평점
    rating_avg DECIMAL(2, 1) NOT NULL DEFAULT 0.0,
    review_count INT NOT NULL DEFAULT 0,

    -- 상태
    status seller_status NOT NULL DEFAULT 'PENDING',

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

-- Seller Addresses (판매자 주소 - 단일 창고)
CREATE TABLE seller_addresses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    seller_id UUID NOT NULL UNIQUE REFERENCES sellers(id),

    -- 사업장 주소
    business_postal_code VARCHAR(10) NOT NULL,
    business_road_address VARCHAR(200) NOT NULL,
    business_detail_address VARCHAR(100),

    -- 출고지 주소 (창고)
    shipping_postal_code VARCHAR(10) NOT NULL,
    shipping_road_address VARCHAR(200) NOT NULL,
    shipping_detail_address VARCHAR(100),

    -- 반품지 주소
    return_postal_code VARCHAR(10) NOT NULL,
    return_road_address VARCHAR(200) NOT NULL,
    return_detail_address VARCHAR(100),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- 3. PRODUCT DOMAIN
-- =============================================================================

-- Product Categories (상품 카테고리 - 계층형)
CREATE TABLE product_categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    parent_id UUID REFERENCES product_categories(id),
    name VARCHAR(50) NOT NULL,
    depth SMALLINT NOT NULL DEFAULT 1,
    path VARCHAR(255),
    display_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Products (상품)
CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    seller_id UUID NOT NULL REFERENCES sellers(id),
    category_id UUID REFERENCES product_categories(id),

    -- 기본 정보
    name VARCHAR(200) NOT NULL,
    description TEXT,

    -- 가격 (변형이 없는 단일 상품용)
    price DECIMAL(12, 0) NOT NULL,
    compare_at_price DECIMAL(12, 0),

    -- 상품 식별
    sku VARCHAR(50),
    barcode VARCHAR(50),

    -- 재고 (변형이 없는 단일 상품용)
    stock_quantity INT NOT NULL DEFAULT 0,
    safety_stock INT NOT NULL DEFAULT 0,
    is_track_inventory BOOLEAN NOT NULL DEFAULT TRUE,

    -- 배송 정보
    weight_g INT,
    width_mm INT,
    height_mm INT,
    depth_mm INT,

    -- 배송비 정책
    shipping_fee DECIMAL(10, 0) NOT NULL DEFAULT 0,
    free_shipping_threshold DECIMAL(12, 0),
    additional_shipping_fee DECIMAL(10, 0) DEFAULT 0,

    -- 상품 정보
    brand VARCHAR(100),
    manufacturer VARCHAR(100),
    origin_country VARCHAR(50) DEFAULT '대한민국',

    -- 상태
    status product_status NOT NULL DEFAULT 'DRAFT',
    is_featured BOOLEAN NOT NULL DEFAULT FALSE,
    has_variants BOOLEAN NOT NULL DEFAULT FALSE,

    -- 판매 통계
    sales_count INT NOT NULL DEFAULT 0,
    view_count INT NOT NULL DEFAULT 0,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

-- Product Variants (상품 변형 - 색상/사이즈 등)
CREATE TABLE product_variants (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id UUID NOT NULL REFERENCES products(id),

    -- 변형 정보
    name VARCHAR(100) NOT NULL,
    sku VARCHAR(50),
    barcode VARCHAR(50),

    -- 가격 (변형별)
    price DECIMAL(12, 0) NOT NULL,
    compare_at_price DECIMAL(12, 0),

    -- 재고 (변형별)
    stock_quantity INT NOT NULL DEFAULT 0,

    -- 배송 정보 (변형별 다를 경우)
    weight_g INT,

    -- 옵션 값 (JSON)
    option_values JSONB NOT NULL DEFAULT '{}',

    display_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Product Images (상품 이미지)
CREATE TABLE product_images (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id UUID NOT NULL REFERENCES products(id),
    variant_id UUID REFERENCES product_variants(id),

    image_url VARCHAR(500) NOT NULL,
    alt_text VARCHAR(200),
    display_order SMALLINT NOT NULL DEFAULT 0,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Product Specifications (상품 스펙)
CREATE TABLE product_specifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id UUID NOT NULL REFERENCES products(id),

    spec_name VARCHAR(50) NOT NULL,
    spec_value VARCHAR(200) NOT NULL,
    display_order SMALLINT NOT NULL DEFAULT 0,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- 4. PROMOTION DOMAIN (Orders depend on coupons)
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
    seller_id UUID REFERENCES sellers(id),
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
-- 5. ORDER DOMAIN
-- =============================================================================

-- Orders (주문)
CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_number VARCHAR(20) NOT NULL UNIQUE,
    customer_id UUID NOT NULL REFERENCES customers(id),
    seller_id UUID NOT NULL REFERENCES sellers(id),

    -- 배송지 정보
    shipping_address_id UUID REFERENCES customer_addresses(id),
    shipping_address_snapshot JSONB NOT NULL,

    -- 상태
    status order_status NOT NULL DEFAULT 'PENDING',

    -- 금액
    subtotal_amount DECIMAL(12, 0) NOT NULL,
    shipping_fee DECIMAL(10, 0) NOT NULL DEFAULT 0,
    discount_amount DECIMAL(10, 0) NOT NULL DEFAULT 0,
    total_amount DECIMAL(12, 0) NOT NULL,

    -- 쿠폰
    coupon_id UUID REFERENCES coupons(id),

    -- 메모
    order_memo TEXT,
    shipping_memo TEXT,

    -- 타임스탬프
    ordered_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at TIMESTAMP,
    confirmed_at TIMESTAMP,
    shipped_at TIMESTAMP,
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
    product_id UUID NOT NULL REFERENCES products(id),
    variant_id UUID REFERENCES product_variants(id),

    -- 스냅샷
    product_name_snapshot VARCHAR(200) NOT NULL,
    variant_name_snapshot VARCHAR(100),
    sku_snapshot VARCHAR(50),
    option_values_snapshot JSONB,

    -- 수량 및 가격
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10, 0) NOT NULL,
    total_price DECIMAL(12, 0) NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Order Status Histories (주문 상태 이력)
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
-- 6. SHIPMENT DOMAIN
-- =============================================================================

-- Shipping Carriers (배송업체 마스터)
CREATE TABLE shipping_carriers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    tracking_url_template VARCHAR(255),
    api_endpoint VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Shipments (배송)
CREATE TABLE shipments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id UUID NOT NULL REFERENCES orders(id),

    -- 배송업체
    carrier shipping_carrier NOT NULL,
    carrier_id UUID REFERENCES shipping_carriers(id),
    tracking_number VARCHAR(50),

    -- 상태
    status shipment_status NOT NULL DEFAULT 'PENDING',

    -- 주소 스냅샷
    origin_address_snapshot JSONB NOT NULL,
    destination_address_snapshot JSONB NOT NULL,

    -- 배송 정보
    total_weight_g INT,
    package_count SMALLINT NOT NULL DEFAULT 1,

    -- 타임스탬프
    ready_at TIMESTAMP,
    picked_up_at TIMESTAMP,
    delivered_at TIMESTAMP,
    delivery_photo_url VARCHAR(255),

    -- 배송 실패
    failure_reason TEXT,
    failed_at TIMESTAMP,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Shipment Tracking (배송 추적)
CREATE TABLE shipment_tracking (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    shipment_id UUID NOT NULL REFERENCES shipments(id),

    -- 추적 정보
    status VARCHAR(50) NOT NULL,
    location VARCHAR(100),
    description TEXT,

    -- 택배사 제공 시간
    occurred_at TIMESTAMP NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- 7. PAYMENT DOMAIN
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

    -- 가상계좌
    virtual_account_bank VARCHAR(50),
    virtual_account_number VARCHAR(50),
    virtual_account_holder VARCHAR(50),
    virtual_account_expires_at TIMESTAMP,

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
    return_id UUID,
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
-- 8. RETURN DOMAIN (NEW)
-- =============================================================================

-- Returns (반품/교환)
CREATE TABLE returns (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    return_number VARCHAR(20) NOT NULL UNIQUE,
    order_id UUID NOT NULL REFERENCES orders(id),
    customer_id UUID NOT NULL REFERENCES customers(id),
    seller_id UUID NOT NULL REFERENCES sellers(id),

    -- 반품 유형
    return_type return_type NOT NULL,
    reason return_reason NOT NULL,
    reason_detail TEXT,

    -- 상태
    status return_status NOT NULL DEFAULT 'REQUESTED',

    -- 수거 정보
    pickup_address_snapshot JSONB NOT NULL,
    pickup_carrier shipping_carrier,
    pickup_tracking_number VARCHAR(50),

    -- 교환 배송 정보 (교환인 경우)
    exchange_shipping_address_snapshot JSONB,
    exchange_carrier shipping_carrier,
    exchange_tracking_number VARCHAR(50),

    -- 환불 금액
    refund_amount DECIMAL(12, 0),
    refund_shipping_fee DECIMAL(10, 0) DEFAULT 0,

    -- 타임스탬프
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    approved_at TIMESTAMP,
    rejected_at TIMESTAMP,
    reject_reason TEXT,
    collected_at TIMESTAMP,
    inspected_at TIMESTAMP,
    completed_at TIMESTAMP,
    cancelled_at TIMESTAMP,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Return Items (반품 항목)
CREATE TABLE return_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    return_id UUID NOT NULL REFERENCES returns(id),
    order_item_id UUID NOT NULL REFERENCES order_items(id),

    quantity INT NOT NULL CHECK (quantity > 0),
    reason return_reason NOT NULL,
    reason_detail TEXT,

    -- 검수 결과
    inspection_result VARCHAR(50),
    inspection_note TEXT,

    -- 교환 상품 (교환인 경우)
    exchange_product_id UUID REFERENCES products(id),
    exchange_variant_id UUID REFERENCES product_variants(id),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Add FK from refunds to returns
ALTER TABLE refunds ADD CONSTRAINT refunds_return_id_fkey
    FOREIGN KEY (return_id) REFERENCES returns(id);

-- =============================================================================
-- 9. REVIEW DOMAIN
-- =============================================================================

-- Reviews (리뷰)
CREATE TABLE reviews (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id UUID NOT NULL UNIQUE REFERENCES orders(id),
    customer_id UUID NOT NULL REFERENCES customers(id),
    seller_id UUID NOT NULL REFERENCES sellers(id),
    product_id UUID NOT NULL REFERENCES products(id),
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

-- Review Replies (판매자 답글)
CREATE TABLE review_replies (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    review_id UUID NOT NULL UNIQUE REFERENCES reviews(id),
    seller_id UUID NOT NULL REFERENCES sellers(id),
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

-- Seller indexes
CREATE INDEX idx_sellers_category ON sellers(category_id, status) WHERE deleted_at IS NULL;
CREATE INDEX idx_sellers_owner ON sellers(owner_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_sellers_rating ON sellers(rating_avg DESC) WHERE status = 'ACTIVE' AND deleted_at IS NULL;
CREATE INDEX idx_sellers_business_number ON sellers(business_number) WHERE business_number IS NOT NULL;

-- Product indexes
CREATE INDEX idx_products_seller ON products(seller_id, status) WHERE deleted_at IS NULL;
CREATE INDEX idx_products_category ON products(category_id, status) WHERE deleted_at IS NULL;
CREATE INDEX idx_products_status ON products(status, created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX idx_products_sku ON products(sku) WHERE sku IS NOT NULL AND deleted_at IS NULL;
CREATE INDEX idx_products_featured ON products(is_featured, sales_count DESC)
    WHERE status = 'ACTIVE' AND deleted_at IS NULL;
CREATE INDEX idx_product_variants_product ON product_variants(product_id) WHERE is_active = TRUE;
CREATE INDEX idx_product_images_product ON product_images(product_id, display_order);
CREATE INDEX idx_product_categories_parent ON product_categories(parent_id);
CREATE INDEX idx_product_categories_path ON product_categories(path);

-- Order indexes
CREATE INDEX idx_orders_customer ON orders(customer_id, created_at DESC);
CREATE INDEX idx_orders_seller_status ON orders(seller_id, status, created_at DESC);
CREATE INDEX idx_orders_status ON orders(status, created_at DESC);
CREATE INDEX idx_orders_number ON orders(order_number);
CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_order_items_product ON order_items(product_id);
CREATE INDEX idx_order_status_histories_order ON order_status_histories(order_id, created_at DESC);

-- Shipment indexes
CREATE INDEX idx_shipments_order ON shipments(order_id);
CREATE INDEX idx_shipments_status ON shipments(status, created_at DESC);
CREATE INDEX idx_shipments_tracking ON shipments(carrier, tracking_number)
    WHERE tracking_number IS NOT NULL;
CREATE INDEX idx_shipment_tracking_shipment ON shipment_tracking(shipment_id, occurred_at DESC);

-- Payment indexes
CREATE INDEX idx_payments_order ON payments(order_id);
CREATE INDEX idx_payments_status ON payments(status, created_at DESC);
CREATE INDEX idx_refunds_payment ON refunds(payment_id);
CREATE INDEX idx_refunds_order ON refunds(order_id);
CREATE INDEX idx_refunds_return ON refunds(return_id) WHERE return_id IS NOT NULL;
CREATE INDEX idx_payment_methods_customer ON payment_methods(customer_id) WHERE deleted_at IS NULL;

-- Coupon indexes
CREATE INDEX idx_coupons_code ON coupons(code) WHERE is_active = TRUE;
CREATE INDEX idx_coupons_valid ON coupons(valid_until, is_active) WHERE is_active = TRUE;
CREATE INDEX idx_customer_coupons_customer ON customer_coupons(customer_id, status);
CREATE INDEX idx_customer_coupons_expiry ON customer_coupons(expires_at) WHERE status = 'AVAILABLE';

-- Return indexes
CREATE INDEX idx_returns_order ON returns(order_id);
CREATE INDEX idx_returns_customer ON returns(customer_id, created_at DESC);
CREATE INDEX idx_returns_seller ON returns(seller_id, status, created_at DESC);
CREATE INDEX idx_returns_status ON returns(status, created_at DESC);
CREATE INDEX idx_returns_number ON returns(return_number);
CREATE INDEX idx_return_items_return ON return_items(return_id);

-- Review indexes
CREATE INDEX idx_reviews_seller ON reviews(seller_id, is_visible, created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX idx_reviews_product ON reviews(product_id, is_visible, created_at DESC) WHERE deleted_at IS NULL;
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
            DROP TRIGGER IF EXISTS update_%I_updated_at ON %I;
            CREATE TRIGGER update_%I_updated_at
            BEFORE UPDATE ON %I
            FOR EACH ROW
            EXECUTE FUNCTION update_updated_at_column()
        ', t, t, t, t);
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

-- Function to generate return number
CREATE OR REPLACE FUNCTION generate_return_number()
RETURNS TRIGGER AS $$
DECLARE
    date_part VARCHAR(8);
    seq_num INT;
BEGIN
    date_part := TO_CHAR(CURRENT_DATE, 'YYYYMMDD');

    SELECT COALESCE(MAX(CAST(SUBSTRING(return_number FROM 13) AS INT)), 0) + 1
    INTO seq_num
    FROM returns
    WHERE return_number LIKE 'RET-' || date_part || '-%';

    NEW.return_number := 'RET-' || date_part || '-' || LPAD(seq_num::TEXT, 5, '0');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER generate_return_number_trigger
BEFORE INSERT ON returns
FOR EACH ROW
WHEN (NEW.return_number IS NULL OR NEW.return_number = '')
EXECUTE FUNCTION generate_return_number();

-- Function to update seller rating
CREATE OR REPLACE FUNCTION update_seller_rating()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE sellers
    SET rating_avg = (
        SELECT COALESCE(AVG(rating), 0)
        FROM reviews
        WHERE seller_id = COALESCE(NEW.seller_id, OLD.seller_id)
        AND is_visible = TRUE
        AND deleted_at IS NULL
    ),
    review_count = (
        SELECT COUNT(*)
        FROM reviews
        WHERE seller_id = COALESCE(NEW.seller_id, OLD.seller_id)
        AND is_visible = TRUE
        AND deleted_at IS NULL
    )
    WHERE id = COALESCE(NEW.seller_id, OLD.seller_id);

    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_seller_rating_on_review
AFTER INSERT OR UPDATE OR DELETE ON reviews
FOR EACH ROW
EXECUTE FUNCTION update_seller_rating();

-- Function to update product sales count
CREATE OR REPLACE FUNCTION update_product_sales_count()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE products
        SET sales_count = sales_count + NEW.quantity
        WHERE id = NEW.product_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_product_sales_on_order
AFTER INSERT ON order_items
FOR EACH ROW
EXECUTE FUNCTION update_product_sales_count();

-- =============================================================================
-- COMMENTS
-- =============================================================================

COMMENT ON TABLE customers IS '고객 정보';
COMMENT ON TABLE customer_addresses IS '고객 배송지 주소';
COMMENT ON TABLE seller_categories IS '판매자 분류';
COMMENT ON TABLE sellers IS '판매자 정보';
COMMENT ON TABLE seller_addresses IS '판매자 주소 (사업장/창고/반품지)';
COMMENT ON TABLE product_categories IS '상품 카테고리 (계층형)';
COMMENT ON TABLE products IS '상품';
COMMENT ON TABLE product_variants IS '상품 변형 (색상/사이즈)';
COMMENT ON TABLE product_images IS '상품 이미지';
COMMENT ON TABLE product_specifications IS '상품 스펙';
COMMENT ON TABLE orders IS '주문';
COMMENT ON TABLE order_items IS '주문 항목';
COMMENT ON TABLE order_status_histories IS '주문 상태 변경 이력';
COMMENT ON TABLE shipments IS '배송 정보';
COMMENT ON TABLE shipment_tracking IS '배송 추적';
COMMENT ON TABLE shipping_carriers IS '배송업체 마스터';
COMMENT ON TABLE payments IS '결제 정보';
COMMENT ON TABLE refunds IS '환불 정보';
COMMENT ON TABLE payment_methods IS '저장된 결제수단';
COMMENT ON TABLE coupons IS '쿠폰';
COMMENT ON TABLE customer_coupons IS '고객 보유 쿠폰';
COMMENT ON TABLE promotions IS '프로모션/이벤트';
COMMENT ON TABLE returns IS '반품/교환';
COMMENT ON TABLE return_items IS '반품 항목';
COMMENT ON TABLE reviews IS '리뷰';
COMMENT ON TABLE review_images IS '리뷰 이미지';
COMMENT ON TABLE review_replies IS '판매자 답글';

-- =============================================================================
-- INITIAL DATA
-- =============================================================================

-- Insert shipping carriers
INSERT INTO shipping_carriers (id, code, name, tracking_url_template, display_order) VALUES
    (uuid_generate_v4(), 'CJ', 'CJ대한통운', 'https://www.cjlogistics.com/ko/tool/parcel/tracking?gnbInvcNo={tracking_number}', 1),
    (uuid_generate_v4(), 'HANJIN', '한진택배', 'https://www.hanjin.com/kor/CMS/DeliveryMgr/WaybillResult.do?mession-Y&wblnumText2={tracking_number}', 2),
    (uuid_generate_v4(), 'LOTTE', '롯데택배', 'https://www.lotteglogis.com/home/reservation/tracking/index?InvNo={tracking_number}', 3),
    (uuid_generate_v4(), 'LOGEN', '로젠택배', 'https://www.ilogen.com/web/personal/trace/{tracking_number}', 4),
    (uuid_generate_v4(), 'POST', '우체국택배', 'https://service.epost.go.kr/trace.RetrieveDomRi498.postal?sid1={tracking_number}', 5),
    (uuid_generate_v4(), 'EPOST', '우편등기', 'https://service.epost.go.kr/trace.RetrieveDomRi498.postal?sid1={tracking_number}', 6);

-- Insert sample product categories
INSERT INTO product_categories (id, parent_id, name, depth, path, display_order) VALUES
    ('11111111-1111-1111-1111-111111111111', NULL, '패션의류', 1, '/1/', 1),
    ('22222222-2222-2222-2222-222222222222', NULL, '디지털/가전', 1, '/2/', 2),
    ('33333333-3333-3333-3333-333333333333', NULL, '생활/건강', 1, '/3/', 3),
    ('44444444-4444-4444-4444-444444444444', NULL, '식품', 1, '/4/', 4),
    ('11111111-1111-1111-1111-111111111112', '11111111-1111-1111-1111-111111111111', '남성의류', 2, '/1/1/', 1),
    ('11111111-1111-1111-1111-111111111113', '11111111-1111-1111-1111-111111111111', '여성의류', 2, '/1/2/', 2),
    ('22222222-2222-2222-2222-222222222223', '22222222-2222-2222-2222-222222222222', '컴퓨터', 2, '/2/1/', 1),
    ('22222222-2222-2222-2222-222222222224', '22222222-2222-2222-2222-222222222222', '모바일', 2, '/2/2/', 2);

-- Insert sample seller categories
INSERT INTO seller_categories (id, name, display_order) VALUES
    (uuid_generate_v4(), '패션/의류', 1),
    (uuid_generate_v4(), '디지털/가전', 2),
    (uuid_generate_v4(), '생활용품', 3),
    (uuid_generate_v4(), '식품/건강', 4),
    (uuid_generate_v4(), '뷰티/화장품', 5);

-- =============================================================================
-- END OF SCHEMA
-- =============================================================================
