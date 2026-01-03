# Technology Stack

## Core Framework

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 | Programming Language (LTS) |
| Spring Boot | 4.0.1 | Application Framework |
| Gradle | - | Build Tool |
| Lombok | - | Boilerplate Reduction |

## Spring Modules

| Module | Purpose | Usage |
|--------|---------|-------|
| Spring MVC | REST API | Controller, Request/Response 처리 |
| Spring Data JPA | ORM | CRUD, Fetch Join, Entity 관계 로딩 |
| Spring jOOQ | Type-safe SQL | 통계 쿼리, 단순 Projection |
| Spring Kafka | Messaging | 이벤트 발행/구독 |
| Spring Data Elasticsearch | Search | 전문 검색, 로그 분석 |
| Spring Validation | Validation | Bean Validation (JSR-380) |
| Spring Actuator | Monitoring | Health Check, Metrics |
| Spring WebClient | HTTP Client | 외부 API 호출 |
| Spring Security | Security | 인증/인가, JWT 토큰 기반 |

## Security

### JWT Authentication

JWT (JSON Web Token) 기반 인증 구현:

| Component | Purpose |
|-----------|---------|
| `JwtTokenProvider` | 토큰 생성/검증/파싱 |
| `JwtProperties` | JWT 설정 (secret, expiration) |
| `JwtAuthenticationFilter` | 요청별 토큰 검증 필터 |
| `AuthenticatedUser` | 인증된 사용자 정보 래퍼 |

### Authentication Flow

```
1. POST /auth/login (email, password)
      ↓
2. Validate credentials
      ↓
3. Generate JWT tokens (access + refresh)
      ↓
4. Return TokenResponse
      ↓
5. Client includes "Authorization: Bearer {token}" in requests
      ↓
6. JwtAuthenticationFilter validates token
      ↓
7. Set SecurityContext with authenticated user
```

### Controller Authentication Pattern

```java
@GetMapping
public ResponseEntity<OrderListResponse> getMyOrders(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestParam(required = false) String status
) {
    UUID customerId = UUID.fromString(userDetails.getUsername());
    // ... business logic
}
```

### Dependencies

```groovy
// JWT
implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.6'
runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.6'
```

## Lombok Usage Patterns

### Entity Annotations

모든 JPA Entity는 다음 Lombok 어노테이션 패턴을 사용:

```java
@Getter                                      // Getter 자동 생성
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // JPA 기본 생성자
@Builder                                     // Builder 패턴
@Entity
public class OrderJpaEntity {
    // 필드 정의
}
```

| Annotation | Purpose | Notes |
|------------|---------|-------|
| `@Getter` | Getter 메서드 생성 | Setter는 의도적으로 제외 (불변성) |
| `@NoArgsConstructor(access = PROTECTED)` | JPA 기본 생성자 | Protected로 외부 생성 제한 |
| `@Builder` | Builder 패턴 | 복잡한 객체 생성 시 사용 |

### Service/Controller Annotations

서비스 레이어는 생성자 주입을 위해 다음 패턴 사용:

```java
@Service
@RequiredArgsConstructor  // final 필드 생성자 자동 생성
public class OrderService implements CreateOrderUseCase {
    private final LoadOrderPort loadOrderPort;
    private final SaveOrderPort saveOrderPort;
}
```

### ⚠️ Lombok 사용 시 주의사항

- **@Setter 미사용**: Entity의 불변성 보장을 위해 Setter 생성 금지
- **@Data 미사용**: equals/hashCode 자동 생성 시 JPA 프록시 문제 발생 가능
- **@AllArgsConstructor 미사용**: Builder 패턴과 충돌, 필드 순서 의존성 문제

## Infrastructure

| Technology | Purpose | Configuration |
|------------|---------|---------------|
| PostgreSQL | Primary Database | JPA + jOOQ |
| Kafka | Message Broker | Event-Driven Architecture |
| Elasticsearch | Search Engine | Full-text Search |
| Prometheus | Metrics Collection | Micrometer Registry |

## Technology Roles

### Data Access Strategy (JPA + jOOQ 하이브리드)

```
┌─────────────────────────────────────────────────────────────┐
│                      Data Access Layer                       │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│   ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │
│   │  Spring     │  │    jOOQ     │  │   Elasticsearch     │ │
│   │  Data JPA   │  │             │  │                     │ │
│   ├─────────────┤  ├─────────────┤  ├─────────────────────┤ │
│   │ • CRUD      │  │ • 통계/집계  │  │ • 전문 검색          │ │
│   │ • Fetch Join│  │ • Projection│  │ • 자동완성           │ │
│   │ • Entity    │  │ • 단순 조회  │  │ • 필터링             │ │
│   │   관계 로딩  │  │   (DTO)     │  │ • 로그 분석          │ │
│   └─────────────┘  └─────────────┘  └─────────────────────┘ │
│          │                │                   │              │
│          └────────────────┼───────────────────┘              │
│                           ▼                                  │
│                    ┌─────────────┐                           │
│                    │ PostgreSQL  │                           │
│                    └─────────────┘                           │
└─────────────────────────────────────────────────────────────┘
```

### JPA vs jOOQ 역할 분담

| 영역 | JPA | jOOQ |
|------|-----|------|
| **CRUD** | ✅ 담당 | ❌ |
| **Fetch Join** | ✅ 담당 | ❌ |
| **Entity 관계 로딩** | ✅ 담당 | ❌ |
| **통계/집계** | ❌ | ✅ 담당 |
| **단순 Projection** | ❌ | ✅ 담당 |
| **컴파일 타임 타입 안전성** | ❌ (런타임) | ✅ |

