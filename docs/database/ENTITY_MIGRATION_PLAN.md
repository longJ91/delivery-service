# Entity Migration Plan: v1 → v2

## Overview

| 항목 | v1 (현재) | v2 (목표) |
|------|----------|----------|
| 서비스 유형 | 음식 배달 | 물품 배송 |
| Domain 수 | 1 (Order) | 9 |
| JPA Entity 수 | 2 | 27 |
| Domain Class 수 | 4 | 15 |

## Implementation Phases

### Phase 1: 기존 도메인 수정

#### 1.1 OrderStatus 변경
```java
// v1
PENDING → ACCEPTED → PREPARING → READY_FOR_DELIVERY → PICKED_UP → DELIVERED / CANCELLED

// v2
PENDING → PAID → CONFIRMED → PREPARING → SHIPPED → IN_TRANSIT → OUT_FOR_DELIVERY → DELIVERED
                                                                              ↓
                                                          CANCELLED ← RETURN_REQUESTED → RETURNED
```

#### 1.2 Order 필드 변경
| v1 Field | v2 Field | 변경 사항 |
|----------|----------|----------|
| shopId | sellerId | 명칭 변경 |
| deliveryAddress | shippingAddressSnapshot | JSONB로 변경 |
| - | orderNumber | 신규 (자동 생성) |
| - | subtotalAmount | 신규 |
| - | shippingFee | 신규 |
| - | discountAmount | 신규 |
| - | orderMemo | 신규 |
| - | shippingMemo | 신규 |
| - | paidAt, confirmedAt, shippedAt | 신규 타임스탬프 |

#### 1.3 OrderItem 필드 변경
| v1 Field | v2 Field | 변경 사항 |
|----------|----------|----------|
| menuId | productId | 명칭 변경 |
| menuName | productNameSnapshot | 명칭 변경 |
| - | variantId | 신규 |
| - | variantNameSnapshot | 신규 |
| - | skuSnapshot | 신규 |

### Phase 2: 기초 도메인 생성

#### 2.1 Customer Domain
```
domain/customer/
├── Customer.java
├── CustomerStatus.java
└── exception/CustomerNotFoundException.java

adapter/out/persistence/jpa/entity/
├── CustomerJpaEntity.java
└── CustomerAddressJpaEntity.java
```

#### 2.2 Seller Domain
```
domain/seller/
├── Seller.java
├── SellerType.java
├── SellerStatus.java
└── exception/SellerNotFoundException.java

adapter/out/persistence/jpa/entity/
├── SellerJpaEntity.java
├── SellerCategoryJpaEntity.java
└── SellerAddressJpaEntity.java
```

#### 2.3 Product Domain
```
domain/product/
├── Product.java
├── ProductVariant.java
├── ProductStatus.java
└── exception/ProductNotFoundException.java

adapter/out/persistence/jpa/entity/
├── ProductJpaEntity.java
├── ProductVariantJpaEntity.java
├── ProductCategoryJpaEntity.java
├── ProductImageJpaEntity.java
└── ProductSpecificationJpaEntity.java
```

### Phase 3: 핵심 비즈니스 도메인

#### 3.1 Shipment Domain
```
domain/shipment/
├── Shipment.java
├── ShipmentStatus.java
├── ShippingCarrier.java
└── exception/ShipmentNotFoundException.java

adapter/out/persistence/jpa/entity/
├── ShipmentJpaEntity.java
├── ShipmentTrackingJpaEntity.java
└── ShippingCarrierJpaEntity.java
```

#### 3.2 Payment Domain
```
domain/payment/
├── Payment.java
├── Refund.java
├── PaymentStatus.java
├── PaymentMethodType.java
└── exception/PaymentNotFoundException.java

adapter/out/persistence/jpa/entity/
├── PaymentJpaEntity.java
├── RefundJpaEntity.java
└── PaymentMethodJpaEntity.java
```

### Phase 4: 부가 도메인

#### 4.1 Return Domain
```
domain/returns/
├── ProductReturn.java
├── ReturnItem.java
├── ReturnStatus.java
├── ReturnReason.java
├── ReturnType.java
└── exception/ReturnNotFoundException.java

adapter/out/persistence/jpa/entity/
├── ReturnJpaEntity.java
└── ReturnItemJpaEntity.java
```

#### 4.2 Promotion Domain
```
domain/promotion/
├── Coupon.java
├── CustomerCoupon.java
├── DiscountType.java
├── CouponScope.java
└── exception/CouponNotFoundException.java

adapter/out/persistence/jpa/entity/
├── CouponJpaEntity.java
├── CustomerCouponJpaEntity.java
└── PromotionJpaEntity.java
```

#### 4.3 Review Domain
```
domain/review/
├── Review.java
├── ReviewReply.java
└── exception/ReviewNotFoundException.java

adapter/out/persistence/jpa/entity/
├── ReviewJpaEntity.java
├── ReviewImageJpaEntity.java
└── ReviewReplyJpaEntity.java
```

## File Count Summary

| Domain | Domain Classes | Enums | JPA Entities | Total |
|--------|---------------|-------|--------------|-------|
| Customer | 1 | 1 | 2 | 4 |
| Seller | 1 | 2 | 3 | 6 |
| Product | 2 | 1 | 5 | 8 |
| Order (수정) | 2 | 1 | 3 | 6 |
| Shipment | 1 | 2 | 3 | 6 |
| Payment | 2 | 2 | 3 | 7 |
| Return | 2 | 3 | 2 | 7 |
| Promotion | 2 | 2 | 3 | 7 |
| Review | 2 | 0 | 3 | 5 |
| **Total** | **15** | **14** | **27** | **56** |

## Execution Order

```
1. OrderStatus 수정 (상태 전이 규칙 변경)
2. Order/OrderItem 수정 (필드명 변경)
3. OrderJpaEntity/OrderItemJpaEntity 수정
4. Customer 도메인 생성
5. Seller 도메인 생성
6. Product 도메인 생성
7. Shipment 도메인 생성
8. Payment 도메인 생성
9. Return 도메인 생성
10. Promotion 도메인 생성
11. Review 도메인 생성
```
