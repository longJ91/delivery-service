# Delivery Service Architecture

## Overview

Hexagonal Architecture (Ports & Adapters) 기반의 배달 서비스 백엔드 애플리케이션.

## Architecture Diagram

```
                    ┌─────────────────────────────────────────────────────────┐
                    │                    Driving Adapters                      │
                    │  ┌─────────────┐  ┌─────────────┐                       │
                    │  │   REST API  │  │    Kafka    │                       │
                    │  │ Controller  │  │  Listener   │                       │
                    │  └──────┬──────┘  └──────┬──────┘                       │
                    └─────────┼────────────────┼──────────────────────────────┘
                              │                │
                              ▼                ▼
                    ┌─────────────────────────────────────────────────────────┐
                    │                   Driving Ports (In)                     │
                    │  ┌─────────────────────────────────────────────────┐    │
                    │  │  CreateOrderUseCase  │  GetOrderUseCase  │ ...  │    │
                    │  └─────────────────────────────────────────────────┘    │
                    └─────────────────────────────────────────────────────────┘
                                              │
                                              ▼
                    ┌─────────────────────────────────────────────────────────┐
                    │                   Application Layer                      │
                    │  ┌─────────────────────────────────────────────────┐    │
                    │  │     OrderService     │    OrderQueryService     │    │
                    │  └─────────────────────────────────────────────────┘    │
                    └─────────────────────────────────────────────────────────┘
                                              │
                                              ▼
                    ┌─────────────────────────────────────────────────────────┐
                    │                     Domain Layer                         │
                    │  ┌─────────────────────────────────────────────────┐    │
                    │  │  Order (Aggregate)  │  OrderItem  │  OrderStatus │   │
                    │  │  OrderEvent         │  OrderNotFoundException    │   │
                    │  └─────────────────────────────────────────────────┘    │
                    └─────────────────────────────────────────────────────────┘
                                              │
                                              ▼
                    ┌─────────────────────────────────────────────────────────┐
                    │                   Driven Ports (Out)                     │
                    │  ┌───────────┐ ┌───────────┐ ┌───────────┐ ┌──────────┐ │
                    │  │LoadOrder  │ │SaveOrder  │ │OrderEvent │ │OrderQuery│ │
                    │  │   Port    │ │   Port    │ │   Port    │ │   Port   │ │
                    │  └───────────┘ └───────────┘ └───────────┘ └──────────┘ │
                    └─────────────────────────────────────────────────────────┘
                              │                │              │           │
                              ▼                ▼              ▼           ▼
                    ┌─────────────────────────────────────────────────────────┐
                    │                    Driven Adapters                       │
                    │  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌───────────┐   │
                    │  │   JPA   │  │  jOOQ   │  │  Kafka  │  │Elasticsearch│ │
                    │  │ Adapter │  │ Adapter │  │ Adapter │  │  Adapter   │  │
                    │  └─────────┘  └─────────┘  └─────────┘  └───────────┘   │
                    └─────────────────────────────────────────────────────────┘
                              │                │              │           │
                              ▼                ▼              ▼           ▼
                    ┌─────────┐        ┌─────────┐    ┌─────────┐  ┌───────────┐
                    │PostgreSQL│       │PostgreSQL│   │  Kafka  │  │Elasticsearch│
                    └─────────┘        └─────────┘    └─────────┘  └───────────┘
```

## Layer Descriptions

### Domain Layer
순수한 비즈니스 로직을 담당. 외부 프레임워크 의존성 없음.

| Component | Description |
|-----------|-------------|
| `Order` | Aggregate Root - 주문 엔티티 |
| `OrderItem` | Value Object - 주문 항목 |
| `OrderStatus` | Enum with State Machine - 상태 전이 규칙 |
| `OrderEvent` | Sealed Interface - 도메인 이벤트 |
| `OrderNotFoundException` | Domain Exception |

### Application Layer
Use Case 구현 및 비즈니스 흐름 조합.

| Component | Description |
|-----------|-------------|
| `CreateOrderUseCase` | 주문 생성 Port |
| `GetOrderUseCase` | 주문 조회 Port |
| `UpdateOrderStatusUseCase` | 주문 상태 변경 Port |
| `SearchOrderUseCase` | 주문 검색 Port |
| `OrderService` | Use Case 구현체 |
| `OrderQueryService` | 복잡한 쿼리 전용 서비스 |

### Adapter Layer

#### Driving Adapters (Inbound)
| Adapter | Technology | Description |
|---------|------------|-------------|
| `OrderController` | Spring MVC | REST API 엔드포인트 |
| `OrderKafkaListener` | Spring Kafka | 이벤트 수신 |

#### Driven Adapters (Outbound)
| Adapter | Technology | Description |
|---------|------------|-------------|
| `OrderJpaAdapter` | Spring Data JPA | CRUD, 단순 쿼리 |
| `OrderJooqAdapter` | jOOQ | 복잡한 쿼리, 통계, 리포팅 |
| `OrderKafkaAdapter` | Spring Kafka | 이벤트 발행 |
| `OrderElasticsearchAdapter` | Spring Data Elasticsearch | 전문 검색 |

## Dependency Direction

```
adapter → application → domain
   ↓           ↓           ↓
  구현체      Port       순수 객체
(Framework) (Interface)  (No Deps)
```

**핵심 원칙**: 의존성은 항상 안쪽(Domain)으로 향함
