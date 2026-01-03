# Database Documentation

데이터베이스 스키마 설계 및 관련 문서입니다.

## Documents

| Document | Description | Version |
|----------|-------------|---------|
| [SCHEMA.md](./SCHEMA.md) | 스키마 설계 문서 | 2.0.0 |
| [schema.sql](./schema.sql) | PostgreSQL DDL (물품 배송) | 2.0.0 |
| [dummy_data.sql](./dummy_data.sql) | 더미 데이터 생성 스크립트 | 1.0.0 |
| [reset_data.sql](./reset_data.sql) | 데이터 초기화 스크립트 | 1.0.0 |
| [init_data.sh](./init_data.sh) | 데이터베이스 초기화 쉘 스크립트 | 1.0.0 |
| [MIGRATION.md](./MIGRATION.md) | 마이그레이션 계획 | - |
| [ENTITY_MIGRATION_PLAN.md](./ENTITY_MIGRATION_PLAN.md) | Entity 마이그레이션 계획 | - |

## Current Schema (v2.0.0)

물품 배송 서비스를 위한 스키마입니다.

### Service Constraints

| 항목 | 설정 |
|------|------|
| 서비스 유형 | 물품 배송 |
| 배송 방식 | 택배사 연동 |
| 창고 | 판매자당 단일 창고 |
| 배송 지역 | 국내 |
| 반품/교환 | 지원 |

### Domain Overview

```
┌─────────────────────────────────────────────────────┐
│                   12 Domains                        │
├─────────────────────────────────────────────────────┤
│  Customer     │ Seller        │ Product            │
│  Category     │ Cart          │ Order              │
│  Payment      │ Shipment      │ Returns            │
│  Review       │ Promotion     │ Webhook            │
├─────────────────────────────────────────────────────┤
│                  Total: 28 Tables                   │
└─────────────────────────────────────────────────────┘
```

## Persistence Strategy

### JPA + jOOQ 하이브리드 아키텍처

| 기술 | 역할 | Repository 패턴 |
|------|------|----------------|
| **JPA** | CRUD, Fetch Join, Entity 관계 로딩 | `{Entity}JpaRepository` |
| **jOOQ** | 통계, Projection, 컴파일 타임 타입 안전성 | `{Entity}JooqRepository` |

### JPA Entity Lombok 패턴

```java
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Entity
@Table(name = "orders")
public class OrderJpaEntity {
    @Id
    private String id;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItemJpaEntity> items = new ArrayList<>();
}
```

### Fetch Join 쿼리 (JPA)

Entity와 연관관계를 함께 로딩해야 할 때 사용:

```java
@Query("SELECT c FROM CustomerJpaEntity c LEFT JOIN FETCH c.addresses WHERE c.id = :id")
Optional<CustomerJpaEntity> findByIdWithAddresses(@Param("id") String id);

@Query("SELECT p FROM ProductJpaEntity p LEFT JOIN FETCH p.variants WHERE p.id = :id")
Optional<ProductJpaEntity> findByIdWithVariants(@Param("id") String id);

@Query("SELECT r FROM ReviewJpaEntity r LEFT JOIN FETCH r.images LEFT JOIN FETCH r.reply WHERE r.id = :id")
Optional<ReviewJpaEntity> findByIdWithDetails(@Param("id") String id);
```

### 통계/Projection 쿼리 (jOOQ)

컴파일 타임 타입 안전성이 필요한 경우 사용:

```java
// 평균 평점 조회
public Double getAverageRatingByProductId(String productId) {
    return dsl.select(DSL.avg(REVIEWS.RATING))
            .from(REVIEWS)
            .where(REVIEWS.PRODUCT_ID.eq(productId))
            .fetchOneInto(Double.class);
}

// 특정 필드만 조회 (Projection)
public Optional<String> findBusinessNameById(String sellerId) {
    return Optional.ofNullable(
        dsl.select(SELLERS.BUSINESS_NAME)
           .from(SELLERS)
           .where(SELLERS.ID.eq(sellerId))
           .fetchOneInto(String.class)
    );
}
```

### Quick Start

```bash
# 1. Docker 컨테이너 시작
docker compose up -d postgres

# 2. 스키마 적용 (비밀번호: secret)
PGPASSWORD=secret psql -h localhost -U delivery -d delivery -f docs/database/schema.sql

# 새 데이터베이스로 시작
psql -h localhost -U postgres -c "DROP DATABASE IF EXISTS delivery;"
psql -h localhost -U postgres -c "CREATE DATABASE delivery;"
PGPASSWORD=secret psql -h localhost -U delivery -d delivery -f docs/database/schema.sql
```

## Dummy Data

테스트/개발 환경에서 사용할 더미 데이터를 생성하는 스크립트입니다.

### 생성 데이터 요약

| 테이블 | 데이터 수 | 설명 |
|--------|----------|------|
| sellers | 20 | 판매자 |
| categories | 30 | 카테고리 (3-depth 계층) |
| products | 10,000 | 상품 |
| product_variants | ~25,000 | 상품 옵션 (상품당 평균 2.5개) |
| product_images | ~30,000 | 상품 이미지 (상품당 평균 3개) |
| product_categories | ~15,000 | 상품-카테고리 매핑 |

### 사용법

```bash
# 방법 1: 쉘 스크립트 사용 (권장)
cd docs/database
./init_data.sh           # 스키마 + 더미 데이터 생성
./init_data.sh --reset   # 기존 데이터 삭제 후 재생성
./init_data.sh --data    # 더미 데이터만 생성
./init_data.sh --stats   # 통계 확인

# 방법 2: SQL 직접 실행
PGPASSWORD=secret psql -h localhost -U postgres -d delivery -f docs/database/dummy_data.sql

# 데이터 초기화 (모든 데이터 삭제)
PGPASSWORD=secret psql -h localhost -U postgres -d delivery -f docs/database/reset_data.sql
```

### 환경 변수

| 변수명 | 기본값 | 설명 |
|--------|--------|------|
| DB_HOST | localhost | 데이터베이스 호스트 |
| DB_PORT | 5432 | 데이터베이스 포트 |
| DB_NAME | delivery | 데이터베이스 이름 |
| DB_USER | postgres | 데이터베이스 사용자 |
| DB_PASSWORD | secret | 데이터베이스 비밀번호 |

### 카테고리 구조

```
대분류 (6개)
├── 패션의류
│   ├── 남성의류
│   │   ├── 남성 상의
│   │   └── 남성 하의
│   └── 여성의류
│       ├── 여성 상의
│       └── 여성 하의
├── 전자기기
│   ├── 스마트폰
│   │   ├── 스마트폰 본체
│   │   └── 스마트폰 케이스
│   └── 컴퓨터
│       ├── 노트북
│       └── 모니터
├── 가구/인테리어
│   ├── 거실가구
│   │   ├── 소파
│   │   └── 테이블
│   └── 침실가구
├── 뷰티
│   ├── 스킨케어
│   │   ├── 에센스/세럼
│   │   └── 크림/로션
│   └── 메이크업
├── 스포츠/레저
│   ├── 헬스/요가
│   └── 캠핑/아웃도어
└── 식품
    ├── 신선식품
    └── 가공식품
```

## Version History

| Version | Date | Description |
|---------|------|-------------|
| 2.0.0 | 2025-01-01 | 물품 배송 서비스 (Product Delivery) |
| 1.0.0 | 2025-01-01 | 음식 배달 서비스 (Food Delivery) |

## Related Documents

- [Architecture](../architecture/README.md)
- [API Documentation](../api/README.md)
- [Tech Stack](../guides/TECH_STACK.md)
