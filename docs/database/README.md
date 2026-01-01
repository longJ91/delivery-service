# Database Documentation

데이터베이스 스키마 설계 및 관련 문서입니다.

## Documents

| Document | Description | Version |
|----------|-------------|---------|
| [SCHEMA.md](./SCHEMA.md) | 스키마 설계 문서 | 2.0.0 |
| [schema_v2.sql](./schema_v2.sql) | PostgreSQL DDL (물품 배송) | 2.0.0 |
| [schema.sql](./schema.sql) | PostgreSQL DDL (음식 배달) | 1.0.0 |
| [MIGRATION.md](./MIGRATION.md) | 마이그레이션 계획 | - |

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
│                    9 Domains                        │
├─────────────────────────────────────────────────────┤
│  User (2)     │ Seller (3)    │ Product (5)        │
│  Order (4)    │ Shipment (3)  │ Payment (3)        │
│  Promotion (3)│ Return (2)    │ Review (3)         │
├─────────────────────────────────────────────────────┤
│                  Total: 28 Tables                   │
└─────────────────────────────────────────────────────┘
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
