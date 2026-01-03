# Project Structure

## Directory Layout

```
src/main/java/jjh/delivery/
├── DeliveryApplication.java              # Spring Boot Entry Point
│
├── domain/                               # Domain Layer (순수 도메인)
│   ├── customer/                         # 고객 도메인
│   │   ├── Customer.java                 # Aggregate Root
│   │   ├── Address.java                  # Value Object
│   │   └── exception/
│   ├── seller/                           # 판매자 도메인
│   │   ├── Seller.java
│   │   ├── SellerStatus.java
│   │   └── exception/
│   ├── product/                          # 상품 도메인
│   │   ├── Product.java
│   │   ├── ProductVariant.java
│   │   ├── ProductStatus.java
│   │   └── exception/
│   ├── category/                         # 카테고리 도메인
│   │   ├── Category.java
│   │   └── exception/
│   ├── cart/                             # 장바구니 도메인
│   │   ├── Cart.java
│   │   ├── CartItem.java
│   │   └── exception/
│   ├── order/                            # 주문 도메인
│   │   ├── Order.java                    # Aggregate Root
│   │   ├── OrderItem.java
│   │   ├── OrderStatus.java              # Enum with State Machine
│   │   ├── event/
│   │   │   └── OrderEvent.java           # Sealed Interface
│   │   └── exception/
│   ├── payment/                          # 결제 도메인
│   │   ├── Payment.java
│   │   ├── PaymentStatus.java
│   │   └── exception/
│   ├── shipment/                         # 배송 도메인
│   │   ├── Shipment.java
│   │   ├── ShipmentStatus.java
│   │   └── exception/
│   ├── returns/                          # 반품 도메인
│   │   ├── Return.java
│   │   ├── ReturnStatus.java
│   │   └── exception/
│   ├── review/                           # 리뷰 도메인
│   │   ├── Review.java
│   │   ├── ReviewImage.java
│   │   ├── ReviewReply.java
│   │   └── exception/
│   ├── promotion/                        # 프로모션 도메인
│   │   ├── Promotion.java
│   │   └── exception/
│   ├── webhook/                          # 웹훅 도메인
│   │   └── Webhook.java
│   ├── outbox/                           # Outbox Pattern 도메인
│   │   ├── OutboxEvent.java              # 이벤트 저장 엔티티
│   │   └── OutboxStatus.java             # 상태 (PENDING, SENT, FAILED)
│   └── idempotency/                      # Consumer 멱등성 도메인
│       └── ProcessedEvent.java           # 처리된 이벤트 기록
│
├── application/                          # Application Layer (Use Cases)
│   ├── port/
│   │   ├── in/                           # Driving Ports (외부 → 애플리케이션)
│   │   │   ├── Manage{Entity}UseCase.java    # CRUD Use Case (Command)
│   │   │   └── Query{Entity}UseCase.java     # Query Use Case (Record)
│   │   └── out/                          # Driven Ports (애플리케이션 → 외부)
│   │       ├── Load{Entity}Port.java         # Read Port
│   │       ├── Save{Entity}Port.java         # Write Port
│   │       ├── Load{Entity}StatsPort.java    # Statistics Port (jOOQ)
│   │       ├── {Entity}SearchPort.java       # Search Port (ES)
│   │       ├── SaveOutboxEventPort.java      # Outbox 저장 Port
│   │       ├── LoadOutboxEventPort.java      # Outbox 조회 Port
│   │       └── ProcessedEventPort.java       # 멱등성 체크 Port
│   └── service/
│       ├── {Entity}Service.java          # Use Case 구현체
│       ├── OutboxEventPublisher.java     # Outbox → Kafka 발행 스케줄러
│       └── OutboxEventCleaner.java       # Outbox 정리 스케줄러
│
├── adapter/                              # Adapter Layer (Infrastructure)
│   ├── in/                               # Driving Adapters
│   │   ├── web/
│   │   │   ├── {domain}/                 # 도메인별 하위 디렉토리
│   │   │   │   ├── {Entity}Controller.java
│   │   │   │   └── dto/
│   │   │   │       ├── {Action}{Entity}Request.java
│   │   │   │       └── {Entity}Response.java
│   │   │   ├── dto/                      # 공통 DTO
│   │   │   ├── mapper/
│   │   │   │   └── {Entity}WebMapper.java
│   │   │   └── exception/
│   │   │       └── GlobalExceptionHandler.java
│   │   └── messaging/
│   │       └── {Entity}KafkaListener.java
│   │
│   └── out/                              # Driven Adapters
│       ├── persistence/
│       │   ├── jpa/                      # JPA: CRUD, Fetch Join
│       │   │   ├── entity/
│       │   │   │   ├── {Entity}JpaEntity.java
│       │   │   │   ├── OutboxEventJpaEntity.java     # Outbox 엔티티
│       │   │   │   └── ProcessedEventJpaEntity.java  # 멱등성 엔티티
│       │   │   ├── repository/
│       │   │   │   ├── {Entity}JpaRepository.java
│       │   │   │   ├── OutboxEventJpaRepository.java
│       │   │   │   └── ProcessedEventJpaRepository.java
│       │   │   ├── mapper/
│       │   │   │   └── {Entity}PersistenceMapper.java
│       │   │   ├── OutboxEventJpaAdapter.java        # Outbox Port 구현
│       │   │   └── ProcessedEventJpaAdapter.java     # 멱등성 Port 구현
│       │   └── jooq/                     # jOOQ: 통계, Projection
│       │       ├── repository/
│       │       │   └── {Entity}JooqRepository.java
│       │       └── {Entity}JooqAdapter.java
│       ├── messaging/
│       │   └── OrderOutboxAdapter.java   # Outbox Pattern 이벤트 발행
│       └── search/
│           └── {Entity}ElasticsearchAdapter.java
│
└── config/                               # Configuration
    ├── JpaConfig.java
    ├── JooqConfig.java
    ├── KafkaConfig.java
    ├── ElasticsearchConfig.java
    ├── OpenApiConfig.java                # Swagger/OpenAPI 설정
    ├── WebConfig.java
    ├── SchedulingConfig.java             # @EnableScheduling (Outbox)
    ├── JacksonConfig.java                # ObjectMapper 설정
    └── security/                         # Security 설정
        ├── SecurityConfig.java
        ├── JwtProperties.java
        └── JwtTokenProvider.java

src/main/resources/
├── application.yaml                      # Application Configuration

docs/
├── README.md                             # 메인 문서 인덱스
├── architecture/                         # 아키텍처 & 설계
├── api/                                  # API 문서
├── database/                             # 데이터베이스
└── guides/                               # 가이드
```

