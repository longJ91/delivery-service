# Delivery Service Documentation

## Overview

물품 배송 서비스 백엔드 애플리케이션 문서입니다.

- **Architecture**: Hexagonal Architecture (Ports & Adapters)
- **Framework**: Spring Boot 4.0.1
- **Language**: Java 21
- **Service Type**: Product Delivery (물품 배송)

## Directory Structure

```
docs/
├── README.md                    # 메인 문서 인덱스
├── architecture/                # 아키텍처 & 설계
│   ├── README.md
│   ├── ARCHITECTURE.md          # 시스템 아키텍처
│   └── PROJECT_STRUCTURE.md     # 프로젝트 구조
├── api/                         # API 문서
│   ├── README.md
│   ├── API.md                   # REST API 레퍼런스
│   └── openapi.yaml             # OpenAPI 3.0 스펙
├── database/                    # 데이터베이스
│   ├── README.md
│   ├── SCHEMA.md                # 스키마 설계 문서
│   ├── schema.sql               # PostgreSQL DDL (물품 배송)
│   └── MIGRATION.md             # 마이그레이션 계획
└── guides/                      # 가이드
    ├── README.md
    └── TECH_STACK.md            # 기술 스택
```

## Documentation Index

### Architecture & Design

| Document | Description |
|----------|-------------|
| [architecture/ARCHITECTURE.md](./architecture/ARCHITECTURE.md) | 시스템 아키텍처 (Hexagonal) |
| [architecture/PROJECT_STRUCTURE.md](./architecture/PROJECT_STRUCTURE.md) | 프로젝트 디렉토리 구조 |

### API Documentation

| Document | Description |
|----------|-------------|
| [api/API.md](./api/API.md) | REST API 레퍼런스 |
| [api/openapi.yaml](./api/openapi.yaml) | OpenAPI 3.0 스펙 (Swagger) |

### Database

| Document | Description |
|----------|-------------|
| [database/SCHEMA.md](./database/SCHEMA.md) | 스키마 설계 문서 |
| [database/schema.sql](./database/schema.sql) | PostgreSQL DDL |
| [database/MIGRATION.md](./database/MIGRATION.md) | 마이그레이션 계획 |

### Guides & References

| Document | Description |
|----------|-------------|
| [guides/TECH_STACK.md](./guides/TECH_STACK.md) | 기술 스택 및 사용 방법 |

## Swagger UI

애플리케이션 실행 후 아래 URL에서 API 문서 확인:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

## Quick Start

### Prerequisites

- Java 21
- Docker (PostgreSQL, Kafka, Elasticsearch)
- Gradle

### Run

```bash
# 빌드
./gradlew build

# 실행
./gradlew bootRun

# 테스트
./gradlew test
```

### Database Setup

```bash
# 스키마 적용
psql -U postgres -d delivery -f docs/database/schema.sql
```

## Key Features

### Hexagonal Architecture
- **Domain**: 순수 비즈니스 로직 (프레임워크 독립적)
- **Application**: Use Case 구현
- **Adapter**: 외부 시스템 연동

### Multi-Database Strategy
| Technology | Usage |
|------------|-------|
| JPA | CRUD, Fetch Join, Entity 관계 로딩 |
| jOOQ | 통계/집계, 단순 Projection |
| Elasticsearch | 전문 검색 |

### Security
| Feature | Implementation |
|---------|---------------|
| Authentication | JWT Token (Bearer) |
| Library | jjwt 0.12.6 |
| Token Provider | `JwtTokenProvider` |

### Event-Driven Architecture
- Kafka를 통한 도메인 이벤트 발행/구독
- 느슨한 결합을 통한 서비스 간 통신

### Validation Strategy
- Request DTO: 형식 검증 (Bean Validation)
- Command: 비즈니스 규칙
- Domain: 도메인 불변식

## Contact

프로젝트 관련 문의사항은 이슈를 등록해주세요.
