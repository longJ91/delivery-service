# API Documentation

물품 배송 서비스 REST API 문서입니다.

## Documents

| Document | Description |
|----------|-------------|
| [USER_FLOW.md](./USER_FLOW.md) | 사용자 서비스 플로우 (Customer/Seller Journey) |
| [EVENT_FLOW.md](./EVENT_FLOW.md) | 이벤트 기반 아키텍처 흐름 |
| [API.md](./API.md) | REST API 레퍼런스 |
| [openapi.yaml](./openapi.yaml) | OpenAPI 3.0 스펙 |

## Quick Reference

### Base URL

| Environment | URL |
|-------------|-----|
| Local | `http://localhost:8080` |
| Production | `https://api.delivery.com` |

### Authentication

```http
Authorization: Bearer <access_token>
```

| Endpoint | Authentication |
|----------|----------------|
| `POST /auth/*` | 불필요 |
| `GET /products/*` | 불필요 |
| `GET /categories` | 불필요 |
| 기타 모든 엔드포인트 | 필요 |

---

## API Endpoints Overview

### 인증 (Auth)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/auth/register` | 회원가입 |
| `POST` | `/auth/login` | 로그인 |
| `POST` | `/auth/refresh` | 토큰 갱신 |
| `POST` | `/auth/logout` | 로그아웃 |

### 고객 (Customer)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/customers/me` | 내 정보 조회 |
| `PUT` | `/customers/me` | 내 정보 수정 |
| `GET` | `/customers/me/addresses` | 배송지 목록 |
| `POST` | `/customers/me/addresses` | 배송지 등록 |
| `PUT` | `/customers/me/addresses/{id}` | 배송지 수정 |
| `DELETE` | `/customers/me/addresses/{id}` | 배송지 삭제 |

### 상품 (Product)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/categories` | 카테고리 목록 |
| `GET` | `/products` | 상품 목록 (검색, 필터) |
| `GET` | `/products/{id}` | 상품 상세 |
| `GET` | `/products/{id}/variants` | 변형 상품 목록 |
| `GET` | `/products/{id}/reviews` | 상품 리뷰 목록 |

### 판매자 (Seller)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/sellers/{id}` | 판매자 정보 조회 |

### 장바구니 (Cart)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/cart` | 장바구니 조회 |
| `POST` | `/cart/items` | 상품 추가 |
| `PATCH` | `/cart/items/{id}` | 수량 변경 |
| `DELETE` | `/cart/items/{id}` | 상품 삭제 |
| `DELETE` | `/cart` | 장바구니 비우기 |

### 주문 (Order)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/orders` | 주문 생성 |
| `GET` | `/orders` | 내 주문 목록 |
| `GET` | `/orders/{id}` | 주문 상세 |
| `GET` | `/orders/{id}/shipment` | 배송 정보 |
| `POST` | `/orders/{id}/cancel` | 주문 취소 |
| `POST` | `/orders/{id}/confirm-receipt` | 구매 확정 |

### 결제 (Payment)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/payments` | 결제 요청 |
| `POST` | `/payments/{id}/confirm` | 결제 확인 |
| `GET` | `/payments/{id}` | 결제 정보 조회 |

### 배송 (Shipment)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/shipments/{id}` | 배송 상세 |
| `GET` | `/shipments/{id}/tracking` | 배송 추적 이벤트 |

### 반품 (Return)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/returns` | 반품/교환 요청 |
| `GET` | `/returns` | 내 반품 목록 |
| `GET` | `/returns/{id}` | 반품 상세 |
| `POST` | `/returns/{id}/cancel` | 반품 취소 |

### 리뷰 (Review)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/reviews` | 리뷰 작성 |
| `PUT` | `/reviews/{id}` | 리뷰 수정 |
| `DELETE` | `/reviews/{id}` | 리뷰 삭제 |

### 쿠폰 (Coupon)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/coupons` | 사용 가능 쿠폰 |
| `POST` | `/coupons/{code}/claim` | 쿠폰 등록 |

---

## Seller Admin Endpoints

### 상품 관리
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/sellers/me/products` | 내 상품 목록 |
| `POST` | `/sellers/me/products` | 상품 등록 |
| `PUT` | `/sellers/me/products/{id}` | 상품 수정 |
| `DELETE` | `/sellers/me/products/{id}` | 상품 삭제 |
| `PATCH` | `/sellers/me/products/{id}/stock` | 재고 수정 |
| `PATCH` | `/sellers/me/products/{id}/status` | 상태 변경 |

### 주문 관리
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/sellers/me/orders` | 주문 목록 |
| `GET` | `/sellers/me/orders/{id}` | 주문 상세 |
| `POST` | `/sellers/me/orders/{id}/confirm` | 주문 확정 |
| `POST` | `/sellers/me/orders/{id}/prepare` | 준비 시작 |
| `POST` | `/sellers/me/orders/{id}/ship` | 출고 (운송장 등록) |
| `POST` | `/sellers/me/orders/{id}/cancel` | 주문 취소 |

### 반품 관리
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/sellers/me/returns` | 반품 목록 |
| `POST` | `/sellers/me/returns/{id}/approve` | 반품 승인 |
| `POST` | `/sellers/me/returns/{id}/reject` | 반품 거절 |
| `POST` | `/sellers/me/returns/{id}/complete` | 반품 완료 |

### 리뷰 관리
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/sellers/me/reviews` | 내 상품 리뷰 |
| `POST` | `/sellers/me/reviews/{id}/reply` | 답글 작성 |
| `PUT` | `/sellers/me/reviews/{id}/reply` | 답글 수정 |
| `DELETE` | `/sellers/me/reviews/{id}/reply` | 답글 삭제 |

---

## Order Status Flow

```
PENDING → PAID → CONFIRMED → PREPARING → SHIPPED → IN_TRANSIT → OUT_FOR_DELIVERY → DELIVERED
    ↓        ↓        ↓           ↓
CANCELLED CANCELLED CANCELLED  (반품 절차)
```

| Status | Description | 취소 가능 |
|--------|-------------|----------|
| `PENDING` | 주문 생성 (결제 전) | ✅ |
| `PAID` | 결제 완료 | ✅ |
| `CONFIRMED` | 판매자 확정 | ✅ |
| `PREPARING` | 상품 준비중 | △ (협의) |
| `SHIPPED` | 출고 완료 | ❌ |
| `IN_TRANSIT` | 배송중 | ❌ |
| `OUT_FOR_DELIVERY` | 배달중 | ❌ |
| `DELIVERED` | 배송 완료 | ❌ |
| `CANCELLED` | 주문 취소 | - |
| `RETURN_REQUESTED` | 반품 요청 | - |
| `RETURNED` | 반품 완료 | - |

---

## Swagger UI

애플리케이션 실행 후 아래 URL에서 API 문서 확인:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

---

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

### Error Types

| Type | Status | Description |
|------|--------|-------------|
| `/errors/validation` | 400 | 입력값 검증 실패 |
| `/errors/authentication` | 401 | 인증 실패 |
| `/errors/forbidden` | 403 | 권한 없음 |
| `/errors/not-found` | 404 | 리소스 없음 |
| `/errors/conflict` | 409 | 상태 충돌 |
| `/errors/internal` | 500 | 서버 오류 |

---

## Related Documents

- [Architecture](../architecture/README.md)
- [Database Schema](../database/README.md)