## Package Naming Convention

| Package | Purpose | Naming Pattern |
|---------|---------|----------------|
| `domain` | 순수 도메인 객체 | `{aggregate}/` |
| `application.port.in` | Driving Ports | `{Action}{Entity}UseCase` |
| `application.port.out` | Driven Ports | `{Action}{Entity}Port` |
| `application.service` | Use Case 구현 | `{Entity}Service` |
| `adapter.in.web` | REST Controllers | `{Entity}Controller` |
| `adapter.in.messaging` | Kafka Listeners | `{Entity}KafkaListener` |
| `adapter.out.persistence` | DB Adapters | `{Entity}{Tech}Adapter` |
| `adapter.out.messaging` | Event Publishers | `{Entity}KafkaAdapter` |
| `adapter.out.search` | Search Adapters | `{Entity}ElasticsearchAdapter` |

## Key Design Decisions

### 1. Aggregate per Domain
각 도메인은 독립적인 Aggregate로 관리:
- Customer, Seller, Product, Category
- Cart, Order, Payment, Shipment, Returns
- Review, Promotion, Webhook

### 2. Port/Adapter Separation
- **Port**: 인터페이스 (비즈니스 의도 표현)
- **Adapter**: 구현체 (기술적 세부사항)

### 3. DTO Layering
```
Request DTO → Command → Domain → Entity → Document
     ↓           ↓         ↓         ↓         ↓
   Web API   Application  Core    Database   Search
```

### 4. Event-Driven Communication
도메인 이벤트를 통한 느슨한 결합

### 5. Lombok Conventions

#### Entity Pattern
```java
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Entity
public class OrderJpaEntity { ... }
```

#### Service Pattern
```java
@Service
@RequiredArgsConstructor
public class OrderService implements CreateOrderUseCase { ... }
```

### 6. JPA + jOOQ Hybrid Persistence

| Repository Type | Technology | Use Case |
|-----------------|------------|----------|
| `{Entity}JpaRepository` | JPA | CRUD, Fetch Join, Entity 관계 로딩 |
| `{Entity}JooqRepository` | jOOQ | 통계, Projection, 컴파일 타임 타입 안전성 |

#### JPA Repository 예시 (Fetch Join)
```java
@Query("SELECT c FROM CustomerJpaEntity c LEFT JOIN FETCH c.addresses WHERE c.id = :id")
Optional<CustomerJpaEntity> findByIdWithAddresses(@Param("id") String id);
```

#### jOOQ Repository 예시 (통계)
```java
public Double getAverageRatingByProductId(String productId) {
    return dsl.select(DSL.avg(REVIEWS.RATING))
            .from(REVIEWS)
            .where(REVIEWS.PRODUCT_ID.eq(productId))
            .fetchOneInto(Double.class);
}
```

### 7. Port 분리 전략

| Port Type | Purpose | Implementation |
|-----------|---------|----------------|
| `Load{Entity}Port` | Entity 조회 | JPA Adapter |
| `Save{Entity}Port` | Entity 저장/삭제 | JPA Adapter |
| `Load{Entity}StatsPort` | 통계 조회 | jOOQ Adapter |
| `{Entity}SearchPort` | 전문 검색 | Elasticsearch Adapter |
