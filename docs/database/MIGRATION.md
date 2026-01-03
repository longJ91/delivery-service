# Schema Migration Plan: 음식 배달 → 물품 배송

## Overview

| 항목 | 기존 | 변경 |
|------|------|------|
| **서비스 유형** | 음식 배달 (Food Delivery) | 물품 배송 (Product Delivery) |
| **핵심 도메인** | Menu (메뉴) | Product (상품) |
| **판매자** | Shop (가게) | Seller (판매자) |
| **배송 모델** | 라이더 즉시 배달 | 택배/배송 |
| **테이블 수** | 28개 | 29개 |

---

## Impact Analysis

### 도메인별 영향도

```
┌─────────────────────────────────────────────────────────────────────┐
│                        영향도 매트릭스                                │
├─────────────────────────────────────────────────────────────────────┤
│  Domain          │ 변경 수준  │ 테이블 수 │ 주요 변경 사항            │
├──────────────────┼───────────┼──────────┼─────────────────────────┤
│  User            │ ⚪ 낮음   │ 3 → 2    │ riders 제거/변경         │
│  Shop → Seller   │ 🟡 중간   │ 4 → 3    │ 명칭 변경, 영업시간 제거   │
│  Menu → Product  │ 🔴 높음   │ 4 → 6    │ 전면 재설계              │
│  Order           │ 🟡 중간   │ 4 → 4    │ 상태/필드 변경           │
│  Delivery → Ship │ 🟡 중간   │ 3 → 4    │ 택배 모델로 전환          │
│  Payment         │ ⚪ 낮음   │ 3 → 3    │ 변경 없음                │
│  Promotion       │ ⚪ 낮음   │ 3 → 3    │ 변경 없음                │
│  Review          │ ⚪ 낮음   │ 3 → 3    │ FK 참조 변경             │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Phase 1: Enum 타입 재정의

### 1.1 Order Status 변경

```sql
-- 기존 (음식 배달)
CREATE TYPE order_status AS ENUM (
    'PENDING',           -- 주문 대기
    'ACCEPTED',          -- 주문 접수
    'PREPARING',         -- 조리중
    'READY_FOR_DELIVERY',-- 배달 준비 완료
    'PICKED_UP',         -- 픽업 완료
    'DELIVERED',         -- 배달 완료
    'CANCELLED'          -- 취소
);

-- 변경 (물품 배송)
CREATE TYPE order_status AS ENUM (
    'PENDING',           -- 주문 대기 (결제 전)
    'PAID',              -- 결제 완료
    'CONFIRMED',         -- 주문 확정 (판매자 확인)
    'PREPARING',         -- 상품 준비중
    'SHIPPED',           -- 출고 완료
    'IN_TRANSIT',        -- 배송중
    'OUT_FOR_DELIVERY',  -- 배달중 (최종 배송)
    'DELIVERED',         -- 배송 완료
    'CANCELLED',         -- 취소
    'RETURN_REQUESTED',  -- 반품 요청
    'RETURNED'           -- 반품 완료
);
```

### 1.2 Shipment Status (신규)

```sql
-- 기존 delivery_status 대체
CREATE TYPE shipment_status AS ENUM (
    'PENDING',           -- 배송 준비 대기
    'READY_TO_SHIP',     -- 출고 준비 완료
    'PICKED_UP',         -- 택배사 수거 완료
    'IN_TRANSIT',        -- 배송중 (허브 이동)
    'OUT_FOR_DELIVERY',  -- 배달중 (최종 배송)
    'DELIVERED',         -- 배송 완료
    'FAILED',            -- 배송 실패
    'RETURNED'           -- 반송
);
```

### 1.3 Shipping Carrier (신규)

```sql
CREATE TYPE shipping_carrier AS ENUM (
    'CJ_LOGISTICS',      -- CJ대한통운
    'HANJIN',            -- 한진택배
    'LOTTE',             -- 롯데택배
    'LOGEN',             -- 로젠택배
    'POST_OFFICE',       -- 우체국택배
    'COUPANG',           -- 쿠팡 로켓배송
    'SELF_DELIVERY',     -- 직접 배송
    'OTHER'              -- 기타
);
```

---

## Phase 2: Shop → Seller 도메인 변경

### 2.1 테이블 매핑

| 기존 | 변경 | 비고 |
|------|------|------|
| `shop_categories` | `seller_categories` | 명칭 변경 |
| `shops` | `sellers` | 필드 변경 포함 |
| `shop_addresses` | `seller_addresses` | 창고 주소 개념 추가 |
| `shop_business_hours` | **제거** | 물품 배송에 불필요 |

### 2.2 sellers 테이블 변경사항

```sql
-- 제거 필드
-- min_order_amount      -- 최소주문금액 (상품별로 이동)
-- delivery_fee          -- 배달비 (배송비 정책 테이블로 분리)
-- estimated_delivery_time -- 예상 배달시간 (상품별/배송지별)

