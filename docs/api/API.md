# API Reference v2

물품 배송 서비스 REST API 레퍼런스입니다.

## Base URL

| Environment | URL |
|-------------|-----|
| Local | `http://localhost:8080/api/v2` |
| Production | `https://api.delivery.com/api/v2` |

## Authentication

```http
Authorization: Bearer {access_token}
```

---

## 1. Auth API

### 회원가입
```http
POST /auth/register
Content-Type: application/json
```

**Request Body**
```json
{
  "email": "user@example.com",
  "password": "password123!",
  "name": "홍길동",
  "phone": "010-1234-5678"
}
```

**Response** `201 Created`
```json
{
  "id": "cust-uuid-123",
  "email": "user@example.com",
  "name": "홍길동",
  "phone": "010-1234-5678",
  "status": "ACTIVE",
  "createdAt": "2025-01-01T10:00:00"
}
```

### 로그인
```http
POST /auth/login
Content-Type: application/json
```

**Request Body**
```json
{
  "email": "user@example.com",
  "password": "password123!"
}
```

**Response** `200 OK`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 3600
}
```

---

## 2. Customer API

### 내 정보 조회
```http
GET /customers/me
Authorization: Bearer {token}
```

**Response** `200 OK`
```json
{
  "id": "cust-uuid-123",
  "email": "user@example.com",
  "name": "홍길동",
  "phone": "010-1234-5678",
  "profileImageUrl": null,
  "status": "ACTIVE",
  "pointBalance": 5000,
  "createdAt": "2025-01-01T10:00:00"
}
```

### 배송지 목록 조회
```http
GET /customers/me/addresses
Authorization: Bearer {token}
```

**Response** `200 OK`
```json
{
  "addresses": [
    {
      "id": "addr-uuid-123",
      "name": "집",
      "recipientName": "홍길동",
      "recipientPhone": "010-1234-5678",
      "postalCode": "06234",
      "roadAddress": "서울시 강남구 테헤란로 123",
      "detailAddress": "1층 101호",
      "isDefault": true
    }
  ]
}
```

### 배송지 등록
```http
POST /customers/me/addresses
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body**
```json
{
  "name": "회사",
  "recipientName": "홍길동",
  "recipientPhone": "010-1234-5678",
  "postalCode": "06234",
  "roadAddress": "서울시 강남구 테헤란로 456",
  "detailAddress": "10층",
  "isDefault": false
}
```

**Response** `201 Created`

### 기본 배송지 설정
```http
PATCH /customers/me/addresses/{addressId}/default
Authorization: Bearer {token}
```

**Response** `200 OK`

---

## 3. Product API

### 카테고리 목록 조회
```http
GET /categories
```

**Response** `200 OK`
```json
{
  "categories": [
    {
      "id": "cat-uuid-123",
      "name": "전자제품",
      "depth": 1,
      "children": [
        {
          "id": "cat-uuid-456",
          "name": "스마트폰",
          "depth": 2,
          "children": []
        }
      ]
    }
  ]
}
```

### 상품 목록 조회
```http
GET /products?categoryId={categoryId}&keyword={keyword}&minPrice={min}&maxPrice={max}&sort={sort}&page={page}&size={size}
```

**Query Parameters**
| Parameter | Type | Description |
|-----------|------|-------------|
| categoryId | string | 카테고리 ID |
| keyword | string | 검색어 |
| sellerId | string | 판매자 ID |
| minPrice | number | 최소 가격 |
| maxPrice | number | 최대 가격 |
| sort | string | 정렬 (recent, price_asc, price_desc, rating, sales) |
| page | number | 페이지 (0부터 시작) |
| size | number | 페이지 크기 (기본 20) |

**Response** `200 OK`
```json
{
  "content": [
    {
      "id": "prod-uuid-123",
      "name": "아이폰 15 Pro",
      "price": 1500000,
      "discountPrice": 1350000,
      "thumbnailUrl": "https://cdn.example.com/products/iphone15.jpg",
      "ratingAvg": 4.5,
      "reviewCount": 128,
      "sellerId": "seller-uuid-123",
      "sellerName": "애플스토어"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5
}
```

### 상품 상세 조회
```http
GET /products/{productId}
```

