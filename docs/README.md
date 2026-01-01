# Delivery Service Documentation

## Overview

배달 서비스 백엔드 애플리케이션 문서입니다.

- **Architecture**: Hexagonal Architecture (Ports & Adapters)
- **Framework**: Spring Boot 4.0.1
- **Language**: Java 21

## Documentation Index

| Document | Description |
|----------|-------------|
| [ARCHITECTURE.md](./ARCHITECTURE.md) | 시스템 아키텍처 및 레이어 설명 |
| [PROJECT_STRUCTURE.md](./PROJECT_STRUCTURE.md) | 프로젝트 디렉토리 구조 |
| [TECH_STACK.md](./TECH_STACK.md) | 기술 스택 및 사용 방법 |
| [API.md](./API.md) | REST API 레퍼런스 |

## Quick Start

### Prerequisites

- Java 21
- Docker (PostgreSQL, Kafka, Elasticsearch)
- Gradle

### Run with Docker Compose

```bash
./gradlew bootRun
```

### Build

```bash
./gradlew build
```

## Key Features

### Hexagonal Architecture
- **Domain**: 순수 비즈니스 로직 (프레임워크 독립적)
- **Application**: Use Case 구현
- **Adapter**: 외부 시스템 연동

### Multi-Database Strategy
| Technology | Usage |
|------------|-------|
| JPA | CRUD, 단순 쿼리 |
| jOOQ | 복잡한 쿼리, 통계 |
| Elasticsearch | 전문 검색 |

### Event-Driven Architecture
- Kafka를 통한 도메인 이벤트 발행/구독
- 느슨한 결합을 통한 서비스 간 통신

### Validation Strategy
- Request DTO: 형식 검증 (Bean Validation)
- Command: 비즈니스 규칙
- Domain: 도메인 불변식

## Contact

프로젝트 관련 문의사항은 이슈를 등록해주세요.