### Fetch Join을 JPA에 유지하는 이유

JPA의 `LEFT JOIN FETCH`는 다음과 같은 이점으로 인해 JPA에서 유지:

```java
// JPA: 1줄 - 자동 Entity 그래프 구성
@Query("SELECT c FROM CustomerJpaEntity c LEFT JOIN FETCH c.addresses WHERE c.id = :id")
Optional<CustomerJpaEntity> findByIdWithAddresses(@Param("id") String id);
```

1. **자동 Entity 그래프 구성**: JPA가 연관 엔티티를 자동으로 채움
2. **영속성 컨텍스트 통합**: 1차 캐시, 변경 감지 자동 적용
3. **코드 복잡도 최소화**: jOOQ로 구현 시 20-30줄의 변환 코드 필요
4. **N+1 최적화**: Fetch Join은 JPA의 핵심 최적화 기능

### jOOQ 사용 사례

jOOQ는 컴파일 타임 타입 안전성이 중요한 영역에 사용:

```java
// jOOQ: 평균 평점 계산 (통계)
public Double getAverageRatingByProductId(String productId) {
    return dsl.select(DSL.avg(REVIEWS.RATING))
            .from(REVIEWS)
            .where(REVIEWS.PRODUCT_ID.eq(productId))
            .and(REVIEWS.IS_VISIBLE.isTrue())
            .fetchOneInto(Double.class);
}

// jOOQ: 단순 Projection (특정 필드만 조회)
public Optional<String> findBusinessNameById(String sellerId) {
    return Optional.ofNullable(
        dsl.select(SELLERS.BUSINESS_NAME)
           .from(SELLERS)
           .where(SELLERS.ID.eq(sellerId))
           .fetchOneInto(String.class)
    );
}
```

### 아키텍처 결정 원칙

```
┌─────────────────────────────────────────────────────────────┐
│                    Query Type Decision Tree                  │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│   쿼리 타입?                                                  │
│       │                                                      │
│       ├── Entity + 연관관계 필요 ──────▶ JPA (Fetch Join)     │
│       │                                                      │
│       ├── 통계/집계/리포팅 ─────────────▶ jOOQ                │
│       │                                                      │
│       ├── 단순 필드 조회 (Projection) ──▶ jOOQ                │
│       │                                                      │
│       └── 전문 검색/필터링 ─────────────▶ Elasticsearch       │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### Event Flow

```
┌──────────┐     ┌─────────┐     ┌──────────────┐
│  Order   │────▶│  Kafka  │────▶│   Delivery   │
│ Service  │     │         │     │   Service    │
└──────────┘     └─────────┘     └──────────────┘
     │                │
     │                ▼
     │          ┌─────────────┐
     └─────────▶│Elasticsearch│
                │   (Index)   │
                └─────────────┘
```

## Validation Strategy

### Defense in Depth (이중 방어선)

| Layer | Validation Type | Technology | Example |
|-------|-----------------|------------|---------|
| Request DTO | Format Validation | Bean Validation | @NotBlank, @Size, @Positive |
| Command | Business Rules | Compact Constructor | items.size() <= 50 |
| Domain | Domain Invariants | Domain Logic | State Machine |

### Validation Annotations

```java
// Request DTO Level
@NotBlank(message = "고객 ID는 필수입니다")
@Size(max = 500, message = "배송 주소는 500자 이하여야 합니다")
@Positive(message = "수량은 1 이상이어야 합니다")
@NotEmpty(message = "주문 항목은 최소 1개 이상이어야 합니다")
@Valid  // 중첩 객체 검증

// Command Level
if (items.size() > MAX_ORDER_ITEMS) {
    throw new IllegalArgumentException("한 주문에 50개 이상의 항목을 담을 수 없습니다");
}
```

## Error Handling

### RFC 7807 Problem Details

```json
{
  "type": "https://api.delivery.com/errors/validation",
  "title": "Validation Failed",
  "status": 400,
  "detail": "입력값 검증에 실패했습니다",
  "errors": {
    "customerId": "고객 ID는 필수입니다",
    "items": "주문 항목은 최소 1개 이상이어야 합니다"
  },
  "timestamp": "2025-01-01T10:00:00Z"
}
```

### Exception Hierarchy

| Exception | HTTP Status | Use Case |
|-----------|-------------|----------|
| `MethodArgumentNotValidException` | 400 | Validation 실패 |
| `IllegalArgumentException` | 400 | 잘못된 요청 |
| `OrderNotFoundException` | 404 | 주문 없음 |
| `IllegalStateException` | 409 | 상태 전이 오류 |

## Configuration

### Application Properties

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/delivery
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
  kafka:
    bootstrap-servers: localhost:9092
  elasticsearch:
    uris: localhost:9200
```

## Dependencies

```groovy
dependencies {
    // Spring Boot Starters
    implementation 'org.springframework.boot:spring-boot-starter-webmvc'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-jooq'
    implementation 'org.springframework.boot:spring-boot-starter-kafka'
    implementation 'org.springframework.boot:spring-boot-starter-data-elasticsearch'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'org.springframework.boot:spring-boot-starter-security'

    // API Documentation
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0'

    // JWT
    implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.6'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.6'

    // Database
    runtimeOnly 'org.postgresql:postgresql'

    // Monitoring
    runtimeOnly 'io.micrometer:micrometer-registry-prometheus'

    // Development
    compileOnly 'org.projectlombok:lombok'
    developmentOnly 'org.springframework.boot:spring-boot-devtools'
}
```
