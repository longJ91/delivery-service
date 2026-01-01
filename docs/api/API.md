# API Reference

## Base URL

```
http://localhost:8080/api/v1
```

## Orders API

### Create Order

```http
POST /orders
Content-Type: application/json
```

**Request Body**
```json
{
  "customerId": "customer-123",
  "shopId": "shop-456",
  "items": [
    {
      "menuId": "menu-001",
      "menuName": "치킨",
      "quantity": 2,
      "unitPrice": 18000
    }
  ],
  "deliveryAddress": "서울시 강남구 테헤란로 123"
}
```

**Response** `201 Created`
```json
{
  "id": "order-789",
  "customerId": "customer-123",
  "shopId": "shop-456",
  "items": [
    {
      "menuId": "menu-001",
      "menuName": "치킨",
      "quantity": 2,
      "unitPrice": 18000,
      "subtotal": 36000
    }
  ],
  "status": "PENDING",
  "totalAmount": 36000,
  "deliveryAddress": "서울시 강남구 테헤란로 123",
  "createdAt": "2025-01-01T10:00:00",
  "updatedAt": "2025-01-01T10:00:00"
}
```

**Validation Errors** `400 Bad Request`
```json
{
  "type": "https://api.delivery.com/errors/validation",
  "title": "Validation Failed",
  "status": 400,
  "detail": "입력값 검증에 실패했습니다",
  "errors": {
    "customerId": "고객 ID는 필수입니다",
    "items": "주문 항목은 최소 1개 이상이어야 합니다"
  },
  "timestamp": "2025-01-01T10:00:00Z"
}
```

---

### Get Order

```http
GET /orders/{orderId}
```

**Response** `200 OK`
```json
{
  "id": "order-789",
  "customerId": "customer-123",
  "shopId": "shop-456",
  "items": [...],
  "status": "PENDING",
  "totalAmount": 36000,
  "deliveryAddress": "서울시 강남구 테헤란로 123",
  "createdAt": "2025-01-01T10:00:00",
  "updatedAt": "2025-01-01T10:00:00"
}
```

**Not Found** `404 Not Found`
```json
{
  "type": "https://api.delivery.com/errors/order-not-found",
  "title": "Order Not Found",
  "status": 404,
  "detail": "Order not found: order-789",
  "orderId": "order-789",
  "timestamp": "2025-01-01T10:00:00Z"
}
```

---

### Update Order Status

```http
PATCH /orders/{orderId}/status
Content-Type: application/json
```

**Request Body**
```json
{
  "status": "ACCEPTED"
}
```

**Response** `200 OK`

---

### Order Status Actions

| Endpoint | Action | Valid From Status |
|----------|--------|-------------------|
| `POST /orders/{id}/accept` | Accept Order | PENDING |
| `POST /orders/{id}/prepare` | Start Preparing | ACCEPTED |
| `POST /orders/{id}/ready` | Ready for Delivery | PREPARING |
| `POST /orders/{id}/cancel` | Cancel Order | PENDING, ACCEPTED, PREPARING, READY_FOR_DELIVERY |

**State Transition Error** `409 Conflict`
```json
{
  "type": "https://api.delivery.com/errors/invalid-state",
  "title": "Invalid State Transition",
  "status": 409,
  "detail": "Cannot transition from DELIVERED to CANCELLED",
  "timestamp": "2025-01-01T10:00:00Z"
}
```

---

### Get Orders by Customer

```http
GET /orders/customer/{customerId}
```

**Response** `200 OK`
```json
[
  {
    "id": "order-789",
    "customerId": "customer-123",
    ...
  }
]
```

---

### Get Orders by Shop

```http
GET /orders/shop/{shopId}
```

**Response** `200 OK`
```json
[
  {
    "id": "order-789",
    "shopId": "shop-456",
    ...
  }
]
```

---

## Order Status Flow

```
PENDING ──────▶ ACCEPTED ──────▶ PREPARING ──────▶ READY_FOR_DELIVERY
    │              │                 │                     │
    │              │                 │                     │
    ▼              ▼                 ▼                     ▼
CANCELLED     CANCELLED         CANCELLED            PICKED_UP
                                                          │
                                                          ▼
                                                      DELIVERED
```

## Validation Rules

### CreateOrderRequest

| Field | Constraint | Message |
|-------|------------|---------|
| `customerId` | @NotBlank | 고객 ID는 필수입니다 |
| `shopId` | @NotBlank | 가게 ID는 필수입니다 |
| `items` | @NotEmpty | 주문 항목은 최소 1개 이상이어야 합니다 |
| `deliveryAddress` | @NotBlank, @Size(max=500) | 배송 주소는 필수입니다 / 500자 이하 |

### OrderItemRequest

| Field | Constraint | Message |
|-------|------------|---------|
| `menuId` | @NotBlank | 메뉴 ID는 필수입니다 |
| `menuName` | @NotBlank | 메뉴명은 필수입니다 |
| `quantity` | @Positive | 수량은 1 이상이어야 합니다 |
| `unitPrice` | @NotNull, @Positive | 단가는 필수입니다 / 0보다 커야 합니다 |

### Business Rules

| Rule | Constraint |
|------|------------|
| Max Items per Order | 50개 |
