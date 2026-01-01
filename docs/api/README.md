# API Documentation

REST API 및 OpenAPI 스펙 문서입니다.

## Documents

| Document | Description |
|----------|-------------|
| [API.md](./API.md) | REST API 레퍼런스 |
| [openapi.yaml](./openapi.yaml) | OpenAPI 3.0 스펙 |

## Quick Reference

### Base URL

| Environment | URL |
|-------------|-----|
| Local | `http://localhost:8080` |
| Production | `https://api.delivery.com` |

### API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/orders` | 주문 생성 |
| `GET` | `/api/v1/orders/{orderId}` | 주문 조회 |
| `PATCH` | `/api/v1/orders/{orderId}/status` | 주문 상태 변경 |
| `POST` | `/api/v1/orders/{orderId}/accept` | 주문 수락 |
| `POST` | `/api/v1/orders/{orderId}/prepare` | 조리 시작 |
| `POST` | `/api/v1/orders/{orderId}/ready` | 배달 준비 완료 |
| `POST` | `/api/v1/orders/{orderId}/cancel` | 주문 취소 |
| `GET` | `/api/v1/orders/customer/{customerId}` | 고객별 주문 조회 |
| `GET` | `/api/v1/orders/shop/{shopId}` | 가게별 주문 조회 |

## Swagger UI

애플리케이션 실행 후 아래 URL에서 API 문서 확인:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

## Error Response Format (RFC 7807)

```json
{
  "type": "https://api.delivery.com/errors/validation",
  "title": "Validation Failed",
  "status": 400,
  "detail": "입력값 검증에 실패했습니다",
  "errors": {
    "customerId": "고객 ID는 필수입니다"
  },
  "timestamp": "2025-01-01T10:00:00Z"
}
```

## Related Documents

- [Architecture](../architecture/README.md)
- [Database Schema](../database/README.md)