**Response** `200 OK`
```json
{
  "id": "prod-uuid-123",
  "sellerId": "seller-uuid-123",
  "sellerName": "애플스토어",
  "categoryId": "cat-uuid-456",
  "name": "아이폰 15 Pro",
  "description": "Apple의 최신 스마트폰입니다.",
  "price": 1500000,
  "discountPrice": 1350000,
  "stock": 50,
  "sku": "APPLE-IP15P-256",
  "status": "ACTIVE",
  "isFeatured": true,
  "images": [
    {
      "id": "img-uuid-123",
      "imageUrl": "https://cdn.example.com/products/iphone15-1.jpg",
      "isPrimary": true
    }
  ],
  "variants": [
    {
      "id": "var-uuid-123",
      "name": "256GB / 블랙 티타늄",
      "sku": "APPLE-IP15P-256-BT",
      "price": 1500000,
      "stock": 20,
      "optionValues": {
        "용량": "256GB",
        "색상": "블랙 티타늄"
      }
    }
  ],
  "ratingAvg": 4.5,
  "reviewCount": 128,
  "salesCount": 500,
  "createdAt": "2025-01-01T10:00:00"
}
```

### 상품 리뷰 목록 조회
```http
GET /products/{productId}/reviews?sort={sort}&page={page}&size={size}
```

**Query Parameters**
| Parameter | Type | Description |
|-----------|------|-------------|
| sort | string | 정렬 (recent, rating_high, rating_low) |
| page | number | 페이지 |
| size | number | 페이지 크기 |

**Response** `200 OK`
```json
{
  "content": [
    {
      "id": "review-uuid-123",
      "customerId": "cust-uuid-123",
      "customerName": "홍*동",
      "rating": 5,
      "content": "매우 만족합니다. 배송도 빠르고 상품 상태도 좋아요.",
      "images": [
        {
          "id": "img-uuid-123",
          "imageUrl": "https://cdn.example.com/reviews/img1.jpg"
        }
      ],
      "reply": {
        "content": "리뷰 감사합니다! 앞으로도 좋은 상품으로 보답하겠습니다.",
        "createdAt": "2025-01-02T10:00:00"
      },
      "createdAt": "2025-01-01T14:00:00"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 128,
  "totalPages": 13,
  "ratingDistribution": {
    "5": 80,
    "4": 30,
    "3": 10,
    "2": 5,
    "1": 3
  }
}
```

---

## 4. Seller API (Public)

### 판매자 정보 조회
```http
GET /sellers/{sellerId}
```

**Response** `200 OK`
```json
{
  "id": "seller-uuid-123",
  "name": "애플스토어",
  "description": "Apple 공식 리셀러입니다.",
  "sellerType": "BUSINESS",
  "ratingAvg": 4.8,
  "reviewCount": 1500,
  "productCount": 50,
  "status": "ACTIVE"
}
```

---

## 5. Cart API

### 장바구니 조회
```http
GET /cart
Authorization: Bearer {token}
```

**Response** `200 OK`
```json
{
  "items": [
    {
      "id": "cart-item-uuid-123",
      "productId": "prod-uuid-123",
      "productName": "아이폰 15 Pro",
      "variantId": "var-uuid-123",
      "variantName": "256GB / 블랙 티타늄",
      "quantity": 1,
      "unitPrice": 1350000,
      "subtotal": 1350000,
      "thumbnailUrl": "https://cdn.example.com/products/iphone15.jpg",
      "sellerId": "seller-uuid-123",
      "sellerName": "애플스토어",
      "stock": 20,
      "isAvailable": true
    }
  ],
  "totalAmount": 1350000,
  "totalItems": 1
}
```

### 장바구니 상품 추가
```http
POST /cart/items
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body**
```json
{
  "productId": "prod-uuid-123",
  "variantId": "var-uuid-123",
  "quantity": 1
}
```

**Response** `201 Created`

### 장바구니 수량 변경
```http
PATCH /cart/items/{itemId}
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body**
```json
{
  "quantity": 2
}
```

**Response** `200 OK`

### 장바구니 상품 삭제
```http
DELETE /cart/items/{itemId}
Authorization: Bearer {token}
```

**Response** `204 No Content`

---

## 6. Order API

