# Architecture Documentation

시스템 아키텍처 및 설계 관련 문서입니다.

## Documents

| Document | Description |
|----------|-------------|
| [ARCHITECTURE.md](./ARCHITECTURE.md) | 시스템 아키텍처 (Hexagonal Architecture) |
| [PROJECT_STRUCTURE.md](./PROJECT_STRUCTURE.md) | 프로젝트 디렉토리 구조 |

## Overview

### Hexagonal Architecture (Ports & Adapters)

```
                    ┌─────────────────────────────────────┐
                    │           Adapters (In)             │
                    │  ┌─────────┐  ┌─────────────────┐   │
                    │  │   Web   │  │  Message Queue  │   │
                    │  │(REST API)│  │    (Kafka)      │   │
                    │  └────┬────┘  └────────┬────────┘   │
                    └───────┼────────────────┼────────────┘
                            │                │
                    ┌───────▼────────────────▼────────────┐
                    │         Application Layer           │
                    │  ┌──────────────────────────────┐   │
                    │  │       Use Cases / Services    │   │
                    │  │   (Ports In - Interfaces)     │   │
                    │  └──────────────────────────────┘   │
                    └───────────────┬─────────────────────┘
                                    │
                    ┌───────────────▼─────────────────────┐
                    │          Domain Layer               │
                    │  ┌──────────────────────────────┐   │
                    │  │  Entities, Value Objects     │   │
                    │  │  Domain Events, Exceptions   │   │
                    │  └──────────────────────────────┘   │
                    └───────────────┬─────────────────────┘
                                    │
                    ┌───────────────▼─────────────────────┐
                    │         Application Layer           │
                    │  ┌──────────────────────────────┐   │
                    │  │     Ports Out - Interfaces    │   │
                    │  └──────────────────────────────┘   │
                    └───────────────┬─────────────────────┘
                                    │
                    ┌───────────────▼─────────────────────┐
                    │          Adapters (Out)             │
                    │  ┌─────┐ ┌─────┐ ┌─────┐ ┌───────┐  │
                    │  │ JPA │ │jOOQ │ │Kafka│ │Elastic│  │
                    │  └─────┘ └─────┘ └─────┘ └───────┘  │
                    └─────────────────────────────────────┘
```

## Related Documents

- [API Documentation](../api/README.md)
- [Database Schema](../database/README.md)
- [Tech Stack](../guides/TECH_STACK.md)
