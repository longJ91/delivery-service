# Guides & References

기술 스택 및 개발 가이드 문서입니다.

## Documents

| Document | Description |
|----------|-------------|
| [TECH_STACK.md](./TECH_STACK.md) | 기술 스택 및 사용 방법 |

## Tech Stack Overview

### Core Framework

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 | Language |
| Spring Boot | 4.0.1 | Framework |
| Gradle | 8.x | Build Tool |

### Data Access

| Technology | Purpose |
|------------|---------|
| Spring Data JPA | CRUD, 단순 쿼리 |
| jOOQ | 복잡한 쿼리, 통계 |
| Elasticsearch | 전문 검색 |

### Messaging

| Technology | Purpose |
|------------|---------|
| Apache Kafka | 이벤트 기반 통신 |

### Infrastructure

| Technology | Purpose |
|------------|---------|
| PostgreSQL | Primary Database |
| Docker Compose | Local Development |
| Prometheus | Metrics |

## Quick Commands

```bash
# 빌드
./gradlew build

# 실행
./gradlew bootRun

# 테스트
./gradlew test

# Docker Compose로 인프라 실행
docker-compose up -d
```

## Related Documents

- [Architecture](../architecture/README.md)
- [API Documentation](../api/README.md)
- [Database Schema](../database/README.md)
