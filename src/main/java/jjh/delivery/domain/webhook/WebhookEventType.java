package jjh.delivery.domain.webhook;

/**
 * Webhook Event Type Enum
 * 웹훅으로 전달 가능한 이벤트 타입
 */
public enum WebhookEventType {

    // Order Events
    ORDER_CREATED("주문 생성"),
    ORDER_CONFIRMED("주문 확정"),
    ORDER_CANCELLED("주문 취소"),
    ORDER_COMPLETED("주문 완료"),

    // Payment Events
    PAYMENT_COMPLETED("결제 완료"),
    PAYMENT_FAILED("결제 실패"),
    PAYMENT_REFUNDED("결제 환불"),

    // Shipment Events
    SHIPMENT_CREATED("배송 생성"),
    SHIPMENT_PICKED_UP("상품 수거"),
    SHIPMENT_IN_TRANSIT("배송 중"),
    SHIPMENT_DELIVERED("배송 완료"),

    // Return Events
    RETURN_REQUESTED("반품 요청"),
    RETURN_APPROVED("반품 승인"),
    RETURN_COMPLETED("반품 완료"),

    // Review Events
    REVIEW_CREATED("리뷰 작성"),
    REVIEW_UPDATED("리뷰 수정");

    private final String description;

    WebhookEventType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
