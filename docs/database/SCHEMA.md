# Database Schema Documentation

## Overview

물품 배송 서비스를 위한 PostgreSQL 데이터베이스 스키마 설계 문서입니다.

- **Database**: PostgreSQL 15+
- **Total Tables**: 30개
- **Version**: 2.0.0
- **Type**: Product Delivery Service (물품 배송)

## Schema Files

| File | Version | Description |
|------|---------|-------------|
| `schema.sql` | 2.0.0 | **현재** - 물품 배송 서비스 |

## Service Constraints

| 항목 | 설정 |
|------|------|
| 배송 방식 | 택배사 연동만 지원 |
| 창고 | 판매자당 단일 창고 |
| 배송 지역 | 국내 배송만 |
| 반품/교환 | 지원 |

## Entity Relationship Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              USER DOMAIN                                     │
├─────────────────────────────────────────────────────────────────────────────┤
│  ┌──────────────┐     ┌────────────────────┐                                │
│  │  customers   │────<│ customer_addresses │                                │
│  └──────────────┘     └────────────────────┘                                │
└───────────────────────────────────────────────────────────────────────────────┘
                                │
┌───────────────────────────────┼───────────────────────────────────────────────┐
│                          SELLER DOMAIN                                        │
├───────────────────────────────┼───────────────────────────────────────────────┤
│  ┌──────────────────┐   ┌─────┴─────┐   ┌──────────────────┐                 │
│  │ seller_categories│──>│  sellers  │──<│ seller_addresses │                 │
│  └──────────────────┘   └───────────┘   └──────────────────┘                 │
└───────────────────────────────┼───────────────────────────────────────────────┘
                                │
┌───────────────────────────────┼───────────────────────────────────────────────┐
│                         PRODUCT DOMAIN                                        │
├───────────────────────────────┼───────────────────────────────────────────────┤
│  ┌───────────────────┐  ┌─────┴─────┐  ┌──────────────────┐                  │
│  │ product_categories│─>│ products  │─<│ product_variants │                  │
│  │    (계층형)       │  └───────────┘  └──────────────────┘                  │
│  └───────────────────┘       │ │                                              │
│                              │ ├────<│ product_images │                       │
│                              │ └────<│ product_specifications │               │
└───────────────────────────────────────────────────────────────────────────────┘
                                │
┌───────────────────────────────┼───────────────────────────────────────────────┐
│                          ORDER DOMAIN                                         │
├───────────────────────────────┼───────────────────────────────────────────────┤
│  ┌──────────┐   ┌─────────────┴───┐   ┌────────────────────────┐             │
│  │ coupons  │──>│     orders      │──<│ order_status_histories │             │
│  └──────────┘   └─────────────────┘   └────────────────────────┘             │
│                         │                                                     │
│                  ┌──────┴──────┐                                              │
│                  │ order_items │                                              │
│                  └─────────────┘                                              │
└───────────────────────────────────────────────────────────────────────────────┘
                                │
┌───────────────────────────────┼───────────────────────────────────────────────┐
│                        SHIPMENT DOMAIN                                        │
├───────────────────────────────┼───────────────────────────────────────────────┤
│  ┌───────────────────┐  ┌─────┴──────┐  ┌───────────────────┐                │
│  │ shipping_carriers │─>│ shipments  │─<│ shipment_tracking │                │
│  └───────────────────┘  └────────────┘  └───────────────────┘                │
└───────────────────────────────────────────────────────────────────────────────┘
                                │
