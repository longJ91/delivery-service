# Technology Stack

## Core Framework

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 | Programming Language (LTS) |
| Spring Boot | 4.0.1 | Application Framework |
| Gradle | - | Build Tool |

## Spring Modules

| Module | Purpose | Usage |
|--------|---------|-------|
| Spring MVC | REST API | Controller, Request/Response 처리 |
| Spring Data JPA | ORM | CRUD, 단순 쿼리 |
| Spring jOOQ | Type-safe SQL | 복잡한 쿼리, 통계, 리포팅 |
| Spring Kafka | Messaging | 이벤트 발행/구독 |
| Spring Data Elasticsearch | Search | 전문 검색, 로그 분석 |
| Spring Validation | Validation | Bean Validation (JSR-380) |
| Spring Actuator | Monitoring | Health Check, Metrics |
| Spring WebClient | HTTP Client | 외부 API 호출 |

## Infrastructure

| Technology | Purpose | Configuration |
|------------|---------|---------------|
| PostgreSQL | Primary Database | JPA + jOOQ |
| Kafka | Message Broker | Event-Driven Architecture |
| Elasticsearch | Search Engine | Full-text Search |
| Prometheus | Metrics Collection | Micrometer Registry |

## Technology Roles

### Data Access Strategy

```
┌─────────────────────────────────────────────────────────────┐
│                      Data Access Layer                       │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│   ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │
│   │  Spring     │  │    jOOQ     │  │   Elasticsearch     │ │
│   │  Data JPA   │  │             │  │                     │ │
│   ├─────────────┤  ├─────────────┤  ├─────────────────────┤ │
│   │ • CRUD      │  │ • 복잡 쿼리  │  │ • 전문 검색          │ │
│   │ • 단순 조회  │  │ • 동적 쿼리  │  │ • 자동완성           │ │
│   │ • 연관관계   │  │ • 통계/집계  │  │ • 필터링             │ │
│   │ • 트랜잭션   │  │ • 리포팅    │  │ • 로그 분석          │ │
│   └─────────────┘  └─────────────┘  └─────────────────────┘ │
│          │                │                   │              │
│          └────────────────┼───────────────────┘              │
│                           ▼                                  │
│                    ┌─────────────┐                           │
│                    │ PostgreSQL  │                           │
│                    └─────────────┘                           │
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

    // Database
    runtimeOnly 'org.postgresql:postgresql'

    // Monitoring
    runtimeOnly 'io.micrometer:micrometer-registry-prometheus'

    // Development
    compileOnly 'org.projectlombok:lombok'
    developmentOnly 'org.springframework.boot:spring-boot-devtools'
}
```