-- 추가 필드
seller_type             -- INDIVIDUAL, BUSINESS (개인/사업자)
company_name            -- 상호명
representative_name     -- 대표자명
return_address_id       -- 반품 주소 FK
cs_phone               -- 고객센터 전화번호
cs_email               -- 고객센터 이메일
```

### 2.3 seller_addresses 확장

```sql
-- 추가 필드
address_type    -- BUSINESS, WAREHOUSE, RETURN (사업장/창고/반품)
is_primary      -- 대표 주소 여부
```

---

## Phase 3: Menu → Product 도메인 변경

### 3.1 테이블 매핑

| 기존 | 변경 | 비고 |
|------|------|------|
| `menu_categories` | `product_categories` | 계층 구조 추가 |
| `menus` | `products` | 전면 재설계 |
| `menu_option_groups` | `product_variants` | 색상/사이즈 변형 |
| `menu_options` | `product_variant_options` | 변형별 가격/재고 |
| - | `product_images` | **신규** |
| - | `product_specifications` | **신규** |

### 3.2 product_categories (계층형)

```sql
CREATE TABLE product_categories (
    id UUID PRIMARY KEY,
    parent_id UUID REFERENCES product_categories(id),  -- 상위 카테고리
    name VARCHAR(50) NOT NULL,
    depth SMALLINT NOT NULL DEFAULT 1,                  -- 깊이 (1=대, 2=중, 3=소)
    path VARCHAR(255),                                  -- Materialized Path ("/1/5/23/")
    display_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### 3.3 products (상품)

```sql
CREATE TABLE products (
    id UUID PRIMARY KEY,
    seller_id UUID NOT NULL REFERENCES sellers(id),
    category_id UUID REFERENCES product_categories(id),

    -- 기본 정보
    name VARCHAR(200) NOT NULL,
    description TEXT,

    -- 가격 (변형이 없는 단일 상품용)
    price DECIMAL(12, 0) NOT NULL,
    compare_at_price DECIMAL(12, 0),           -- 할인 전 가격 (취소선 표시용)

    -- 상품 식별
    sku VARCHAR(50),                            -- Stock Keeping Unit
    barcode VARCHAR(50),                        -- 바코드/UPC

    -- 재고 (변형이 없는 단일 상품용)
    stock_quantity INT NOT NULL DEFAULT 0,
    safety_stock INT NOT NULL DEFAULT 0,        -- 안전 재고
    is_track_inventory BOOLEAN DEFAULT TRUE,    -- 재고 추적 여부

    -- 배송 정보
    weight_g INT,                               -- 무게 (gram)
    width_mm INT,                               -- 가로 (mm)
    height_mm INT,                              -- 세로 (mm)
    depth_mm INT,                               -- 높이 (mm)

    -- 상품 정보
    brand VARCHAR(100),
    manufacturer VARCHAR(100),
    origin_country VARCHAR(50),                 -- 원산지

    -- 상태
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT', -- DRAFT, ACTIVE, INACTIVE, ARCHIVED
    is_featured BOOLEAN NOT NULL DEFAULT FALSE,  -- 추천 상품
    has_variants BOOLEAN NOT NULL DEFAULT FALSE, -- 변형 상품 여부

    -- 메타
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);
```

### 3.4 product_variants (상품 변형)

```sql
-- 색상, 사이즈 등 변형 상품 관리
CREATE TABLE product_variants (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL REFERENCES products(id),

    -- 변형 정보
    name VARCHAR(100) NOT NULL,                 -- "블랙 / L"
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
    option_values JSONB NOT NULL,               -- {"color": "블랙", "size": "L"}

    display_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### 3.5 product_images (상품 이미지)

```sql
CREATE TABLE product_images (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL REFERENCES products(id),
    variant_id UUID REFERENCES product_variants(id),  -- 변형 전용 이미지

    image_url VARCHAR(500) NOT NULL,
    alt_text VARCHAR(200),
    display_order SMALLINT NOT NULL DEFAULT 0,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### 3.6 product_specifications (상품 스펙)

```sql
CREATE TABLE product_specifications (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL REFERENCES products(id),

    spec_name VARCHAR(50) NOT NULL,             -- "재질", "용량", "인증"
    spec_value VARCHAR(200) NOT NULL,           -- "스테인리스", "500ml", "KC인증"
    display_order SMALLINT NOT NULL DEFAULT 0,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

---

## Phase 4: Order 도메인 필드 변경

### 4.1 orders 테이블 변경

```sql
-- 컬럼 변경
ALTER TABLE orders RENAME COLUMN order_request TO order_memo;
ALTER TABLE orders RENAME COLUMN delivery_request TO shipping_memo;
ALTER TABLE orders RENAME COLUMN prepared_at TO shipped_at;

-- 컬럼 추가
ALTER TABLE orders ADD COLUMN paid_at TIMESTAMP;
ALTER TABLE orders ADD COLUMN confirmed_at TIMESTAMP;
```

### 4.2 order_items 테이블 변경

```sql
-- 컬럼 변경
ALTER TABLE order_items RENAME COLUMN menu_id TO product_id;
ALTER TABLE order_items RENAME COLUMN menu_name_snapshot TO product_name_snapshot;

-- 컬럼 추가
ALTER TABLE order_items ADD COLUMN variant_id UUID REFERENCES product_variants(id);
ALTER TABLE order_items ADD COLUMN variant_name_snapshot VARCHAR(100);
ALTER TABLE order_items ADD COLUMN sku_snapshot VARCHAR(50);
```

---

## Phase 5: Delivery → Shipment 도메인 변경

### 5.1 테이블 매핑

| 기존 | 변경 | 비고 |
|------|------|------|
| `deliveries` | `shipments` | 택배 배송 모델 |
| `delivery_tracking` | `shipment_tracking` | 택배 추적 |
| `rider_assignments` | **제거** | 택배사 연동으로 대체 |
| `riders` | `couriers` 또는 **제거** | 선택적 |
| - | `shipping_carriers` | **신규** 배송업체 마스터 |

### 5.2 shipments (배송)

```sql
CREATE TABLE shipments (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES orders(id),

    -- 배송업체
    carrier shipping_carrier NOT NULL,
    carrier_name VARCHAR(50),                   -- 기타 배송업체명
    tracking_number VARCHAR(50),                -- 운송장 번호

    -- 상태
    status shipment_status NOT NULL DEFAULT 'PENDING',

    -- 주소 스냅샷
    origin_address_snapshot JSONB NOT NULL,     -- 출고지 (판매자/창고)
    destination_address_snapshot JSONB NOT NULL, -- 배송지

    -- 배송 정보
    total_weight_g INT,                         -- 총 무게
    package_count SMALLINT NOT NULL DEFAULT 1,  -- 박스 수
    shipping_fee DECIMAL(10, 0),                -- 배송비

    -- 타임스탬프
    shipped_at TIMESTAMP,                       -- 출고 완료
    delivered_at TIMESTAMP,                     -- 배송 완료
    delivery_photo_url VARCHAR(255),

    -- 배송 실패/반송
    failure_reason TEXT,
    return_tracking_number VARCHAR(50),         -- 반송 운송장

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### 5.3 shipment_tracking (배송 추적)

```sql
CREATE TABLE shipment_tracking (
    id UUID PRIMARY KEY,
    shipment_id UUID NOT NULL REFERENCES shipments(id),

    -- 추적 정보
    status VARCHAR(50) NOT NULL,                -- 배송 상태
    location VARCHAR(100),                      -- 위치 (○○허브, ○○터미널)
    description TEXT,                           -- 상세 내용

    -- 타임스탬프 (택배사 제공 시간)
    occurred_at TIMESTAMP NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### 5.4 shipping_carriers (배송업체 마스터)

```sql
CREATE TABLE shipping_carriers (
    id UUID PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,           -- 'CJ', 'HANJIN', 'LOTTE'
    name VARCHAR(50) NOT NULL,                  -- CJ대한통운
    tracking_url_template VARCHAR(255),         -- 추적 URL 템플릿
    api_endpoint VARCHAR(255),                  -- API 연동 URL
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

---

## Phase 6: Review 도메인 참조 변경

### 6.1 FK 변경

```sql
-- reviews
ALTER TABLE reviews RENAME COLUMN shop_id TO seller_id;
ALTER TABLE reviews DROP CONSTRAINT reviews_shop_id_fkey;
ALTER TABLE reviews ADD CONSTRAINT reviews_seller_id_fkey
    FOREIGN KEY (seller_id) REFERENCES sellers(id);

-- review_replies
ALTER TABLE review_replies RENAME COLUMN shop_id TO seller_id;
ALTER TABLE review_replies DROP CONSTRAINT review_replies_shop_id_fkey;
ALTER TABLE review_replies ADD CONSTRAINT review_replies_seller_id_fkey
    FOREIGN KEY (seller_id) REFERENCES sellers(id);
```

---

## Migration Summary

### 테이블 변경 요약

| 작업 | 테이블 | 비고 |
|------|--------|------|
| **RENAME** | shops → sellers | |
| **RENAME** | shop_categories → seller_categories | |
| **RENAME** | shop_addresses → seller_addresses | |
| **RENAME** | menu_categories → product_categories | 구조 변경 포함 |
| **RENAME** | menus → products | 전면 재설계 |
| **RENAME** | menu_option_groups → product_variants | 구조 변경 |
| **RENAME** | menu_options → product_variant_options | 구조 변경 |
| **RENAME** | deliveries → shipments | 구조 변경 |
| **RENAME** | delivery_tracking → shipment_tracking | 구조 변경 |
| **DELETE** | shop_business_hours | 불필요 |
| **DELETE** | riders | 택배사 연동으로 대체 |
| **DELETE** | rider_assignments | 불필요 |
| **CREATE** | product_images | 신규 |
| **CREATE** | product_specifications | 신규 |
| **CREATE** | shipping_carriers | 신규 |

### 최종 테이블 수

```
기존: 28개
  - 삭제: 3개 (shop_business_hours, riders, rider_assignments)
  + 추가: 3개 (product_images, product_specifications, shipping_carriers)
변경: 28개
```

---

## Execution Plan

### Step 1: 신규 스키마 파일 생성
`docs/database/schema.sql` 생성 (물품 배송 버전)

### Step 2: 마이그레이션 스크립트 작성 (선택)
기존 데이터가 있는 경우 마이그레이션 SQL 작성

### Step 3: 애플리케이션 코드 변경
- Entity 클래스 변경
- Repository 변경
- Service/UseCase 변경
- API 스펙 변경

---

## Questions for Clarification

1. **라이더 직접 배송 지원 여부**: 택배사만 사용? 또는 자체 배송팀도 있음?
2. **창고 관리**: 판매자가 여러 창고를 가질 수 있는지?
3. **해외 배송**: 국제 배송 지원 필요 여부?
4. **반품/교환**: 반품/교환 프로세스 상세 필요?