┌───────────────────────────────┼───────────────────────────────────────────────┐
│                         RETURN DOMAIN (NEW)                                   │
├───────────────────────────────┼───────────────────────────────────────────────┤
│                         ┌─────┴─────┐                                        │
│                         │  returns  │                                        │
│                         └───────────┘                                        │
│                              │                                                │
│                       ┌──────┴───────┐                                        │
│                       │ return_items │                                        │
│                       └──────────────┘                                        │
└───────────────────────────────────────────────────────────────────────────────┘
```

## Bounded Contexts

### 1. User Domain (2 tables)

| Table | Description | Key Features |
|-------|-------------|--------------|
| `customers` | 고객 정보 | 이메일 로그인, 포인트, 소프트 삭제 |
| `customer_addresses` | 고객 배송지 | 다중 주소, 기본 배송지 설정 |

### 2. Seller Domain (3 tables)

| Table | Description | Key Features |
|-------|-------------|--------------|
| `seller_categories` | 판매자 분류 | 업종별 분류 |
| `sellers` | 판매자 정보 | 개인/사업자, 평점, 고객센터 정보 |
| `seller_addresses` | 판매자 주소 | 사업장/출고지/반품지 (단일 창고) |

### 3. Product Domain (5 tables)

| Table | Description | Key Features |
|-------|-------------|--------------|
| `product_categories` | 상품 카테고리 | 계층형 (대/중/소분류) |
| `products` | 상품 | SKU, 재고, 배송비, 무게/크기 |
| `product_variants` | 상품 변형 | 색상/사이즈, 변형별 가격/재고 |
| `product_images` | 상품 이미지 | 다중 이미지, 대표 이미지 |
| `product_specifications` | 상품 스펙 | 상품 상세 정보 |

### 4. Order Domain (4 tables)

| Table | Description | Key Features |
|-------|-------------|--------------|
| `orders` | 주문 | 주문번호 자동생성, Optimistic Lock |
| `order_items` | 주문 항목 | 상품/변형 스냅샷 |
| `order_status_histories` | 상태 이력 | 감사 로그 |
| `customer_coupons` | 고객 보유 쿠폰 | 발급/사용 관리 |

### 5. Shipment Domain (3 tables)

| Table | Description | Key Features |
|-------|-------------|--------------|
| `shipping_carriers` | 배송업체 마스터 | 택배사 정보, 추적 URL |
| `shipments` | 배송 | 운송장, 택배사, 배송 상태 |
| `shipment_tracking` | 배송 추적 | 택배 추적 이력 |

### 6. Payment Domain (3 tables)

| Table | Description | Key Features |
|-------|-------------|--------------|
| `payments` | 결제 정보 | PG 연동, 가상계좌 지원 |
| `refunds` | 환불 정보 | 전체/부분 환불, 반품 연동 |
| `payment_methods` | 저장된 결제수단 | 빌링키 토큰화 |

### 7. Promotion Domain (3 tables)

| Table | Description | Key Features |
|-------|-------------|--------------|
| `coupons` | 쿠폰 | 정액/정률, 사용횟수 제한 |
| `customer_coupons` | 고객 쿠폰 | 개인별 만료일 |
| `promotions` | 프로모션 | JSON 조건/혜택 |

### 8. Return Domain (2 tables) - NEW

| Table | Description | Key Features |
|-------|-------------|--------------|
| `returns` | 반품/교환 | 반품번호, 수거/교환 배송 |
| `return_items` | 반품 항목 | 검수 결과, 교환 상품 |

### 9. Review Domain (3 tables)

| Table | Description | Key Features |
|-------|-------------|--------------|
| `reviews` | 리뷰 | 1-5점 평점, 상품별 리뷰 |
| `review_images` | 리뷰 이미지 | 다중 이미지 |
| `review_replies` | 판매자 답글 | 리뷰당 1개 |

### 10. Event Infrastructure (2 tables)

| Table | Description | Key Features |
|-------|-------------|--------------|
| `outbox_events` | Transactional Outbox | At-Least-Once 이벤트 발행 보장 |
| `processed_events` | Consumer Idempotency | 중복 이벤트 처리 방지 |

## Order Status Flow

```
┌─────────┐    ┌──────┐    ┌───────────┐    ┌───────────┐
│ PENDING │───>│ PAID │───>│ CONFIRMED │───>│ PREPARING │
└─────────┘    └──────┘    └───────────┘    └───────────┘
                                                  │
                                                  ▼
┌───────────┐    ┌──────────────────┐    ┌───────────┐
│ DELIVERED │<───│ OUT_FOR_DELIVERY │<───│ IN_TRANSIT│<───│ SHIPPED │
└───────────┘    └──────────────────┘    └───────────┘    └─────────┘
      │
      ▼
┌──────────────────┐    ┌──────────┐
│ RETURN_REQUESTED │───>│ RETURNED │
└──────────────────┘    └──────────┘

                    ┌───────────┐
       (any)  ─────>│ CANCELLED │
                    └───────────┘