### 주문 생성
```http
POST /orders
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body**
```json
{
  "sellerId": "seller-uuid-123",
  "items": [
    {
      "productId": "prod-uuid-123",
      "productName": "아이폰 15 Pro",
      "variantId": "var-uuid-123",
      "variantName": "256GB / 블랙 티타늄",
      "sku": "APPLE-IP15P-256-BT",
      "optionValues": {
        "용량": "256GB",
        "색상": "블랙 티타늄"
      },
      "quantity": 1,
      "unitPrice": 1350000
    }
  ],
  "shippingAddress": {
    "recipientName": "홍길동",
    "phoneNumber": "010-1234-5678",
    "postalCode": "06234",
    "address1": "서울시 강남구 테헤란로 123",
    "address2": "1층 101호",
    "deliveryNote": "문 앞에 놓아주세요"
  },
  "orderMemo": "빠른 배송 부탁드립니다",
  "couponId": "coupon-uuid-123"
}
```

**Response** `201 Created`
```json
{
  "id": "order-uuid-123",
  "orderNumber": "ORD20250101001234",
  "customerId": "cust-uuid-123",
  "sellerId": "seller-uuid-123",
  "items": [...],
  "status": "PENDING",
  "subtotalAmount": 1350000,
  "shippingFee": 0,
  "discountAmount": 100000,
  "totalAmount": 1250000,
  "shippingAddress": {...},
  "createdAt": "2025-01-01T10:00:00"
}
```

### 내 주문 목록 조회
```http
GET /orders?status={status}&page={page}&size={size}
Authorization: Bearer {token}
```

**Query Parameters**
| Parameter | Type | Description |
|-----------|------|-------------|
| status | string | 상태 필터 (PENDING, PAID, SHIPPED, DELIVERED 등) |
| page | number | 페이지 |
| size | number | 페이지 크기 |

**Response** `200 OK`
```json
{
  "content": [
    {
      "id": "order-uuid-123",
      "orderNumber": "ORD20250101001234",
      "sellerId": "seller-uuid-123",
      "sellerName": "애플스토어",
      "status": "DELIVERED",
      "totalAmount": 1250000,
      "itemCount": 1,
      "thumbnailUrl": "https://cdn.example.com/products/iphone15.jpg",
      "createdAt": "2025-01-01T10:00:00",
      "deliveredAt": "2025-01-03T14:00:00"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 50
}
```

### 주문 상세 조회
```http
GET /orders/{orderId}
Authorization: Bearer {token}
```

**Response** `200 OK`
```json
{
  "id": "order-uuid-123",
  "orderNumber": "ORD20250101001234",
  "customerId": "cust-uuid-123",
  "sellerId": "seller-uuid-123",
  "sellerName": "애플스토어",
  "items": [
    {
      "productId": "prod-uuid-123",
      "productName": "아이폰 15 Pro",
      "variantId": "var-uuid-123",
      "variantName": "256GB / 블랙 티타늄",
      "sku": "APPLE-IP15P-256-BT",
      "optionValues": {
        "용량": "256GB",
        "색상": "블랙 티타늄"
      },
      "quantity": 1,
      "unitPrice": 1350000,
      "subtotal": 1350000
    }
  ],
  "status": "DELIVERED",
  "subtotalAmount": 1350000,
  "shippingFee": 0,
  "discountAmount": 100000,
  "totalAmount": 1250000,
  "shippingAddress": {
    "recipientName": "홍길동",
    "phoneNumber": "010-1234-5678",
    "postalCode": "06234",
    "address1": "서울시 강남구 테헤란로 123",
    "address2": "1층 101호",
    "deliveryNote": "문 앞에 놓아주세요"
  },
  "orderMemo": "빠른 배송 부탁드립니다",
  "shipment": {
    "id": "ship-uuid-123",
    "carrier": "CJ_LOGISTICS",
    "carrierName": "CJ대한통운",
    "trackingNumber": "1234567890123",
    "trackingUrl": "https://www.cjlogistics.com/ko/tool/parcel/tracking?gnbInvcNo=1234567890123",
    "status": "DELIVERED"
  },
  "paidAt": "2025-01-01T10:05:00",
  "confirmedAt": "2025-01-01T10:10:00",
  "shippedAt": "2025-01-01T14:00:00",
  "deliveredAt": "2025-01-03T14:00:00",
  "createdAt": "2025-01-01T10:00:00"
}
```

### 주문 취소
```http
POST /orders/{orderId}/cancel
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body**
```json
{
  "reason": "단순 변심"
}
```

**Response** `200 OK`

**Validation**
- PENDING, PAID, CONFIRMED 상태에서만 취소 가능
- PREPARING 이후 상태에서는 반품 절차 필요

### 구매 확정
```http
POST /orders/{orderId}/confirm-receipt
Authorization: Bearer {token}
```

**Response** `200 OK`

**Validation**
- DELIVERED 상태에서만 가능

---

## 7. Payment API

### 결제 요청
```http
POST /payments
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body**
```json
{
  "orderId": "order-uuid-123",
  "method": "CARD",
  "amount": 1250000
}
```

**Response** `200 OK`
```json
{
  "paymentId": "pay-uuid-123",
  "orderId": "order-uuid-123",
  "status": "PENDING",
  "amount": 1250000,
  "method": "CARD",
  "checkoutUrl": "https://pay.example.com/checkout/abc123"
}
```

### 결제 확인 (PG 콜백 후)
```http
POST /payments/{paymentId}/confirm
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body**
```json
{
  "paymentKey": "toss_payment_key_123",
  "transactionId": "txn-abc-123"
}
```

**Response** `200 OK`
```json
{
  "paymentId": "pay-uuid-123",
  "orderId": "order-uuid-123",
  "status": "COMPLETED",
  "amount": 1250000,
  "paidAt": "2025-01-01T10:05:00"
}
```

---

## 8. Shipment API

### 배송 정보 조회
```http
GET /shipments/{shipmentId}
Authorization: Bearer {token}
```

**Response** `200 OK`
```json
{
  "id": "ship-uuid-123",
  "orderId": "order-uuid-123",
  "carrier": "CJ_LOGISTICS",
  "carrierName": "CJ대한통운",
  "trackingNumber": "1234567890123",
  "trackingUrl": "https://www.cjlogistics.com/ko/tool/parcel/tracking?gnbInvcNo=1234567890123",
  "status": "DELIVERED",
  "estimatedDeliveryDate": "2025-01-03",
  "deliveredAt": "2025-01-03T14:00:00"
}
```

### 배송 추적 조회
```http
GET /shipments/{shipmentId}/tracking
Authorization: Bearer {token}
```

**Response** `200 OK`
```json
{
  "shipmentId": "ship-uuid-123",
  "carrier": "CJ_LOGISTICS",
  "trackingNumber": "1234567890123",
  "status": "DELIVERED",
  "events": [
    {
      "status": "DELIVERED",
      "location": "서울 강남구 역삼동",
      "description": "배송완료 (문앞 배송)",
      "occurredAt": "2025-01-03T14:00:00"
    },
    {
      "status": "OUT_FOR_DELIVERY",
      "location": "서울 강남 대리점",
      "description": "배달 출발",
      "occurredAt": "2025-01-03T08:00:00"
    },
    {
      "status": "IN_TRANSIT",
      "location": "서울 동작 Hub",
      "description": "간선 상차",
      "occurredAt": "2025-01-02T22:00:00"
    },
    {
      "status": "PICKED_UP",
      "location": "경기 화성 집하장",
      "description": "집하",
      "occurredAt": "2025-01-01T16:00:00"
    }
  ]
}
```

---

## 9. Return API

### 반품/교환 요청
```http
POST /returns
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body**
```json
{
  "orderId": "order-uuid-123",
  "type": "REFUND",
  "reason": "DEFECTIVE",
  "reasonDetail": "상품 불량 - 화면에 긁힘이 있습니다",
  "items": [
    {
      "orderItemIndex": 0,
      "quantity": 1
    }
  ],
  "images": [
    "https://cdn.example.com/returns/img1.jpg"
  ],
  "pickupAddress": {
    "recipientName": "홍길동",
    "phoneNumber": "010-1234-5678",
    "postalCode": "06234",
    "address1": "서울시 강남구 테헤란로 123",
    "address2": "1층 101호"
  }
}
```

**Response** `201 Created`
```json
{
  "id": "return-uuid-123",
  "orderId": "order-uuid-123",
  "type": "REFUND",
  "status": "REQUESTED",
  "reason": "DEFECTIVE",
  "estimatedRefundAmount": 1250000,
  "createdAt": "2025-01-05T10:00:00"
}
```

### 반품 목록 조회
```http
GET /returns?status={status}&page={page}&size={size}
Authorization: Bearer {token}
```

**Response** `200 OK`
```json
{
  "content": [
    {
      "id": "return-uuid-123",
      "orderId": "order-uuid-123",
      "orderNumber": "ORD20250101001234",
      "type": "REFUND",
      "status": "COMPLETED",
      "reason": "DEFECTIVE",
      "refundAmount": 1250000,
      "createdAt": "2025-01-05T10:00:00",
      "completedAt": "2025-01-10T14:00:00"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 5
}
```

### 반품 상세 조회
```http
GET /returns/{returnId}
Authorization: Bearer {token}
```

**Response** `200 OK`
```json
{
  "id": "return-uuid-123",
  "orderId": "order-uuid-123",
  "orderNumber": "ORD20250101001234",
  "type": "REFUND",
  "status": "COMPLETED",
  "reason": "DEFECTIVE",
  "reasonDetail": "상품 불량 - 화면에 긁힘이 있습니다",
  "items": [
    {
      "productName": "아이폰 15 Pro",
      "variantName": "256GB / 블랙 티타늄",
      "quantity": 1,
      "refundAmount": 1250000
    }
  ],
  "pickupShipment": {
    "carrier": "CJ_LOGISTICS",
    "trackingNumber": "9876543210123",
    "status": "COLLECTED"
  },
  "refundAmount": 1250000,
  "createdAt": "2025-01-05T10:00:00",
  "approvedAt": "2025-01-05T14:00:00",
  "collectedAt": "2025-01-07T10:00:00",
  "completedAt": "2025-01-10T14:00:00"
}
```

### 반품 취소
```http
POST /returns/{returnId}/cancel
Authorization: Bearer {token}
```

**Response** `200 OK`

**Validation**
- REQUESTED 상태에서만 취소 가능

---

## 10. Review API

### 리뷰 작성
```http
POST /reviews
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body**
```json
{
  "orderId": "order-uuid-123",
  "productId": "prod-uuid-123",
  "rating": 5,
  "content": "매우 만족합니다. 배송도 빠르고 상품 상태도 좋아요.",
  "images": [
    "https://cdn.example.com/reviews/img1.jpg"
  ]
}
```

**Response** `201 Created`
```json
{
  "id": "review-uuid-123",
  "orderId": "order-uuid-123",
  "productId": "prod-uuid-123",
  "rating": 5,
  "content": "매우 만족합니다. 배송도 빠르고 상품 상태도 좋아요.",
  "images": [...],
  "createdAt": "2025-01-05T10:00:00"
}
```

**Validation**
- DELIVERED 상태의 주문만 리뷰 작성 가능
- 주문당 1개의 리뷰만 작성 가능

### 리뷰 수정
```http
PUT /reviews/{reviewId}
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body**
```json
{
  "rating": 4,
  "content": "전반적으로 만족하지만 포장이 아쉬웠어요.",
  "images": []
}
```

**Response** `200 OK`

### 리뷰 삭제
```http
DELETE /reviews/{reviewId}
Authorization: Bearer {token}
```

**Response** `204 No Content`

---

## 11. Coupon API

### 사용 가능 쿠폰 목록
```http
GET /coupons?productId={productId}&sellerId={sellerId}
Authorization: Bearer {token}
```

**Response** `200 OK`
```json
{
  "coupons": [
    {
      "id": "coupon-uuid-123",
      "code": "WELCOME10",
      "name": "신규 가입 10% 할인",
      "discountType": "PERCENTAGE",
      "discountValue": 10,
      "minimumOrderAmount": 50000,
      "maximumDiscountAmount": 10000,
      "validUntil": "2025-12-31T23:59:59"
    }
  ]
}
```

### 쿠폰 발급
```http
POST /coupons/{couponCode}/claim
Authorization: Bearer {token}
```

**Response** `200 OK`
```json
{
  "id": "customer-coupon-uuid-123",
  "couponId": "coupon-uuid-123",
  "code": "WELCOME10",
  "name": "신규 가입 10% 할인",
  "status": "AVAILABLE",
  "validUntil": "2025-12-31T23:59:59"
}
```

### 내 쿠폰 목록
```http
GET /customers/me/coupons?status={status}
Authorization: Bearer {token}
```

**Response** `200 OK`
```json
{
  "coupons": [
    {
      "id": "customer-coupon-uuid-123",
      "couponId": "coupon-uuid-123",
      "code": "WELCOME10",
      "name": "신규 가입 10% 할인",
      "discountType": "PERCENTAGE",
      "discountValue": 10,
      "minimumOrderAmount": 50000,
      "maximumDiscountAmount": 10000,
      "status": "AVAILABLE",
      "validUntil": "2025-12-31T23:59:59"
    }
  ]
}
```

---

## 12. Seller Admin API

### 내 상품 목록
```http
GET /sellers/me/products?status={status}&page={page}&size={size}
Authorization: Bearer {token}
```

**Response** `200 OK`

### 상품 등록
```http
POST /sellers/me/products
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body**
```json
{
  "categoryId": "cat-uuid-456",
  "name": "아이폰 15 Pro",
  "description": "Apple의 최신 스마트폰입니다.",
  "price": 1500000,
  "stock": 100,
  "sku": "APPLE-IP15P",
  "images": [
    {
      "imageUrl": "https://cdn.example.com/products/iphone15.jpg",
      "isPrimary": true
    }
  ],
  "variants": [
    {
      "name": "256GB / 블랙 티타늄",
      "sku": "APPLE-IP15P-256-BT",
      "price": 1500000,
      "stock": 20,
      "optionValues": {
        "용량": "256GB",
        "색상": "블랙 티타늄"
      }
    }
  ]
}
```

**Response** `201 Created`

### 주문 목록 (판매자)
```http
GET /sellers/me/orders?status={status}&page={page}&size={size}
Authorization: Bearer {token}
```

**Response** `200 OK`

### 주문 확정
```http
POST /sellers/me/orders/{orderId}/confirm
Authorization: Bearer {token}
```

**Response** `200 OK`

### 상품 준비 시작
```http
POST /sellers/me/orders/{orderId}/prepare
Authorization: Bearer {token}
```

**Response** `200 OK`

### 출고 완료 (운송장 등록)
```http
POST /sellers/me/orders/{orderId}/ship
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body**
```json
{
  "carrier": "CJ_LOGISTICS",
  "trackingNumber": "1234567890123"
}
```

**Response** `200 OK`

### 반품 승인
```http
POST /sellers/me/returns/{returnId}/approve
Authorization: Bearer {token}
```

**Response** `200 OK`

### 반품 거절
```http
POST /sellers/me/returns/{returnId}/reject
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body**
```json
{
  "reason": "상품 훼손으로 인한 반품 불가"
}
```

**Response** `200 OK`

### 리뷰 답글 작성
```http
POST /sellers/me/reviews/{reviewId}/reply
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body**
```json
{
  "content": "리뷰 감사합니다! 앞으로도 좋은 상품으로 보답하겠습니다."
}
```

**Response** `201 Created`

---

## 13. Webhook API

### 결제 웹훅
```http
POST /webhooks/payments/{provider}
```

| Provider | Description |
|----------|-------------|
| `toss` | 토스페이먼츠 |
| `kakaopay` | 카카오페이 |
| `naverpay` | 네이버페이 |

### 배송 웹훅
```http
POST /webhooks/shipments/{carrier}
```

| Carrier | Description |
|---------|-------------|
| `cj` | CJ대한통운 |
| `hanjin` | 한진택배 |
| `lotte` | 롯데택배 |
| `logen` | 로젠택배 |
| `post` | 우체국택배 |

---

## Order Status Flow

```
PENDING ──────▶ PAID ──────▶ CONFIRMED ──────▶ PREPARING
    │             │              │                  │
    │             │              │                  │
    ▼             ▼              ▼                  ▼
CANCELLED    CANCELLED      CANCELLED           SHIPPED
                                                    │
                                                    ▼
                                               IN_TRANSIT
                                                    │
                                                    ▼
                                            OUT_FOR_DELIVERY
                                                    │
                                                    ▼
                                               DELIVERED
                                                    │
                                                    ▼
                                            RETURN_REQUESTED
                                                    │
                                              ┌─────┴─────┐
                                              ▼           ▼
                                          RETURNED    DELIVERED
                                                    (반품 거절)
```

---

## Error Response Format (RFC 7807)

모든 에러 응답은 RFC 7807 형식을 따릅니다.

```json
{
  "type": "https://api.delivery.com/errors/validation",
  "title": "Validation Failed",
  "status": 400,
  "detail": "입력값 검증에 실패했습니다",
  "errors": {
    "email": "유효한 이메일 형식이 아닙니다",
    "password": "비밀번호는 8자 이상이어야 합니다"
  },
  "timestamp": "2025-01-01T10:00:00Z"
}
```

### 에러 타입

| Type | Status | Description |
|------|--------|-------------|
| `/errors/validation` | 400 | 입력값 검증 실패 |
| `/errors/unauthorized` | 401 | 인증 필요 |
| `/errors/forbidden` | 403 | 권한 없음 |
| `/errors/not-found` | 404 | 리소스를 찾을 수 없음 |
| `/errors/conflict` | 409 | 상태 충돌 |
| `/errors/internal` | 500 | 서버 오류 |
