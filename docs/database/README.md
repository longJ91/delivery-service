# Database Documentation

데이터베이스 스키마 설계 및 관련 문서입니다.

## Documents

| Document | Description | Version |
|----------|-------------|---------|
| [SCHEMA.md](./SCHEMA.md) | 스키마 설계 문서 | 2.0.0 |
| [schema_v2.sql](./schema_v2.sql) | PostgreSQL DDL (물품 배송) | 2.0.0 |
| [schema.sql](./schema.sql) | PostgreSQL DDL (음식 배달) | 1.0.0 |
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
# 스키마 적용
psql -U postgres -d delivery -f docs/database/schema_v2.sql

# 새 데이터베이스로 시작
psql -U postgres -c "DROP DATABASE IF EXISTS delivery;"
psql -U postgres -c "CREATE DATABASE delivery;"
psql -U postgres -d delivery -f docs/database/schema_v2.sql
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
