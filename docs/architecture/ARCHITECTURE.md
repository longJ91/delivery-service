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
| `OrderJpaAdapter` | Spring Data JPA | CRUD, Fetch Join, Entity 관계 로딩 |
| `OrderJooqAdapter` | jOOQ | 통계/집계, Projection, 컴파일 타임 타입 안전성 |
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

## Persistence Strategy (JPA + jOOQ 하이브리드)

### 역할 분담

```
┌─────────────────────────────────────────────────────────────┐
│               JPA + jOOQ Hybrid Architecture                │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│   ┌───────────────────────────┐  ┌────────────────────────┐ │
│   │       JPA Adapter         │  │     jOOQ Adapter       │ │
│   ├───────────────────────────┤  ├────────────────────────┤ │
│   │ • save(), delete()        │  │ • 평균 평점 계산        │ │
│   │ • findById()              │  │ • 평점 분포 조회        │ │
│   │ • findByIdWithAddresses() │  │ • 카테고리별 상품 수    │ │
│   │ • findByIdWithVariants()  │  │ • 판매자 상호명 조회    │ │
│   │ • findByIdWithDetails()   │  │ • 비밀번호 조회/변경    │ │
│   ├───────────────────────────┤  ├────────────────────────┤ │
│   │ ✅ Entity Graph 자동 구성  │  │ ✅ 컴파일 타임 안전성   │ │
│   │ ✅ 영속성 컨텍스트 통합    │  │ ✅ 타입 안전한 통계     │ │
│   │ ✅ 변경 감지 자동 적용     │  │ ✅ 단순 Projection     │ │
│   └───────────────────────────┘  └────────────────────────┘ │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### 쿼리 타입별 기술 선택

| Query Type | Technology | Reason |
|------------|------------|--------|
| CRUD | JPA | 표준 영속성 작업 |
| Fetch Join | JPA | Entity 관계 자동 로딩, 영속성 컨텍스트 통합 |
| 통계/집계 | jOOQ | AVG, COUNT, GROUP BY 등 컴파일 타임 안전성 |
| 단순 Projection | jOOQ | 특정 필드만 조회, DTO 직접 매핑 |
| 전문 검색 | Elasticsearch | 형태소 분석, 필터링, 자동완성 |

### 아키텍처 결정 이유

**Fetch Join을 JPA에 유지하는 이유:**

1. **자동 Entity 그래프 구성**: JPA가 연관 엔티티를 자동으로 채움
2. **영속성 컨텍스트 통합**: 1차 캐시, 변경 감지 자동 적용
3. **코드 복잡도**: jOOQ로 구현 시 Record→Entity 변환 코드 20-30줄 추가 필요
4. **유지보수성**: JPA의 핵심 기능 활용으로 코드 일관성 유지

**jOOQ 사용 영역:**

1. **통계 쿼리**: AVG, SUM, COUNT 등 집계 함수 사용 시 타입 안전성 보장
2. **단순 Projection**: 특정 필드만 조회 시 불필요한 Entity 로딩 방지
3. **인증 관련**: 비밀번호 조회/변경 등 보안 관련 쿼리의 정확성 보장