```

## Return/Exchange Flow

```
┌───────────┐    ┌──────────┐    ┌────────────┐    ┌───────────┐
│ REQUESTED │───>│ APPROVED │───>│ COLLECTING │───>│ COLLECTED │
└───────────┘    └──────────┘    └────────────┘    └───────────┘
      │                                                  │
      ▼                                                  ▼
┌──────────┐                                      ┌────────────┐
│ REJECTED │                                      │ INSPECTING │
└──────────┘                                      └────────────┘
                                                       │
                                                       ▼
                                                 ┌───────────┐
                                                 │ COMPLETED │
                                                 └───────────┘
```

## Key Design Patterns

### 1. UUID Primary Key
```sql
id UUID PRIMARY KEY DEFAULT uuid_generate_v4()
```
- 분산 시스템 대비
- 예측 불가능한 ID로 보안 강화

### 2. Hierarchical Categories
```sql
CREATE TABLE product_categories (
    parent_id UUID REFERENCES product_categories(id),
    depth SMALLINT,
    path VARCHAR(255)  -- Materialized Path
);
```
- 대/중/소분류 계층 구조
- path로 빠른 조상/자손 조회

### 3. Product Variants
```sql
CREATE TABLE product_variants (
    option_values JSONB  -- {"color": "블랙", "size": "L"}
);
```
- 색상, 사이즈 등 변형 상품
- 변형별 독립적 가격/재고

### 4. Snapshot Pattern
```sql
product_name_snapshot VARCHAR(200)
shipping_address_snapshot JSONB
```
- 주문 시점 데이터 보존
- 원본 변경과 독립적

### 5. Soft Delete
```sql
deleted_at TIMESTAMP
```
- 데이터 복구 가능
- 감사 및 히스토리 보존

### 6. Optimistic Locking
```sql
version BIGINT NOT NULL DEFAULT 0
```
- 동시성 제어
- Lost Update 방지

## Enum Types Summary

| Type | Values |
|------|--------|
| `order_status` | PENDING, PAID, CONFIRMED, PREPARING, SHIPPED, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, CANCELLED, RETURN_REQUESTED, RETURNED |
| `shipment_status` | PENDING, READY_TO_SHIP, PICKED_UP, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, FAILED, RETURNED |
| `shipping_carrier` | CJ_LOGISTICS, HANJIN, LOTTE, LOGEN, POST_OFFICE, EPOST, GS_POSTBOX, CU_POST, OTHER |
| `return_status` | REQUESTED, APPROVED, REJECTED, COLLECTING, COLLECTED, INSPECTING, COMPLETED, CANCELLED |
| `return_reason` | CHANGE_OF_MIND, WRONG_ITEM, DEFECTIVE, DIFFERENT_FROM_DESC, DELAYED_DELIVERY, OTHER |
| `return_type` | RETURN, EXCHANGE |

## Triggers

### Auto-Generated Fields
- **주문번호**: `ORD-YYYYMMDD-NNNNN`
- **반품번호**: `RET-YYYYMMDD-NNNNN`
- **updated_at**: 모든 테이블 자동 갱신

### Business Logic Triggers
- **판매자 평점**: 리뷰 추가/수정/삭제 시 자동 계산
- **상품 판매량**: 주문 생성 시 자동 증가

## Initial Data

스키마 생성 시 자동으로 추가되는 데이터:

### 배송업체 (shipping_carriers)
- CJ대한통운, 한진택배, 롯데택배, 로젠택배, 우체국택배, 우편등기

### 상품 카테고리 (product_categories)
- 패션의류 (남성의류, 여성의류)
- 디지털/가전 (컴퓨터, 모바일)
- 생활/건강
- 식품

### 판매자 분류 (seller_categories)
- 패션/의류, 디지털/가전, 생활용품, 식품/건강, 뷰티/화장품

## Usage

### 스키마 적용
```bash
psql -U postgres -d delivery -f docs/database/schema.sql
```

### 스키마 초기화 (주의: 모든 데이터 삭제)
```bash
psql -U postgres -c "DROP DATABASE IF EXISTS delivery;"
psql -U postgres -c "CREATE DATABASE delivery;"
psql -U postgres -d delivery -f docs/database/schema.sql
```

## Migration from v1

음식 배달(v1) → 물품 배송(v2) 마이그레이션 계획은 `SCHEMA_MIGRATION_PLAN.md` 참조
