# Project Structure

## Directory Layout

```
src/main/java/jjh/delivery/
├── DeliveryApplication.java          # Spring Boot Entry Point
│
├── domain/                            # Domain Layer (순수 도메인)
│   └── order/
│       ├── Order.java                 # Aggregate Root
│       ├── OrderItem.java             # Value Object
│       ├── OrderStatus.java           # Enum with State Machine
│       ├── event/
│       │   ├── OrderEvent.java        # Sealed Interface
│       │   ├── OrderCreatedEvent.java
│       │   └── OrderStatusChangedEvent.java
│       └── exception/
│           └── OrderNotFoundException.java
│
├── application/                       # Application Layer (Use Cases)
│   ├── port/
│   │   ├── in/                        # Driving Ports (외부 → 애플리케이션)
│   │   │   ├── CreateOrderUseCase.java
│   │   │   ├── GetOrderUseCase.java
│   │   │   ├── UpdateOrderStatusUseCase.java
│   │   │   └── SearchOrderUseCase.java
│   │   └── out/                       # Driven Ports (애플리케이션 → 외부)
│   │       ├── LoadOrderPort.java
│   │       ├── SaveOrderPort.java
│   │       ├── OrderEventPort.java
│   │       ├── OrderSearchPort.java
│   │       └── OrderQueryPort.java
│   └── service/
│       ├── OrderService.java          # Use Case 구현체
│       └── OrderQueryService.java     # jOOQ 전용 쿼리 서비스
│
├── adapter/                           # Adapter Layer (Infrastructure)
│   ├── in/                            # Driving Adapters
│   │   ├── web/
│   │   │   ├── OrderController.java
│   │   │   ├── dto/
│   │   │   │   ├── CreateOrderRequest.java
│   │   │   │   ├── OrderResponse.java
│   │   │   │   └── UpdateOrderStatusRequest.java
│   │   │   ├── mapper/
│   │   │   │   └── OrderWebMapper.java
│   │   │   └── exception/
│   │   │       └── GlobalExceptionHandler.java
│   │   └── messaging/
│   │       └── OrderKafkaListener.java
│   │
│   └── out/                           # Driven Adapters
│       ├── persistence/
│       │   ├── jpa/
│       │   │   ├── entity/
│       │   │   │   ├── OrderJpaEntity.java
│       │   │   │   └── OrderItemJpaEntity.java
│       │   │   ├── repository/
│       │   │   │   └── OrderJpaRepository.java
│       │   │   ├── mapper/
│       │   │   │   └── OrderPersistenceMapper.java
│       │   │   └── OrderJpaAdapter.java
│       │   └── jooq/
│       │       └── OrderJooqAdapter.java
│       ├── messaging/
│       │   ├── OrderKafkaAdapter.java
│       │   └── dto/
│       │       └── OrderEventMessage.java
│       └── search/
│           ├── document/
│           │   └── OrderDocument.java
│           ├── repository/
│           │   └── OrderElasticsearchRepository.java
│           └── OrderElasticsearchAdapter.java
│
└── config/                            # Configuration
    ├── JpaConfig.java
    ├── JooqConfig.java
    ├── KafkaConfig.java
    ├── ElasticsearchConfig.java
    └── WebConfig.java

src/main/resources/
├── application.yaml                   # Application Configuration

docs/
├── ARCHITECTURE.md                    # Architecture Overview
├── PROJECT_STRUCTURE.md               # This file
└── TECH_STACK.md                      # Technology Stack
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
각 도메인(Order, Delivery, Shop 등)은 독립적인 Aggregate로 관리

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
